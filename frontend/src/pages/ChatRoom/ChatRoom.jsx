import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Clock,
  Code,
  Copy,
  Check
} from 'lucide-react';

import ChatHeader from './ChatHeader';
import Sidebar from './Sidebar';
import ChatInput from './ChatInput';
import CodeModal from './CodeModal';

import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { okaidia } from 'react-syntax-highlighter/dist/esm/styles/prism';

import { getChatRoomInfo, getChatsByRoomId } from '../../service/ChatService.jsx';
import { useChatStomp } from '../../hooks/useChatStomp.js';
import { useAuth } from '../../hooks/useAuth.ts';
import styles from '../../css/ChatRoom.module.css';
import { supportedLanguages, getLanguageColor } from '../../constants/chatConstants';

const ChatRoom = () => {
  const navigate = useNavigate();
  const { roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam);

  const { auth, loading: authLoading } = useAuth();

  const [roomInfo, setRoomInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [messages, setMessages] = useState([]);
  const [participants, setParticipants] = useState([]);

  const [inputMessage, setInputMessage] = useState('');
  const [isComposing, setIsComposing] = useState(false);
  const [showCodeModal, setShowCodeModal] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [copiedCodeId, setCopiedCodeId] = useState(null);

  const messagesEndRef = useRef(null);
  const codeTextareaRef = useRef(null);

  // 1) 방 정보 로드
useEffect(() => {
  if (authLoading) return;
  (async () => {
    try {
      setLoading(true);
      const data = await getChatRoomInfo(roomId);
      setRoomInfo(data);

      // 서버에서 이미 접속해 있는 유저 정보가 data.participants에 온다고 가정하면
      if (data.participants) {
        const initial = data.participants.map(u => ({
          userId: String(u.id),
          login: u.login,
          avatarUrl: u.avatarUrl
        }));
        setParticipants(initial);
      }
    } catch (err) {
      console.error(err);
      setError(err);
    } finally {
      setLoading(false);
    }
  })();
}, [authLoading, auth, roomIdParam, roomId, navigate]);


  // 2) 이전 채팅 기록 로드
  useEffect(() => {
    if (authLoading || !auth.authenticated || isNaN(roomId)) return;
    (async () => {
      try {
        const chatResponses = await getChatsByRoomId(roomId);
        const mapped = chatResponses.map(payload => ({
          id: payload.id,
          userId: String(payload.senderId),
          nickname: payload.senderName,
          content: payload.message,
          timestamp: payload.createdAt,
          isMe: payload.senderId === auth.id,
          type: payload.messageType === 'CODE' ? 'code' : 'text',
          language: payload.language?.toLowerCase() || null,
          code: payload.codeContent
        }));
        setMessages(mapped);
      } catch (err) {
        console.error('이전 채팅 기록을 불러오는 중 오류:', err);
      }
    })();
  }, [authLoading, auth, roomId]);

  // 3) 실시간 메시지 수신
  const handleIncomingMessage = useCallback(
    (payload) => {
      if (auth.id === 0) return;
      const isMy = payload.senderId === auth.id;
      const msg = {
        id: payload.id || Date.now(),
        userId: String(payload.senderId),
        nickname: payload.senderName,
        content: payload.message,
        timestamp: payload.createdAt,
        isMe: isMy,
        type: payload.messageType === 'CODE' ? 'code' : 'text',
        language: payload.language?.toLowerCase() || null,
        code: payload.codeContent
      };
      if (!isMy) {
        setMessages(prev => [...prev, msg]);
      }
    },
    [auth.id]
  );

  // 4) 실시간 입장/퇴장 처리
  const handlePresenceUpdate = useCallback((presence) => {
  console.log('🔥 Presence update received:', presence);

  // joined/left 프로퍼티가 없으면, 전체 목록이라고 보고 초기화
  if (!presence.joined && !presence.left) {
    const all = Object.entries(presence).map(([id, str]) => {
      const [login, avatarUrl] = str.split('|');
      return { userId: id, login, avatarUrl };
    });
    console.log('🔄 Initial participants from full list:', all);
    setParticipants(all);
    return;
  }

  // (기존 joined/left 처리 로직)
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
    console.log('🔄 Updated participants after join/leave:', updated);
    return updated;
  });
}, [setParticipants]);


  // 5) STOMP 훅 연결 (메시지 + Presence)
  const { sendChat: sendMessage, connected } = useChatStomp(
    roomId,
    handleIncomingMessage,
    handlePresenceUpdate
  );

  // 6) 텍스트 메시지 전송
  const handleSendMessage = () => {
    if (!inputMessage.trim() || !connected || auth.id === 0) return;
    const outgoing = {
      id: Date.now(),
      userId: String(auth.id),
      nickname: auth.login || '나',
      content: inputMessage.trim(),
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'text'
    };
    setMessages(prev => [...prev, outgoing]);
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

  // 7) 코드 메시지 전송
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

  // 8) 복사·키 핸들러
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

  // 9) 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 10) 모달 포커스
  useEffect(() => {
    if (showCodeModal) codeTextareaRef.current?.focus();
  }, [showCodeModal]);

  // 11) 시간 포맷
  const formatTime = iso => {
    const date = new Date(iso);
    const diff = Math.floor((Date.now() - date) / 60000);
    if (diff < 1) return '방금 전';
    if (diff < 60) return `${diff}분 전`;
    if (diff < 1440) return `${Math.floor(diff / 60)}시간 전`;
    return date.toLocaleDateString();
  };

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
      />

      <div className={styles.chatContainer}>
        <div className={styles.messagesContainer}>
          <div className={styles.messagesList}>
            {messages.map(message => {
              const senderInfo = participants.find(p => p.userId === message.userId);
              return (
                <div
                  key={message.id}
                  className={`${styles.message} ${message.isMe ? styles.messageMe : styles.messageOther}`}
                >
                  {!message.isMe && (
                    <div className={styles.messageAvatar}>
                      {senderInfo?.avatarUrl ? (
                        <img src={senderInfo.avatarUrl} alt={senderInfo.login} className={styles.avatarImage} />
                      ) : (
                        <div className={styles.avatarCircle}>{message.nickname?.[0] || '🤖'}</div>
                      )}
                    </div>
                  )}
                  <div className={styles.messageContent}>
                    {!message.isMe && (
                      <div className={styles.messageHeader}>
                        <span className={styles.messageNickname}>{message.nickname}</span>
                        <span className={styles.messageTime}>
                          <Clock size={12} />{formatTime(message.timestamp)}
                        </span>
                      </div>
                    )}
                    <div className={styles.messageBubble}>
                      {message.type === 'text' ? (
                        <p className={styles.messageText}>{message.content}</p>
                      ) : (
                        <div className={styles.codeMessage}>
                          <div className={styles.codeHeader}>
                            <div className={styles.codeLanguage}>
                              <Code size={14} />
                              <span style={{ color: getLanguageColor(message.language) }}>
                                {supportedLanguages.find(l => l.value === message.language)?.label || message.language}
                              </span>
                            </div>
                            <button
                              className={styles.copyButton}
                              onClick={() => handleCopyCode(message.code, message.id)}
                            >
                              {copiedCodeId === message.id ? <Check size={14} /> : <Copy size={14} />}
                              {copiedCodeId === message.id ? '복사됨' : '복사'}
                            </button>
                          </div>
                          <SyntaxHighlighter
                            language={message.language}
                            style={okaidia}
                            showLineNumbers
                            wrapLongLines
                            className={styles.codeBlock}
                          >
                            {message.code}
                          </SyntaxHighlighter>
                        </div>
                      )}
                      {message.isMe && (
                        <div className={styles.messageTimeMe}>
                          <Clock size={12} />
                          {formatTime(message.timestamp)}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
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

