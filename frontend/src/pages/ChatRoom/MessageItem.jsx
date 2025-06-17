// MessageItem.jsx 수정
import React from 'react';
import { Clock, Code, Copy, Check } from 'lucide-react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { okaidia } from 'react-syntax-highlighter/dist/esm/styles/prism';
import styles from '../../css/ChatRoom.module.css';
import { formatTime } from '../../util/formatTime';
import { supportedLanguages, getLanguageColor } from '../../constants/chatConstants';

const MessageItem = ({ message, participants, onCopy, copiedCodeId }) => {
  // 1순위: 메시지에 포함된 발신자 프로필 정보 사용
  // 2순위: 현재 participants에서 찾기 (실시간 참여자 정보)
  // 3순위: 메시지의 기본 정보 사용
  const sender = message.senderProfile || 
                 participants.find(p => p.userId === message.userId) ||
                 {
                   userId: message.userId,
                   login: message.nickname,
                   avatarUrl: null
                 };

  return (
    <div
      key={message.id}
      className={`${styles.message} ${message.isMe ? styles.messageMe : styles.messageOther}`}
    >
      {/* 다른 사람 메시지일 때만 아바타 */}
      {!message.isMe && (
        <div className={styles.messageAvatar}>
          {sender?.avatarUrl ? (
            <img src={sender.avatarUrl} alt={sender.login} className={styles.avatarImage} />
          ) : (
            <div className={styles.avatarCircle}>
              {(sender?.login || message.nickname)?.[0] || '🤖'}
            </div>
          )}
        </div>
      )}

      <div className={styles.messageContent}>
        {/* 다른 사람 메시지일 때 헤더(닉네임+시간) */}
        {!message.isMe && (
          <div className={styles.messageHeader}>
            <span className={styles.messageNickname}>
              {sender?.login || message.nickname}
            </span>
            <span className={styles.messageTime}>
              <Clock size={12} /> {formatTime(message.timestamp)}
            </span>
          </div>
        )}

        <div className={styles.messageBubble}>
          {message.type === 'text' ? (
            // 일반 텍스트 메시지
            <p className={styles.messageText}>{message.content}</p>
          ) : (
            // 코드 메시지
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
                  onClick={() => onCopy(message.code, message.id)}
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

          {/* 본인 메시지일 때 시간 표시 */}
          {message.isMe && (
            <div className={styles.messageTimeMe}>
              <Clock size={12} /> {formatTime(message.timestamp)}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MessageItem;