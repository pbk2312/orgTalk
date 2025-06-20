import React from 'react';
import { Hash, Lock, Globe, Users, Clock, ChevronRight } from 'lucide-react';
import styles from '../css/ChatRoomCard.module.css';
import { formatTime } from '../util/ChatRoomsFormatTime';

const ChatRoomCard = ({
  room,
  isHovered,
  isSelected,
  onClick,
  onMouseEnter,
  onMouseLeave
}) => {
  const getTypeIcon = (type) =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  return (
    <div
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      className={`${styles['room-card']} ${isHovered ? styles.hovered : ''} ${isSelected ? styles.selected : ''}`}
    >
      <div className={styles['room-card-header']}>
        <div className={styles['room-icon']}><Hash size={20} /></div>
        <div className={styles['room-info']}>
          <div className={styles['room-name-row']}>
            <h3 className={styles['room-name']}>{room.name}</h3>
            <div className={styles['room-type']}>{getTypeIcon(room.type)}</div>
          </div>
          <p className={styles['room-description']}>{room.description}</p>
        </div>
      </div>

      {/* 생성일 표시 */}
      <div className={styles['room-card-body']}>
        <div className={styles['created-time']}>  
          <Clock size={12} />
          <span>{formatTime(room.createdAt)}</span>
        </div>
      </div>

      <div className={styles['room-card-footer']}>
        <div className={styles['member-count']}>
          <Users size={16} /> <span>{room.memberCount}명</span>
        </div>
        <div className={styles['room-status']}>
          {room.joined ? (
            <>
              <div className={styles['status-dot-joined']}></div>
              <span className={styles['status-text-joined']}>참여중</span>
            </>
          ) : (
            <>
              <div className={styles['status-dot-notJoined']}></div>
              <span className={styles['status-text-notJoined']}>미참여</span>
            </>
          )}
        </div>
        <ChevronRight size={16} className={styles['enter-icon']} />
      </div>
    </div>
  );
};

export default ChatRoomCard;