import React, { useState, useEffect, useRef } from 'react';
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





import { getChatRoomInfo } from '../service/ChatService'; // API 호출 함수
import styles from '../css/ChatRoom.module.css';

const ChatRoom = () => {

  const navigate = useNavigate();

  // ── 1) URL 파라미터에서 roomId 가져오기 ─────────────────────
  const {roomId: roomIdParam } = useParams();
  const roomId = Number(roomIdParam);                // 숫자로 변환

  // ── 2) 채팅방 정보: API로부터 받아올 예정 ──────────────────
  const [roomInfo, setRoomInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // ── 3) 채팅 메시지 (예시용 하드코딩 데이터) ─────────────────
  const [messages, setMessages] = useState([
    {
      id: 1,
      userId: 'user1',
      nickname: '김개발',
      content: '안녕하세요! 새로운 프로젝트에 대해 이야기해볼까요?',
      timestamp: new Date(Date.now() - 10 * 60000).toISOString(),
      isMe: false,
      type: 'text'
    },
    {
      id: 2,
      userId: 'user2',
      nickname: '이디자인',
      content: 'React 컴포넌트 예시입니다.',
      timestamp: new Date(Date.now() - 8 * 60000).toISOString(),
      isMe: false,
      type: 'code',
      language: 'javascript',
      code: `function Welcome(props) {
  return <h1>Hello, {props.name}!</h1>;
}

const element = <Welcome name="Sara" />;`
    },
    {
      id: 3,
      userId: 'me',
      nickname: '나',
      content: '좋은 예시네요! Python으로도 비슷하게 할 수 있죠.',
      timestamp: new Date(Date.now() - 5 * 60000).toISOString(),
      isMe: true,
      type: 'code',
      language: 'python',
      code: `def greet(name):
    return f"Hello, {name}!"

message = greet("Sara")
print(message)`
    },
    {
      id: 4,
      userId: 'user1',
      nickname: '김개발',
      content: '훌륭한 선택이군요! 상태 관리는 어떻게 할 계획인가요?',
      timestamp: new Date(Date.now() - 3 * 60000).toISOString(),
      isMe: false,
      type: 'text'
    }
  ]);

  // ── 4) 입력 상태 ─────────────────────────────────────────
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [showCodeModal, setShowCodeModal] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('javascript');
  const [copiedCodeId, setCopiedCodeId] = useState(null);

  // ── 5) 온라인 멤버 목록 (예시용 하드코딩 데이터) ───────────
  const [onlineMembers] = useState([
    { userId: 'user1', nickname: '김개발', isOnline: true },
    { userId: 'user2', nickname: '이디자인', isOnline: true },
    { userId: 'user3', nickname: '박백엔드', isOnline: false },
    { userId: 'user4', nickname: '최기획', isOnline: true },
    { userId: 'me', nickname: '나', isOnline: true }
  ]);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  const codeTextareaRef = useRef(null);

  // 지원하는 프로그래밍 언어 목록
  const supportedLanguages = [
    { value: 'javascript', label: 'JavaScript' },
    { value: 'python', label: 'Python' },
    { value: 'java', label: 'Java' },
    { value: 'cpp', label: 'C++' },
    { value: 'c', label: 'C' },
    { value: 'csharp', label: 'C#' },
    { value: 'php', label: 'PHP' },
    { value: 'ruby', label: 'Ruby' },
    { value: 'go', label: 'Go' },
    { value: 'rust', label: 'Rust' },
    { value: 'typescript', label: 'TypeScript' },
    { value: 'html', label: 'HTML' },
    { value: 'css', label: 'CSS' },
    { value: 'sql', label: 'SQL' },
    { value: 'bash', label: 'Bash' },
    { value: 'json', label: 'JSON' },
    { value: 'xml', label: 'XML' },
    { value: 'yaml', label: 'YAML' }
  ];

  // ── 6) API 호출: 채팅방 정보 가져오기 ──────────────────────
  useEffect(() => {
    // roomIdParam이 없거나 NaN이면 호출 중단
    if (!roomIdParam || isNaN(roomId)) {
      setError(new Error('올바른 채팅방 ID가 아닙니다.'));
      setLoading(false);
      return;
    }

    async function fetchRoomInfo() {
      try {
        setLoading(true);
        const data = await getChatRoomInfo(roomId);
        // data = { name, description, type, memberCount }
        setRoomInfo(data);
      } catch (err) {
        console.error('채팅방 정보를 불러오는 중 오류:', err);
        setError(err);
      } finally {
        setLoading(false);
      }
    }

    fetchRoomInfo();
  }, [roomIdParam, roomId]);

  // ── 7) 메시지 전송 함수 ───────────────────────────────────
  const handleSendMessage = () => {
    if (!inputMessage.trim()) return;

    const newMessage = {
      id: Date.now(),
      userId: 'me',
      nickname: '나',
      content: inputMessage.trim(),
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'text'
    };

    setMessages(prev => [...prev, newMessage]);
    setInputMessage('');

    // 가상의 응답 (실제로는 WebSocket 또는 별도 API로 받을 예정)
    setTimeout(() => {
      const responses = [
        '좋은 의견이네요!',
        '동의합니다.',
        '한번 시도해보죠!',
        '그렇게 진행하면 될 것 같아요.',
        '더 자세히 설명해주실 수 있나요?'
      ];
      
      const randomResponse = responses[Math.floor(Math.random() * responses.length)];
      const responseMessage = {
        id: Date.now() + 1,
        userId: 'user1',
        nickname: '김개발',
        content: randomResponse,
        timestamp: new Date().toISOString(),
        isMe: false,
        type: 'text'
      };
      
      setMessages(prev => [...prev, responseMessage]);
    }, 1000 + Math.random() * 2000);
  };

  // ── 8) 코드 전송 함수 ─────────────────────────────────────
  const handleSendCode = () => {
    if (!codeInput.trim()) return;

    const newMessage = {
      id: Date.now(),
      userId: 'me',
      nickname: '나',
      content: `${selectedLanguage} 코드를 공유했습니다.`,
      timestamp: new Date().toISOString(),
      isMe: true,
      type: 'code',
      language: selectedLanguage,
      code: codeInput.trim()
    };

    setMessages(prev => [...prev, newMessage]);
    setCodeInput('');
    setShowCodeModal(false);
  };

  // ── 9) 코드 복사 함수 ─────────────────────────────────────
  const handleCopyCode = async (code, messageId) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCodeId(messageId);
      setTimeout(() => setCopiedCodeId(null), 2000);
    } catch (err) {
      console.error('코드 복사 실패:', err);
    }
  };

  // 엔터 키 입력 시 전송
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // 코드 모달에서 엔터 키 처리
  const handleCodeKeyPress = (e) => {
    if (e.key === 'Enter' && e.ctrlKey) {
      e.preventDefault();
      handleSendCode();
    }
  };

  // 메시지 스크롤 맨 아래로
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 코드 모달이 열릴 때 포커스
  useEffect(() => {
    if (showCodeModal && codeTextareaRef.current) {
      codeTextareaRef.current.focus();
    }
  }, [showCodeModal]);

  // 시간 포맷팅 (예: "5분 전" 또는 날짜)
  const formatTime = (isoString) => {
    const date = new Date(isoString);
    const now = new Date();
    const diffInMinutes = Math.floor((now - date) / (1000 * 60));

    if (diffInMinutes < 1) return '방금 전';
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
    if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}시간 전`;
    return date.toLocaleDateString();
  };

  // 채팅방 타입별 아이콘 (PUBLIC/PRIVATE)
  const getTypeIcon = (type) => 
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  // 언어별 색상 반환
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

  // ── 10) 렌더링 로직 ────────────────────────────────────────
  if (loading) {
    return <div className={styles.loading}>로딩 중...</div>;
  }

  if (error || !roomInfo) {
    return <div className={styles.error}>채팅방 정보를 불러오는 중 오류가 발생했습니다.</div>;
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
              <p className={styles.roomDescription}>{roomInfo.description || '설명 없음'}</p>
            </div>
          </div>
        </div>
        <div className={styles.headerRight}>
          <div className={styles.memberCount}>
            <Users size={18} />
            <span>{roomInfo.memberCount}명</span>
          </div>
          <div className={styles.connectionStatus}>
            <div className={`${styles.statusDot} ${styles.active}`}></div>
            <span>실시간</span>
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
                className={`${styles.message} ${message.isMe ? styles.messageMe : styles.messageOther}`}>
                {!message.isMe && (
                  <div className={styles.messageAvatar}>
                    <div className={styles.avatarCircle}>
                      {message.nickname[0]}
                    </div>
                  </div>
                )}
                <div className={styles.messageContent}>
                  {!message.isMe && (
                    <div className={styles.messageHeader}>
                      <span className={styles.messageNickname}>{message.nickname}</span>
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
                            <span style={{ color: getLanguageColor(message.language) }}>
                              {supportedLanguages.find(lang => lang.value === message.language)?.label || message.language}
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
                          showLineNumbers={true}
                          wrapLongLines={true}
                          className={styles.codeBlock}
                        >
                          {message.code}
                        </SyntaxHighlighter>
                        {message.content && (
                          <p className={styles.codeDescription}>{message.content}</p>
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
              {onlineMembers.filter(m => m.isOnline).length}명
            </span>
          </div>
          <div className={styles.membersList}>
            {onlineMembers.map((member) => (
              <div key={member.userId} className={styles.memberItem}>
                <div className={styles.memberAvatar}>
                  <div className={styles.avatarCircle}>
                    {member.nickname[0]}
                  </div>
                  <div className={`${styles.statusIndicator} ${member.isOnline ? styles.online : styles.offline}`}></div>
                </div>
                <div className={styles.memberInfo}>
                  <span className={styles.memberNickname}>{member.nickname}</span>
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
        {isTyping && (
          <div className={styles.typingIndicator}>
            <span>김개발님이 입력 중...</span>
          </div>
        )}
        <div className={styles.inputBox}>
          <div className={styles.inputWrapper}>
            <textarea
              ref={inputRef}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder="메시지를 입력하세요..."
              className={styles.messageInput}
              rows="1"
            />
            <button className={styles.emojiButton}>
              <Smile size={20} />
            </button>
            <button 
              className={styles.codeButton}
              onClick={() => setShowCodeModal(true)}
              title="코드 블록 추가"
            >
              <Code size={20} />
            </button>
          </div>
          <button 
            className={styles.sendButton}
            onClick={handleSendMessage}
            disabled={!inputMessage.trim()}
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
                    disabled={!codeInput.trim()}
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