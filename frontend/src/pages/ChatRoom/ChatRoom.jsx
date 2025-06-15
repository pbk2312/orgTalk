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

import { getChatRoomInfo, getChatsByCursor } from '../../service/ChatService.jsx';
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

  // Load room info
  useEffect(() => {
    if (authLoading) return;
    (async () => {
      try {
        setLoading(true);
        const data = await getChatRoomInfo(roomId);
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
        code: p.codeContent
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
      setShouldScrollToBottom(true);
    }
  }, [auth.id]);

  // Presence updates
  const handlePresenceUpdate = useCallback(presence => {
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
      type: 'text'
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

  // Time format
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
            {messages.map(message => {
              const sender = participants.find(p => p.userId === message.userId);
              return (
                <div
                  key={message.id}
                  className={`${styles.message} ${message.isMe ? styles.messageMe : styles.messageOther}`}
                >
                  {!message.isMe && (
                    <div className={styles.messageAvatar}>
                      {sender?.avatarUrl ? (
                        <img src={sender.avatarUrl} alt={sender.login} className={styles.avatarImage} />
                      ) : (
                        <div className={styles.avatarCircle}>
                          {message.nickname?.[0] || '🤖'}
                        </div>
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
