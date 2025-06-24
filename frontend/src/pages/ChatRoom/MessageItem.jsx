import React from 'react';
import { Clock, Code, Copy, Check } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { okaidia } from 'react-syntax-highlighter/dist/esm/styles/prism';
import styles from '../../css/ChatRoom/MessageItem.module.css';
import { formatKoreaTime, isToday, isYesterday, formatRelativeDate } from '../../util/formatTime';
import { supportedLanguages, getLanguageColor } from '../../constants/chatConstants';

const MessageItem = React.memo(({ message, participants, onCopy, copiedCodeId }) => {
  // 메시지 발신자 정보 가져오기
  const getSenderInfo = () => {
    return message.senderProfile || 
           participants.find(p => p.userId === message.userId) ||
           {
             userId: message.userId,
             login: message.nickname,
             avatarUrl: null
           };
  };

  // 언어 표시명 가져오기
  const getLanguageLabel = (language) => {
    return supportedLanguages.find(l => l.value === language)?.label || language;
  };

  // 아바타 렌더링
  const renderAvatar = (sender) => {
    if (sender?.avatarUrl) {
      return (
        <img 
          src={sender.avatarUrl} 
          alt={sender.login} 
          className={styles.avatarImage} 
        />
      );
    }
    
    return (
      <div className={styles.avatarCircle}>
        {(sender?.login || message.nickname)?.[0]?.toUpperCase() || '🤖'}
      </div>
    );
  };

  // 메시지 헤더 렌더링 (닉네임만)
  const renderMessageHeader = (sender) => (
    <div className={styles.messageHeader}>
      <span className={styles.messageNickname}>
        {sender?.login || message.nickname}
      </span>
    </div>
  );

  // 메시지 시간 렌더링 (말풍선 아래) - 날짜 구분 추가
  const renderMessageTime = () => {
    const timeStr = formatKoreaTime(message.timestamp);
    let dateStr = '';
    
    if (isToday(message.timestamp)) {
      dateStr = ''; // 오늘이면 날짜 표시 안함
    } else if (isYesterday(message.timestamp)) {
      dateStr = '어제 ';
    } else {
      dateStr = `${formatRelativeDate(message.timestamp)} `;
    }

    return (
      <div className={styles.messageTime}>
        <Clock size={12} />
        <span>{dateStr}{timeStr}</span>
      </div>
    );
  };

  // 일반 텍스트 메시지 렌더링 - 줄바꿈 처리
  const renderTextMessage = () => {
    // 줄바꿈을 <br> 태그로 변환
    const formatTextWithLineBreaks = (text) => {
      return text.split('\n').map((line, index, array) => (
        <React.Fragment key={index}>
          {line}
          {index < array.length - 1 && <br />}
        </React.Fragment>
      ));
    };

    return (
      <p className={styles.messageText}>
        {formatTextWithLineBreaks(message.content)}
      </p>
    );
  };

  // 코드 메시지 렌더링
  const renderCodeMessage = () => (
    <div className={styles.codeMessage}>
      <div className={styles.codeHeader}>
        <div className={styles.codeLanguage}>
          <Code size={14} />
          <span style={{ color: getLanguageColor(message.language) }}>
            {getLanguageLabel(message.language)}
          </span>
        </div>
        <button
          className={styles.copyButton}
          onClick={() => onCopy(message.code, message.id)}
          aria-label="코드 복사"
        >
          {copiedCodeId === message.id ? (
            <>
              <Check size={14} />
              <span>복사됨</span>
            </>
          ) : (
            <>
              <Copy size={14} />
              <span>복사</span>
            </>
          )}
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
  );

  const sender = getSenderInfo();
  const isMyMessage = message.isMe;
  const isCodeMessage = message.type === 'code';

  return (
    <div
      className={`${styles.message} ${isMyMessage ? styles.messageMe : styles.messageOther}`}
    >
      {/* 상대방 메시지일 때만 아바타 표시 */}
      {!isMyMessage && (
        <div className={styles.messageAvatar}>
          {renderAvatar(sender)}
        </div>
      )}

      <div className={styles.messageContent}>
        {/* 상대방 메시지일 때만 헤더 표시 (닉네임만) */}
        {!isMyMessage && renderMessageHeader(sender)}

        <div className={styles.messageBubble}>
          {/* 메시지 타입에 따른 내용 렌더링 */}
          {isCodeMessage ? renderCodeMessage() : renderTextMessage()}
        </div>

        {/* 모든 메시지에 시간 표시 (말풍선 아래) - 날짜 구분 포함 */}
        {renderMessageTime()}
      </div>
    </div>
  );
});

export default MessageItem;