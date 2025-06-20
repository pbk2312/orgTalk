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

  // Cursor-based pagination
  const [nextCursor, setNextCursor] = useState(null);
  const [hasMore, setHasMore] = useState(true);

  // Input / modal state
  const [inputMessage, setInputMessage] = useState('');
  const [isComposing, setIsComposing] = useState(false);
  const [showCodeModal, setShowCodeModal] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [copiedCodeId, setCopiedCodeId] = useState(null);

  // Scroll management
  const [shouldScrollToBottom, setShouldScrollToBottom] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const codeTextareaRef = useRef(null);

  const isFirstLoad = useRef(true);

  const handleUpdateRoom = async (updateData) => {
    console.log('📌 handleUpdateRoom called for roomId =', roomIdNum, 'updateData =', updateData);
    try {
      // 반드시 roomId 키에 URL param을 담아 보냅니다
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
    } catch {
      // 이미 alert 처리됨
    }
  };


  // Load room info
  useEffect(() => {
    if (authLoading) return;
    (async () => {
      try {
        setLoading(true);
        const data = await getChatRoomInfo(roomId);
        console.log('[ChatRoom] fetched roomInfo:', data);
        setRoomInfo(data);
        if (data.participants) {
          setParticipants(
            data.participants.map(u => ({ userId: String(u.id), login: u.login, avatarUrl: u.avatarUrl }))
          );
        }
      } catch (err) {
        console.error(err);
        setError(err);
      } finally {
        setLoading(false);
      }
    })();
  }, [authLoading, roomId]);

  // Load chats (cursor-based)
  const loadChats = useCallback(async (cursor = null) => {
  try {
    const { chats, nextCursor: newCursor } = await getChatsByCursor(roomId, cursor);
    const mapped = chats.map(p => ({
      id: p.id,
      userId: String(p.senderId),
      nickname: p.senderName,
      content: p.message,
      timestamp: p.createdAt,
      isMe: p.senderId === auth.id,
      type: p.messageType === 'CODE' ? 'code' : 'text',
      language: p.language?.toLowerCase() || null,
      code: p.codeContent,
      // 발신자 프로필 정보 추가
      senderProfile: {
        userId: String(p.senderId),
        login: p.senderName,
        avatarUrl: p.senderAvatarUrl || null // 서버에서 아바타 URL도 함께 제공되어야 함
      }
    }));

    setMessages(prev => {
      if (!cursor) return mapped;
      const existing = new Set(prev.map(m => m.id));
      const newOnes = mapped.filter(m => !existing.has(m.id));
      return [...newOnes, ...prev];
    });
    setNextCursor(newCursor);
    setHasMore(newCursor !== null && newCursor !== undefined);
  } catch (err) {
    console.error('Failed to load chats:', err);
  }
}, [auth.id, roomId]);


  useEffect(() => {
    if (authLoading || !auth.authenticated || isNaN(roomId)) return;
    loadChats(null);
  }, [authLoading, auth, roomId, loadChats]);

  // Load more (preserve scroll)
  const handleLoadMore = async () => {
    const container = messagesContainerRef.current;
    if (!hasMore || !nextCursor || isLoadingMore || !container) return;

    setIsLoadingMore(true);
    setShouldScrollToBottom(false);
    const prevTop = container.scrollTop;
    const prevHeight = container.scrollHeight;

    try {
      await loadChats(nextCursor);
      requestAnimationFrame(() => {
        const diff = container.scrollHeight - prevHeight;
        container.scrollTop = prevTop + diff;
        setIsLoadingMore(false);
      });
    } catch (err) {
      console.error('Load more failed:', err);
      setIsLoadingMore(false);
    }
  };

  // Real-time message handler
  const handleIncomingMessage = useCallback(payload => {
  if (auth.id === 0) return;
  const isMy = payload.senderId === auth.id;
  
  // 현재 participants에서 발신자 정보 찾기
  const senderInfo = participants.find(p => p.userId === String(payload.senderId)) || {
    userId: String(payload.senderId),
    login: payload.senderName,
    avatarUrl: payload.senderAvatarUrl || null
  };
  
  const msg = {
    id: payload.id || Date.now(),
    userId: String(payload.senderId),
    nickname: payload.senderName,
    content: payload.message,
    timestamp: payload.createdAt,
    isMe: isMy,
    type: payload.messageType === 'CODE' ? 'code' : 'text',
    language: payload.language?.toLowerCase() || null,
    code: payload.codeContent,
    // 발신자 프로필 정보 추가
    senderProfile: senderInfo
  };
  
  if (!isMy) {
    setMessages(prev => [...prev, msg]);
    setShouldScrollToBottom(true);
  }
}, [auth.id, participants]);


  // Presence updates
  const handlePresenceUpdate = useCallback(presence => {
    console.log('[ChatRoom] presence update received:', presence);
    if (!presence.joined && !presence.left) {
      const all = Object.entries(presence).map(([id, str]) => {
        const [login, avatarUrl] = str.split('|');
        return { userId: id, login, avatarUrl };
      });
      setParticipants(all);
      return;
    }
    setParticipants(prev => {
      let updated = [...prev];
      presence.joined?.forEach(u => {
        if (!updated.find(p => p.userId === String(u.id))) {
          updated.push({ userId: String(u.id), login: u.login, avatarUrl: u.avatarUrl });
        }
      });
      presence.left?.forEach(u => {
        updated = updated.filter(p => p.userId !== String(u.id));
      });
      return updated;
    });
  }, []);

  // STOMP hook
  const { sendChat: sendMessage, connected } = useChatStomp(
    roomId,
    handleIncomingMessage,
    handlePresenceUpdate
  );

  // Send text
  const handleSendMessage = () => {
    if (!inputMessage.trim() || !connected || auth.id === 0) return;
    const outgoing = {
      id: Date.now(),
      userId: String(auth.id),
      nickname: auth.login || '나',
      content: inputMessage.trim(),
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'text',
      senderProfile: {
      userId: String(auth.id),
      login: auth.login || '나',
      avatarUrl: auth.avatarUrl || null
    }
    };
    setMessages(prev => [...prev, outgoing]);
    setShouldScrollToBottom(true);
    sendMessage({
      ...outgoing,
      roomId,
      message: outgoing.content,
      messageType: 'TEXT',
      codeContent: null,
      language: null,
      senderId: auth.id,
      senderName: auth.login || '나',
      createdAt: outgoing.timestamp
    });
    setInputMessage('');
  };

  // Send code
  const handleSendCode = () => {
    if (!codeInput.trim() || !connected || auth.id === 0) return;
    const outgoing = {
      id: Date.now(),
      userId: String(auth.id),
      nickname: auth.login || '나',
      content: '',
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'code',
      language: selectedLanguage,
      code: codeInput.trim()
    };
    setMessages(prev => [...prev, outgoing]);
    setShouldScrollToBottom(true);
    sendMessage({
      ...outgoing,
      roomId,
      message: '',
      messageType: 'CODE',
      codeContent: codeInput.trim(),
      language: selectedLanguage.toUpperCase(),
      senderId: auth.id,
      senderName: auth.login || '나',
      createdAt: outgoing.timestamp
    });
    setCodeInput('');
    setShowCodeModal(false);
  };

  // Copy & key handlers
  const handleCopyCode = async (code, id) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCodeId(id);
      setTimeout(() => setCopiedCodeId(null), 2000);
    } catch {}
  };
  const handleKeyDown = e => {
    if (isComposing) return;
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };
  const handleCodeKeyPress = e => {
    if (e.key === 'Enter' && e.ctrlKey) {
      e.preventDefault();
      handleSendCode();
    }
  };

  // Auto-scroll
  useEffect(() => {
    if (shouldScrollToBottom && !isLoadingMore) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      setShouldScrollToBottom(false);
    }
  }, [messages, shouldScrollToBottom, isLoadingMore]);

  // Modal focus
  useEffect(() => {
    if (showCodeModal) codeTextareaRef.current?.focus();
  }, [showCodeModal]);

  // Initial scroll on first load
  useEffect(() => {
    if (!loading && messages.length > 0 && isFirstLoad.current) {
      setShouldScrollToBottom(true);
      isFirstLoad.current = false;
    }
  }, [loading, messages.length]);

  if (authLoading || loading) {
    return <div className={styles.loading}>로딩 중...</div>;
  }
  if (error || !roomInfo) {
    return <div className={styles.error}>채팅방 정보를 불러오는 중 오류가 발생했습니다.</div>;
  }

  return (
    <div className={styles.chatRoom}>
      <div className={styles.backgroundEffects}>
        <div className={styles.bgCircle1} />
        <div className={styles.bgCircle2} />
        <div className={styles.bgCircle3} />
      </div>

      <ChatHeader
        roomInfo={roomInfo}
        participants={participants}
        connected={connected}
        onBack={() => navigate(-1)}
        onDeleteRoom={handleDeleteRoom}  
        onUpdateRoom={handleUpdateRoom} 
      />

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
        connected={connected}
      />
    </div>
  );
};

export default ChatRoom;