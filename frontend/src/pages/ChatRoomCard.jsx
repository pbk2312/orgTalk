import React from 'react';
import { Hash, Lock, Globe, Users, ChevronRight,  Calendar } from 'lucide-react';
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

  const getMainIcon = (type) => {
    switch(type) {
      case 'PRIVATE':
        return <Lock size={20} />;
      case 'PUBLIC':
        return <Globe size={20} />;
      default:
        return <Hash size={20} />;
    }
  };


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
        {/* 메인 아이콘을 방 타입에 따라 표시 */}
        <div className={styles['room-icon']}>{getMainIcon(room.type)}</div>
        <div className={styles['room-info']}>
          <div className={styles['room-name-row']}>
            <h3 className={styles['room-name']}>{room.name}</h3>
            {/* 작은 타입 아이콘은 제거하거나 유지 가능 */}
            <div className={styles['room-type']}>{getTypeIcon(room.type)}</div>
          </div>
          <p className={styles['room-description']}>{room.description}</p>
        </div>
      </div>

      {/* 생성일 표시 */}
      <div className={styles['room-card-body']}>
        <div className={styles['created-time']}>  
          <Calendar size={14} />
          <span className={styles['created-label']}>생성일</span>
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