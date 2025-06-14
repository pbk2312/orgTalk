// src/pages/ChatRoom/ChatHeader.jsx
import React from 'react';
import { ArrowLeft, Hash, Lock, Globe, Users, } from 'lucide-react';
import styles from '../../css/ChatRoom/ChatRoomHeader.module.css';


const ChatHeader = ({ roomInfo, participants, connected, onBack }) => {
  const getTypeIcon = (type) =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  return (
    <header className={styles.chatHeader}>
      <div className={styles.headerLeft}>
        <button
          className={styles.backButton}
          onClick={onBack}
          aria-label="뒤로가기"
        >
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
        <div className={styles.memberCount} title="현재 참여자 수">
          <Users size={18} />
          <span>{participants.length}명</span>
        </div>
        <div className={styles.connectionStatus} title={connected ? '실시간 연결됨' : '연결 중...'}>
          <div
            className={`${styles.statusDot} ${
              connected ? styles.active : styles.inactive
            }`}
          />
          <span>{connected ? '실시간' : '연결 중...'}</span>
        </div>
      </div>
    </header>
  );
};

export default ChatHeader;
