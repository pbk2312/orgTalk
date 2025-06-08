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

import { getChatRoomInfo, getChatsByRoomId } from '../service/ChatService';
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


  const [participants, setParticipants] = useState([]);

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

  // 1) 채팅방 정보 가져오기
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

  // 2) roomInfo.members가 바뀌면 participants 상태 업데이트
  useEffect(() => {
    if (!roomInfo) return;

    // roomInfo.members는 Set<ChatMemberResponse> 형태이므로, 배열로 변환
    // ChatMemberResponse: { id: Long, login: String, avatarUrl: String }
    const memberArray = Array.from(roomInfo.members).map((m) => ({
      userId: String(m.id),
      login: m.login,
      avatarUrl: m.avatarUrl
    }));

    setParticipants(memberArray);
  }, [roomInfo]);

  // 3) 페이지 로드/새로고침 시 이전 채팅 기록 가져오기
  useEffect(() => {
    if (authLoading) return;
    if (!auth.authenticated) return;
    if (!roomIdParam || isNaN(roomId)) return;

    async function fetchChatHistory() {
      try {
        const chatResponses = await getChatsByRoomId(roomId);

        // API로 받은 ChatResponse[]를 화면 메시지 형태로 매핑
        const mapped = chatResponses.map((payload) => ({
          id: payload.id,
          userId: String(payload.senderId),
          nickname: payload.senderName,
          content: payload.message,            // 기존에 서버에 저장된 text 메시지
          timestamp: payload.createdAt,
          isMe: payload.senderId === auth.id,
          type: payload.messageType === 'CODE' ? 'code' : 'text',
          language: payload.language?.toLowerCase() || null,
          code: payload.codeContent
        }));

        setMessages(mapped);
      } catch (err) {
        console.error('이전 채팅 기록을 불러오는 중 오류:', err);
        // 오류 시 간단히 콘솔에 남기고, 에러 상태는 따로 처리하지 않음
      }
    }

    fetchChatHistory();
  }, [authLoading, auth, roomIdParam, roomId]);

  // 4) STOMP 훅 연결 + 메시지 수신 콜백
  const handleIncomingMessage = useCallback(
    (payload) => {
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
        content: payload.message,            // 서버에서 받은 message (text or code일 때 빈 문자열)
        timestamp: payload.createdAt,
        isMe: isMyMessage,
        type: payload.messageType === 'CODE' ? 'code' : 'text',
        language: payload.language?.toLowerCase() || null,
        code: payload.codeContent
      };

      // 내가 보낸 메시지는 이미 로컬에 추가했으므로, 본인 메시지는 무시
      if (isMyMessage) return;

      setMessages((prev) => [...prev, newMsg]);
    },
    [auth.id]
  );

  const { sendChat: sendMessage, connected } = useChatStomp(
    roomId,
    handleIncomingMessage
  );

  // 5) 텍스트 메시지 전송 함수 (마우스 클릭용)
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

    setMessages((prev) => [...prev, outgoing]);

    // 서버에 전송
    const payload = {
      id: outgoing.id,
      roomId,
      senderId: auth.id,
      senderName: auth.login || '나',
      message: outgoing.content,   // text 메시지
      messageType: 'TEXT',
      codeContent: null,
      language: null,
      createdAt: outgoing.timestamp
    };

    sendMessage(payload);
    setInputMessage('');
  };

  // 6) 코드 블록 전송 함수
  const handleSendCode = () => {
    if (!codeInput.trim() || !connected || auth.id === 0) return;

    // `content`를 빈 문자열로 설정해서 "코드를 공유했습니다." 문구를 제거
    const outgoing = {
      id: Date.now(),
      userId: String(auth.id),
      nickname: auth.login || '나',
      content: '',                    // ↩ 빈 문자열
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'code',
      language: selectedLanguage,
      code: codeInput.trim()
    };

    setMessages((prev) => [...prev, outgoing]);

    const payload = {
      id: outgoing.id,
      roomId,
      senderId: auth.id,
      senderName: auth.login || '나',
      message: '',                    // ↩ 빈 문자열로 서버에 전송
      messageType: 'CODE',
      codeContent: codeInput.trim(),
      language: selectedLanguage.toUpperCase(),
      createdAt: outgoing.timestamp
    };

    sendMessage(payload);
    setCodeInput('');
    setShowCodeModal(false);
  };

  // 7) 코드 복사
  const handleCopyCode = async (code, messageId) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCodeId(messageId);
      setTimeout(() => setCopiedCodeId(null), 2000);
    } catch (err) {
      console.error('코드 복사 실패:', err);
    }
  };

  // 8) 엔터 키 전송 (Shift+Enter 줄바꿈, IME 완료 시 중복 방지)
  const handleKeyDown = (e) => {
    if (isComposing) return;

    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();

      const msg = inputMessage.trim();
      if (!msg || !connected || auth.id === 0) {
        return;
      }

      setInputMessage('');

      const outgoing = {
        id: Date.now(),
        userId: String(auth.id),
        nickname: auth.login || '나',
        content: msg,
        timestamp: new Date().toISOString(),
        isMe: true,
        type: 'text'
      };
      setMessages((prev) => [...prev, outgoing]);

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

  // 9) 코드 모달 내 Ctrl+Enter 시 전송
  const handleCodeKeyPress = (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      e.preventDefault();
      handleSendCode();
    }
  };

  // 10) 스크롤 맨 아래로
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

  // 11) 시간 포맷
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
                <div className={styles.roomType}>
                  {getTypeIcon(roomInfo.type)}
                </div>
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
            {messages.map((message) => {
              // 메시지 보낸 사람의 프로필 정보(avatarUrl, login)를 participants 배열에서 찾아둠
              const senderInfo = participants.find(
                (p) => p.userId === message.userId
              );

              return (
                <div
                  key={message.id}
                  className={`${styles.message} ${
                    message.isMe ? styles.messageMe : styles.messageOther
                  }`}
                >
                  {/* → 본인 메시지(isMe)가 아닐 때 avatar 표시 */}
                  {!message.isMe && (
                    <div className={styles.messageAvatar}>
                      {senderInfo && senderInfo.avatarUrl ? (
                        <img
                          src={senderInfo.avatarUrl}
                          alt={senderInfo.login}
                          className={styles.avatarImage}
                        />
                      ) : (
                        <div className={styles.avatarCircle}>
                          {message.nickname?.[0] || '🤖'}
                        </div>
                      )}
                    </div>
                  )}

                  <div className={styles.messageContent}>
                    {/* 본인이 아닌 메시지일 때만 닉네임과 시간 헤더 보여줌 */}
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
                        <p className={styles.messageText}>
                          {message.content}
                        </p>
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
                              {copiedCodeId === message.id
                                ? '복사됨'
                                : '복사'}
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
                          {/* 아래 코드는 삭제했습니다. message.content(“코드를 공유했습니다.”)를 더 이상 출력하지 않습니다. */}
                        </div>
                      )}

                      {/* 본인 메시지에만 시간 표시 */}
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

        {/* 사이드바 – 참여 중인 멤버 (roomInfo.members 기반) */}
        <div className={styles.sidebar}>
          <div className={styles.sidebarHeader}>
            <h3>참여 중인 멤버</h3>
            <span className={styles.onlineCount}>
              {participants.length}명
            </span>
          </div>
          <div className={styles.membersList}>
            {participants.map((member) => (
              <div key={member.userId} className={styles.memberItem}>
                <div className={styles.memberAvatar}>
                  {/* avatarUrl이 있으면 이미지, 없으면 첫 글자 */}
                  {member.avatarUrl ? (
                    <img
                      src={member.avatarUrl}
                      alt={member.login}
                      className={styles.avatarImage}
                    />
                  ) : (
                    <div className={styles.avatarCircle}>
                      {member.login?.[0] || '🤖'}
                    </div>
                  )}
                </div>
                <div className={styles.memberInfo}>
                  <span className={styles.memberNickname}>
                    {member.login}
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
              placeholder={
                connected ? '메시지를 입력하세요...' : '연결 중...'
              }
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
