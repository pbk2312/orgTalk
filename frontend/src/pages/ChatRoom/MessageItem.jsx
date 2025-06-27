import React from 'react';
import { Clock, Code, Copy, Check } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { okaidia } from 'react-syntax-highlighter/dist/esm/styles/prism';
import styles from '../../css/ChatRoom/MessageItem.module.css';
import { formatKoreaTime, isToday, isYesterday, formatRelativeDate } from '../../util/formatTime';
import { supportedLanguages, getLanguageColor } from '../../constants/chatConstants';

const MessageItem = React.memo(({ message, participants, onCopy, copiedCodeId }) => {

  const getSenderInfo = () => {
    return message.senderProfile || 
           participants.find(p => p.userId === message.userId) ||
           {
             userId: message.userId,
             login: message.nickname,
             avatarUrl: null
           };
  };


  const getLanguageLabel = (language) => {
    return supportedLanguages.find(l => l.value === language)?.label || language;
  };


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


  const renderMessageHeader = (sender) => (
    <div className={styles.messageHeader}>
      <span className={styles.messageNickname}>
        {sender?.login || message.nickname}
      </span>
    </div>
  );


  const renderMessageTime = () => {
    const timeStr = formatKoreaTime(message.timestamp);
    let dateStr = '';
    
    if (isToday(message.timestamp)) {
      dateStr = ''; 
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


  const renderTextMessage = () => {

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