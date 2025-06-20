// src/pages/ChatRoom/ChatHeader.jsx
import React, { useState } from 'react';
import { ArrowLeft, Hash, Lock, Globe, Users, Trash2 } from 'lucide-react';
import styles from '../../css/ChatRoom/ChatRoomHeader.module.css';

const ChatHeader = ({ roomInfo, participants, connected, onBack, onDeleteRoom }) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const getTypeIcon = (type) =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  const handleDeleteClick = () => {
    setShowDeleteConfirm(true);
  };

  const handleConfirmDelete = () => {
    onDeleteRoom?.(roomInfo.id);
    setShowDeleteConfirm(false);
  };

  const handleCancelDelete = () => {
    setShowDeleteConfirm(false);
  };

  return (
    <>
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
          <button
            className={styles.deleteButton}
            onClick={handleDeleteClick}
            title="채팅방 삭제"
            aria-label="채팅방 삭제"
          >
            <Trash2 size={18} />
          </button>
        </div>
      </header>

      {/* 삭제 확인 모달 */}
      {showDeleteConfirm && (
        <div className={styles.deleteModal}>
          <div className={styles.deleteModalBackdrop} onClick={handleCancelDelete} />
          <div className={styles.deleteModalContent}>
            <div className={styles.deleteModalHeader}>
              <Trash2 size={24} className={styles.deleteModalIcon} />
              <h3 className={styles.deleteModalTitle}>채팅방 삭제</h3>
            </div>
            <p className={styles.deleteModalMessage}>
              '<strong>{roomInfo.name}</strong>' 채팅방을 정말 삭제하시겠습니까?
              <br />
              <span className={styles.deleteWarning}>
                이 작업은 되돌릴 수 없으며, 모든 메시지가 영구적으로 삭제됩니다.
              </span>
            </p>
            <div className={styles.deleteModalActions}>
              <button
                className={styles.cancelButton}
                onClick={handleCancelDelete}
              >
                취소
              </button>
              <button
                className={styles.confirmDeleteButton}
                onClick={handleConfirmDelete}
              >
                삭제하기
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatHeader;