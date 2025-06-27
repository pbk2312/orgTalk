import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import ChatHeader from './ChatHeader';
import Sidebar from './Sidebar';
import ChatInput from './ChatInput';
import CodeModal from './CodeModal';
import MessageItem from './MessageItem';
import { getChatRoomInfo, getChatsByCursor, deleteChatRoom , updateChatRoom} from '../../service/ChatService.jsx';
import { useChatStomp } from '../../hooks/useChatStomp.js';
import { useAuth } from '../../hooks/useAuth.ts';
import styles from '../../css/ChatRoom.module.css';

const ChatRoom = () => {
  const navigate = useNavigate();
  const { roomId } = useParams();
  const roomIdNum = Number(roomId);

  const { auth, loading: authLoading } = useAuth();

  const [roomInfo, setRoomInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [messages, setMessages] = useState([]);
  const [participants, setParticipants] = useState([]);


  const [initParticipants, setInitParticipants] = useState([]);


  const [nextCursor, setNextCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);


  const [inputMessage, setInputMessage] = useState('');
  const [isComposing, setIsComposing] = useState(false);
  const [showCodeModal, setShowCodeModal] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [copiedCodeId, setCopiedCodeId] = useState(null);


  const [shouldScrollToBottom, setShouldScrollToBottom] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const codeTextareaRef = useRef(null);
  const isFirstLoad = useRef(true);

  // 공통 사용자 프로필 생성 함수
  const createUserProfile = useCallback((userId, name, avatarUrl) => ({
    userId: String(userId),
    login: name,
    avatarUrl: avatarUrl || null
  }), []);

  // 메시지 매핑 함수
  const mapChatToMessage = useCallback((chat) => {
    const initInfo = initParticipants.find(u => u.userId === String(chat.senderId));
    return {
      id: chat.id,
      userId: String(chat.senderId),
      nickname: chat.senderName,
      content: chat.message,
      timestamp: chat.createdAt,
      isMe: chat.senderId === auth.id,
      type: chat.messageType === 'CODE' ? 'code' : 'text',
      language: chat.language?.toLowerCase() || null,
      code: chat.codeContent,
      senderProfile: createUserProfile(
        chat.senderId,
        chat.senderName,
        chat.senderAvatarUrl || initInfo?.avatarUrl
      )
    };
  }, [auth.id, initParticipants, createUserProfile]);


  const handleMemberKicked = useCallback((kickedUserId) => {
  console.log(`[ChatRoom] Member ${kickedUserId} was kicked`);
  

  setInitParticipants(prev => 
    prev.filter(p => p.userId !== String(kickedUserId))
  );
  

  setParticipants(prev => 
    prev.filter(p => p.userId !== String(kickedUserId))
  );
}, []);


  const createMessage = useCallback((messageData, isCode = false) => ({
    id: messageData.id || Date.now(),
    userId: String(auth.id),
    nickname: auth.login || '나',
    content: isCode ? '' : messageData.content,
    timestamp: new Date().toISOString(),
    isMe: true,
    type: isCode ? 'code' : 'text',
    ...(isCode && {
      language: messageData.language,
      code: messageData.code
    }),
    senderProfile: createUserProfile(auth.id, auth.login || '나', auth.avatarUrl)
  }), [auth, createUserProfile]);


  const createStompMessage = useCallback((message, isCode = false) => ({
    roomId,
    message: isCode ? '' : message.content,
    messageType: isCode ? 'CODE' : 'TEXT',
    codeContent: isCode ? message.code : null,
    language: isCode ? message.language?.toUpperCase() : null,
    senderId: auth.id,
    senderName: auth.login || '나',
    createdAt: message.timestamp
  }), [roomId, auth]);

  const handleUpdateRoom = async (updateData) => {
    console.log('📌 handleUpdateRoom called for roomId =', roomIdNum, 'updateData =', updateData);
    try {
      await updateChatRoom({ roomId: roomIdNum, ...updateData });
      console.log('✅ updateChatRoom completed, reloading info for', roomIdNum);
      const fresh = await getChatRoomInfo(roomIdNum);
      setRoomInfo(fresh);
    } catch (err) {
      console.error('채팅방 수정 실패:', err);
    }
  };

  const handleDeleteRoom = async () => {
    if (!roomIdNum) {
      console.warn('삭제할 roomId가 없습니다:', roomIdNum);
      return;
    }
    try {
      await deleteChatRoom(roomIdNum);
      navigate(-1);
    } catch (err) {
      console.error('채팅방 삭제 실패:', err);
    }
  };


  useEffect(() => {
    if (authLoading) return;
    
    const loadRoomInfo = async () => {
      try {
        setLoading(true);
        const data = await getChatRoomInfo(roomId);
        console.log('[ChatRoom] fetched roomInfo:', data);

        setRoomInfo(data);

        if (data.members) {
          const memberList = data.members.map(u => 
            createUserProfile(u.id, u.login, u.avatarUrl)
          );
          setInitParticipants(memberList);
        }
      } catch (err) {
        console.error(err);
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    loadRoomInfo();
  }, [authLoading, roomId, createUserProfile]);


  const loadChats = useCallback(async (cursor = null) => {
    if (!auth.authenticated || isNaN(roomId)) return;
    
    try {
      const { chats, nextCursor: newCursor } = await getChatsByCursor(roomId, cursor);
      const mappedMessages = chats.map(mapChatToMessage);

      setMessages(prev => {
        if (!cursor) return mappedMessages;
        const existingIds = new Set(prev.map(m => m.id));
        const newMessages = mappedMessages.filter(m => !existingIds.has(m.id));
        return [...newMessages, ...prev];
      });
      
      setNextCursor(newCursor);
      setHasMore(newCursor != null);
    } catch (err) {
      console.error('Failed to load chats:', err);
    }
  }, [auth.authenticated, roomId, mapChatToMessage]);

  useEffect(() => {
    if (authLoading) return;
    loadChats(null);
  }, [authLoading, auth.authenticated, loadChats]);

  const handleLoadMore = async () => {
    const container = messagesContainerRef.current;
    if (!hasMore || !nextCursor || isLoadingMore || !container) return;

    setIsLoadingMore(true);
    setShouldScrollToBottom(false);
    const prevScrollTop = container.scrollTop;
    const prevScrollHeight = container.scrollHeight;

    try {
      await loadChats(nextCursor);
      requestAnimationFrame(() => {
        const scrollDiff = container.scrollHeight - prevScrollHeight;
        container.scrollTop = prevScrollTop + scrollDiff;
        setIsLoadingMore(false);
      });
    } catch (err) {
      console.error('Load more failed:', err);
      setIsLoadingMore(false);
    }
  };


  const handleIncomingMessage = useCallback(payload => {
    if (auth.id === 0 || payload.senderId === auth.id) return;
    

    const senderInfo = participants.find(p => p.userId === String(payload.senderId)) || 
      createUserProfile(payload.senderId, payload.senderName, payload.senderAvatarUrl);
    
    const message = {
      id: payload.id || Date.now(),
      userId: String(payload.senderId),
      nickname: payload.senderName,
      content: payload.message,
      timestamp: payload.createdAt,
      isMe: false,
      type: payload.messageType === 'CODE' ? 'code' : 'text',
      language: payload.language?.toLowerCase() || null,
      code: payload.codeContent,
      senderProfile: senderInfo
    };
    
    setMessages(prev => [...prev, message]);
    setShouldScrollToBottom(true);
  }, [auth.id, participants, createUserProfile]);


  const handlePresenceUpdate = useCallback(presence => {
    console.log('[ChatRoom] presence update received:', presence);
    
    if (!presence.joined && !presence.left) {

      const allParticipants = Object.entries(presence).map(([id, str]) => {
        const [login, avatarUrl] = str.split('|');
        return createUserProfile(id, login, avatarUrl);
      });
      setParticipants(allParticipants);
      return;
    }


    setParticipants(prev => {
      let updated = [...prev];
      
      presence.joined?.forEach(user => {
        if (!updated.find(p => p.userId === String(user.id))) {
          updated.push(createUserProfile(user.id, user.login, user.avatarUrl));
        }
      });
      
      presence.left?.forEach(user => {
        updated = updated.filter(p => p.userId !== String(user.id));
      });
      
      return updated;
    });
  }, [createUserProfile]);


  const { sendChat: sendMessage, connected } = useChatStomp(
    roomId,
    handleIncomingMessage,
    handlePresenceUpdate
  );


  const sendMessageWithUpdate = useCallback((message, stompMessage) => {
    setMessages(prev => [...prev, message]);
    setShouldScrollToBottom(true);
    sendMessage(stompMessage);
  }, [sendMessage]);


  const handleSendMessage = useCallback(() => {
    if (!inputMessage.trim() || !connected || auth.id === 0) return;
    
    const message = createMessage({ content: inputMessage.trim() });
    const stompMessage = createStompMessage(message);
    
    sendMessageWithUpdate(message, stompMessage);
    setInputMessage('');
  }, [inputMessage, connected, auth.id, createMessage, createStompMessage, sendMessageWithUpdate]);


  const handleSendCode = useCallback(() => {
    if (!codeInput.trim() || !connected || auth.id === 0) return;
    
    const message = createMessage({ 
      language: selectedLanguage, 
      code: codeInput.trim() 
    }, true);
    const stompMessage = createStompMessage(message, true);
    
    sendMessageWithUpdate(message, stompMessage);
    setCodeInput('');
    setShowCodeModal(false);
  }, [codeInput, connected, auth.id, selectedLanguage, createMessage, createStompMessage, sendMessageWithUpdate]);


  const handleCopyCode = async (code, id) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCodeId(id);
      setTimeout(() => setCopiedCodeId(null), 2000);
    } catch (err) {
      console.error('Failed to copy code:', err);
    }
  };

  const handleKeyDown = useCallback((e) => {
    if (isComposing || e.key !== 'Enter' || e.shiftKey) return;
    e.preventDefault();
    handleSendMessage();
  }, [isComposing, handleSendMessage]);

  const handleCodeKeyPress = useCallback((e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      e.preventDefault();
      handleSendCode();
    }
  }, [handleSendCode]);


  useEffect(() => {
    if (shouldScrollToBottom && !isLoadingMore) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      setShouldScrollToBottom(false);
    }
  }, [messages, shouldScrollToBottom, isLoadingMore]);


  useEffect(() => {
    if (showCodeModal) {
      codeTextareaRef.current?.focus();
    }
  }, [showCodeModal]);


  useEffect(() => {
    if (!loading && messages.length > 0 && isFirstLoad.current) {
      setShouldScrollToBottom(true);
      isFirstLoad.current = false;
    }
  }, [loading, messages.length]);


  const BackgroundEffects = () => (
    <div className={styles.backgroundEffects}>
      <div className={styles.bgCircle1} />
      <div className={styles.bgCircle2} />
      <div className={styles.bgCircle3} />
    </div>
  );


  const LoadingSpinner = () => (
    <div className={styles.loading}>
      <div className={styles.spinner} />
    </div>
  );


  if (error) {
    return (
      <div className={styles.chatRoom}>
        <BackgroundEffects />
        <div className={styles.error}>채팅방 정보를 불러오는 중 오류가 발생했습니다.</div>
      </div>
    );
  }

  return (
    <div className={styles.chatRoom}>
      <BackgroundEffects />

      {/* 헤더는 roomInfo가 있을 때만 렌더링 */}
      {roomInfo && (
        <ChatHeader
          roomInfo={roomInfo}
          participants={initParticipants}
          connected={connected}
          onBack={() => navigate(-1)}
          onDeleteRoom={handleDeleteRoom}
          currentUserId={auth.id}
          onMemberKicked={handleMemberKicked}
          onUpdateRoom={handleUpdateRoom} 
          roomIdNum={roomIdNum}   
        />
      )}

      {/* 로딩 상태 */}
      {(authLoading || loading) && <LoadingSpinner />}

      {/* 메인 채팅 영역 */}
      {roomInfo && (
        <>
          <div className={styles.chatContainer}>
            <div className={styles.messagesContainer} ref={messagesContainerRef}>
              <div className={styles.messagesList}>
                {hasMore && (
                  <button
                    className={styles.loadMoreBtn}
                    onClick={handleLoadMore}
                    disabled={isLoadingMore}
                  >
                    {isLoadingMore ? '로딩 중...' : '이전 메시지 더 보기'}
                  </button>
                )}
                {messages.map(message => (
                  <MessageItem
                    key={message.id}
                    message={message}
                    participants={participants}
                    onCopy={handleCopyCode}
                    copiedCodeId={copiedCodeId}
                  />
                ))}
                <div ref={messagesEndRef} />
              </div>
            </div>

            <Sidebar participants={participants} />
          </div>

          <ChatInput
            inputMessage={inputMessage}
            setInputMessage={setInputMessage}
            handleSendMessage={handleSendMessage}
            handleKeyDown={handleKeyDown}
            isComposing={isComposing}
            setIsComposing={setIsComposing}
            onOpenCodeModal={() => setShowCodeModal(true)}
          />

          <CodeModal
            visible={showCodeModal}
            selectedLanguage={selectedLanguage}
            setSelectedLanguage={setSelectedLanguage}
            codeInput={codeInput}
            setCodeInput={setCodeInput}
            onSendCode={handleSendCode}
            onClose={() => setShowCodeModal(false)}
            onKeyPress={handleCodeKeyPress}
            codeTextareaRef={codeTextareaRef}
            connected={connected}
          />
        </>
      )}
    </div>
  );
};

export default ChatRoom;