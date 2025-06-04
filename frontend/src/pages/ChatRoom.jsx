// src/pages/ChatRoom.jsx

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Users,
  Send,
  Hash,
  Lock,
  Globe,
  Clock,
  Smile,
  Code,
  Copy,
  Check
} from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { okaidia } from 'react-syntax-highlighter/dist/esm/styles/prism';

import { getChatRoomInfo } from '../service/ChatService';
import { useChatStomp } from '../hooks/useChatStomp';
import { useAuth } from '../hooks/useAuth.ts';
import styles from '../css/ChatRoom.module.css';

const ChatRoom = () => {
  const navigate = useNavigate();
  const { roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam);

  const { auth, loading: authLoading, logout } = useAuth();
  // auth = { id: number, login: string|null, avatarUrl: string|null, authenticated: boolean }

  const [roomInfo, setRoomInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isComposing, setIsComposing] = useState(false); 
  const [showCodeModal, setShowCodeModal] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [copiedCodeId, setCopiedCodeId] = useState(null);

  const [onlineMembers] = useState([
    { userId: 'user1', nickname: '김개발', isOnline: true },
    { userId: 'user2', nickname: '이디자인', isOnline: true },
    { userId: 'user3', nickname: '박백엔드', isOnline: false },
    { userId: 'user4', nickname: '최기획', isOnline: true },
    { userId: 'me',    nickname: '나',     isOnline: true }
  ]);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  const codeTextareaRef = useRef(null);

  const supportedLanguages = [
    { value: 'javascript', label: 'JavaScript' },
    { value: 'python',     label: 'Python' },
    { value: 'java',       label: 'Java' },
    { value: 'cpp',        label: 'C++' },
    { value: 'c',          label: 'C' },
    { value: 'csharp',     label: 'C#' },
    { value: 'php',        label: 'PHP' },
    { value: 'ruby',       label: 'Ruby' },
    { value: 'go',         label: 'Go' },
    { value: 'rust',       label: 'Rust' },
    { value: 'typescript', label: 'TypeScript' },
    { value: 'html',       label: 'HTML' },
    { value: 'css',        label: 'CSS' },
    { value: 'sql',        label: 'SQL' },
    { value: 'bash',       label: 'Bash' },
    { value: 'json',       label: 'JSON' },
    { value: 'xml',        label: 'XML' },
    { value: 'yaml',       label: 'YAML' }
  ];

  // 2) 채팅방 정보 가져오기
  useEffect(() => {
    if (authLoading) return;

    if (!auth.authenticated) {
      navigate('/login');
      return;
    }

    if (!roomIdParam || isNaN(roomId)) {
      setError(new Error('올바른 채팅방 ID가 아닙니다.'));
      setLoading(false);
      return;
    }
    async function fetchRoomInfo() {
      try {
        setLoading(true);
        const data = await getChatRoomInfo(roomId);
        setRoomInfo(data);
      } catch (err) {
        console.error('채팅방 정보를 불러오는 중 오류:', err);
        setError(err);
      } finally {
        setLoading(false);
      }
    }
    fetchRoomInfo();
  }, [authLoading, auth, roomIdParam, roomId, navigate]);

  // 3) STOMP 훅 연결 + 메시지 수신 콜백
  const handleIncomingMessage = useCallback((payload) => {

    if (auth.id === 0) {
      console.log('📥 로그인 정보 없음, 메시지 무시');
      return;
    }

    const isMyMessage = payload.senderId === auth.id;
    console.log('📥 isMyMessage:', isMyMessage);

    const newMsg = {
      id: payload.id || Date.now(),
      userId: String(payload.senderId),
      nickname: payload.senderName,
      content: payload.message,
      timestamp: payload.createdAt,
      isMe: isMyMessage,
      type: payload.messageType === 'CODE' ? 'code' : 'text',
      language: payload.language,
      code: payload.codeContent
    };

    if (isMyMessage) {
      return;
    }

    setMessages(prev => [...prev, newMsg]);
  }, [auth.id]);

  const { sendChat: sendMessage, connected } = useChatStomp(roomId, handleIncomingMessage);

  // 4) 텍스트 메시지 전송 함수 (마우스 클릭용)
  const handleSendMessage = () => {
    if (!inputMessage.trim() || !connected || auth.id === 0) return;

    // 로컬에 우선 추가
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

    // 서버에 전송
    const payload = {
      id: outgoing.id,
      roomId,
      senderId: auth.id,
      senderName: auth.login || '나',
      message: outgoing.content,
      messageType: 'TEXT',
      codeContent: null,
      language: null,
      createdAt: outgoing.timestamp
    };

    sendMessage(payload);

    setInputMessage('');
  };

  // 5) 코드 블록 전송 함수
  const handleSendCode = () => {
    if (!codeInput.trim() || !connected || auth.id === 0) return;

    const outgoing = {
      id: Date.now(),
      userId: String(auth.id),
      nickname: auth.login || '나',
      content: `${selectedLanguage} 코드를 공유했습니다.`,
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'code',
      language: selectedLanguage,
      code: codeInput.trim()
    };

    setMessages(prev => [...prev, outgoing]);

    const payload = {
      id: outgoing.id,
      roomId,
      senderId: auth.id,
      senderName: auth.login || '나',
      message: outgoing.content,
      messageType: 'CODE',
      codeContent: codeInput.trim(),
      language: selectedLanguage.toUpperCase(),
      createdAt: outgoing.timestamp
    };

    sendMessage(payload);

    setCodeInput('');
    setShowCodeModal(false);
  };

  // 6) 코드 복사
  const handleCopyCode = async (code, messageId) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCodeId(messageId);
      setTimeout(() => setCopiedCodeId(null), 2000);
    } catch (err) {
      console.error('코드 복사 실패:', err);
    }
  };

  // 7) 엔터 키 전송 (Shift+Enter 줄바꿈, IME 완료 시 중복 방지)
  const handleKeyDown = (e) => {
    // 한글 IME 조합 중일 때는 전송하지 않음
    if (isComposing) return;

    // Enter 키 + Shift 키 미사용 => 전송 로직
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();

      const msg = inputMessage.trim();
      if (!msg || !connected || auth.id === 0) {
        return;
      }

      // 메시지 전송 후 입력창을 비워서, 중복 전송 방지
      setInputMessage('');

      // 로컬 메시지 추가
      const outgoing = {
        id: Date.now(),
        userId: String(auth.id),
        nickname: auth.login || '나',
        content: msg,
        timestamp: new Date().toISOString(),
        isMe: true,
        type: 'text'
      };
      setMessages(prev => [...prev, outgoing]);

      // STOMP로 서버 전송
      const payload = {
        id: outgoing.id,
        roomId,
        senderId: auth.id,
        senderName: auth.login || '나',
        message: outgoing.content,
        messageType: 'TEXT',
        codeContent: null,
        language: null,
        createdAt: outgoing.timestamp
      };
      sendMessage(payload);
    }
  };

  // 8) 코드 모달 내 Ctrl+Enter 시 전송
  const handleCodeKeyPress = (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      e.preventDefault();
      handleSendCode();
    }
  };

  // 9) 스크롤 맨 아래로
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (showCodeModal && codeTextareaRef.current) {
      codeTextareaRef.current.focus();
    }
  }, [showCodeModal]);

  // 10) 시간 포맷
  const formatTime = (isoString) => {
    const date = new Date(isoString);
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));

    if (diffInMinutes < 1) return '방금 전';
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}시간 전`;
    return date.toLocaleDateString();
  };

  const getTypeIcon = (type) =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  const getLanguageColor = (language) => {
    const colors = {
      javascript: '#f7df1e',
      python: '#3776ab',
      java: '#ed8b00',
      cpp: '#00599c',
      c: '#a8b9cc',
      csharp: '#239120',
      php: '#777bb4',
      ruby: '#cc342d',
      go: '#00add8',
      rust: '#ce422b',
      typescript: '#3178c6',
      html: '#e34f26',
      css: '#1572b6',
      sql: '#336791',
      bash: '#4eaa25',
      json: '#292929',
      xml: '#ff6600',
      yaml: '#cb171e'
    };
    return colors[language] || '#6b7280';
  };

  if (authLoading || loading) {
    return <div className={styles.loading}>로딩 중...</div>;
  }
  if (error || !roomInfo) {
    return (
      <div className={styles.error}>
        채팅방 정보를 불러오는 중 오류가 발생했습니다.
      </div>
    );
  }

  return (
    <div className={styles.chatRoom}>
      {/* 배경 효과 */}
      <div className={styles.backgroundEffects}>
        <div className={styles.bgCircle1}></div>
        <div className={styles.bgCircle2}></div>
        <div className={styles.bgCircle3}></div>
      </div>

      {/* 헤더 */}
      <header className={styles.chatHeader}>
        <div className={styles.headerLeft}>
          <button className={styles.backButton} onClick={() => navigate(-1)}>
            <ArrowLeft size={20} />
          </button>
          <div className={styles.roomInfo}>
            <div className={styles.roomIcon}>
              <Hash size={20} />
            </div>
            <div className={styles.roomDetails}>
              <div className={styles.roomNameRow}>
                <h1 className={styles.roomName}>{roomInfo.name}</h1>
                <div className={styles.roomType}>{getTypeIcon(roomInfo.type)}</div>
              </div>
              <p className={styles.roomDescription}>
                {roomInfo.description || '설명 없음'}
              </p>
            </div>
          </div>
        </div>
        <div className={styles.headerRight}>
          <div className={styles.memberCount}>
            <Users size={18} />
            <span>{roomInfo.memberCount}명</span>
          </div>
          <div className={styles.connectionStatus}>
            <div
              className={`${styles.statusDot} ${
                connected ? styles.active : styles.inactive
              }`}
            ></div>
            <span>{connected ? '실시간' : '연결 중...'}</span>
          </div>
        </div>
      </header>

      {/* 메인 채팅 영역 */}
      <div className={styles.chatContainer}>
        {/* 채팅 메시지 영역 */}
        <div className={styles.messagesContainer}>
          <div className={styles.messagesList}>
            {messages.map((message) => (
              <div
                key={message.id}
                className={`${styles.message} ${
                  message.isMe ? styles.messageMe : styles.messageOther
                }`}
              >
                {!message.isMe && (
                  <div className={styles.messageAvatar}>
                    <div className={styles.avatarCircle}>
                      {message.nickname?.[0] || '🤖'}
                    </div>
                  </div>
                )}

                <div className={styles.messageContent}>
                  {!message.isMe && (
                    <div className={styles.messageHeader}>
                      <span className={styles.messageNickname}>
                        {message.nickname}
                      </span>
                      <span className={styles.messageTime}>
                        <Clock size={12} />
                        {formatTime(message.timestamp)}
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
                            <span
                              style={{
                                color: getLanguageColor(message.language)
                              }}
                            >
                              {
                                supportedLanguages.find(
                                  (lang) => lang.value === message.language
                                )?.label || message.language
                              }
                            </span>
                          </div>
                          <button
                            className={styles.copyButton}
                            onClick={() =>
                              handleCopyCode(message.code, message.id)
                            }
                          >
                            {copiedCodeId === message.id ? (
                              <Check size={14} />
                            ) : (
                              <Copy size={14} />
                            )}
                            {copiedCodeId === message.id ? '복사됨' : '복사'}
                          </button>
                        </div>
                        <SyntaxHighlighter
                          language={message.language}
                          style={okaidia}
                          showLineNumbers={true}
                          wrapLongLines={true}
                          className={styles.codeBlock}
                        >
                          {message.code}
                        </SyntaxHighlighter>
                        {message.content && (
                          <p className={styles.codeDescription}>
                            {message.content}
                          </p>
                        )}
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
            ))}
            <div ref={messagesEndRef} />
          </div>
        </div>

        {/* 사이드바 – 온라인 멤버 */}
        <div className={styles.sidebar}>
          <div className={styles.sidebarHeader}>
            <h3>온라인 멤버</h3>
            <span className={styles.onlineCount}>
              {onlineMembers.filter((m) => m.isOnline).length}명
            </span>
          </div>
          <div className={styles.membersList}>
            {onlineMembers.map((member) => (
              <div key={member.userId} className={styles.memberItem}>
                <div className={styles.memberAvatar}>
                  <div className={styles.avatarCircle}>
                    {member.nickname[0]}
                  </div>
                  <div
                    className={`${styles.statusIndicator} ${
                      member.isOnline ? styles.online : styles.offline
                    }`}
                  ></div>
                </div>
                <div className={styles.memberInfo}>
                  <span className={styles.memberNickname}>
                    {member.nickname}
                  </span>
                  <span className={styles.memberStatus}>
                    {member.isOnline ? '온라인' : '오프라인'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 입력 영역 */}
      <div className={styles.inputContainer}>
        <div className={styles.inputBox}>
          <div className={styles.inputWrapper}>
            <textarea
              ref={inputRef}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={handleKeyDown}
              onCompositionStart={() => setIsComposing(true)}
              onCompositionEnd={() => setIsComposing(false)}
              placeholder={connected ? '메시지를 입력하세요...' : '연결 중...'}
              className={styles.messageInput}
              rows="1"
              disabled={!connected}
            />
            <button className={styles.emojiButton} disabled={!connected}>
              <Smile size={20} />
            </button>
            <button
              className={styles.codeButton}
              onClick={() => setShowCodeModal(true)}
              title="코드 블록 추가"
              disabled={!connected}
            >
              <Code size={20} />
            </button>
          </div>
          <button
            className={styles.sendButton}
            onClick={handleSendMessage}
            disabled={!inputMessage.trim() || !connected}
          >
            <Send size={18} />
          </button>
        </div>
      </div>

      {/* 코드 입력 모달 */}
      {showCodeModal && (
        <div className={styles.codeModal}>
          <div className={styles.codeModalContent}>
            <div className={styles.codeModalHeader}>
              <h3>코드 블록 추가</h3>
              <button
                className={styles.closeButton}
                onClick={() => setShowCodeModal(false)}
              >
                ×
              </button>
            </div>
            <div className={styles.codeModalBody}>
              <div className={styles.languageSelector}>
                <label htmlFor="language-select">언어 선택:</label>
                <select
                  id="language-select"
                  value={selectedLanguage}
                  onChange={(e) => setSelectedLanguage(e.target.value)}
                  className={styles.languageSelect}
                >
                  {supportedLanguages.map((lang) => (
                    <option key={lang.value} value={lang.value}>
                      {lang.label}
                    </option>
                  ))}
                </select>
              </div>
              <textarea
                ref={codeTextareaRef}
                value={codeInput}
                onChange={(e) => setCodeInput(e.target.value)}
                onKeyDown={handleCodeKeyPress}
                placeholder="코드를 입력하세요..."
                className={styles.codeTextarea}
                disabled={!connected}
              />
              <div className={styles.codeModalFooter}>
                <p className={styles.keyboardHint}>Ctrl + Enter로 전송</p>
                <div className={styles.codeModalButtons}>
                  <button
                    className={styles.cancelButton}
                    onClick={() => setShowCodeModal(false)}
                  >
                    취소
                  </button>
                  <button
                    className={styles.sendCodeButton}
                    onClick={handleSendCode}
                    disabled={!codeInput.trim() || !connected}
                  >
                    전송
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatRoom;
