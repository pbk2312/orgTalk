// src/pages/PublicRoomJoinModal.jsx
import React from 'react';
import { 
  X, 
  Users, 
  MessageCircle, 
  Shield, 
  Check,
  AlertTriangle,
  Ban,
  Zap
} from 'lucide-react';
import styles from '../css/JoinConfirmModal.module.css';

const PublicRoomJoinModal = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  room, 
  isLoading
}) => {
  if (!isOpen || !room) return null;

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handleConfirm = () => {
    onConfirm(room);
  };

  return (
    <div className={styles['modal-overlay']} onClick={handleOverlayClick}>
      <div className={styles['join-guide-modal']}>
        <div className={styles['modal-header']}>
          <div className={styles['modal-title-section']}>
            <MessageCircle size={20} className={styles['modal-icon']} />
            <div>
              <h2 className={styles['modal-title']}>채팅방 참여</h2>
            </div>
          </div>
          <button
            className={styles['modal-close-btn']}
            onClick={onClose}
            disabled={isLoading}
            aria-label="모달 닫기"
          >
            <X size={18} />
          </button>
        </div>

        <div className={styles['modal-content']}>
          <div className={styles['room-info-section']}>
            <div className={styles['room-info-header']}>
              <div className={styles['room-type-badge']}>
                <Users size={12} />
                <span>공개방</span>
              </div>
              <h3 className={styles['room-name']}>{room.name}</h3>
            </div>
            
            {room.description && (
              <p className={styles['room-description']}>{room.description}</p>
            )}
            
            <div className={styles['room-stats']}>
              <div className={styles['stat-item']}>
                <Users size={16} />
                <span>
                  {room.participantCount !== undefined && room.participantCount !== null 
                    ? `${room.participantCount}명 참여 중`
                    : room.memberCount !== undefined && room.memberCount !== null
                    ? `${room.memberCount}명 참여 중`
                    : room.userCount !== undefined && room.userCount !== null
                    ? `${room.userCount}명 참여 중`
                    : '참여 중'}
                </span>
              </div>
              <div className={styles['stat-item']}>
                <Zap size={16} />
                <span>실시간 채팅</span>
              </div>
            </div>
          </div>

          <div className={styles['guidelines-section']}>
            <div className={styles['guidelines-header']}>
              <Shield size={18} className={styles['guidelines-icon']} />
              <h4>채팅 규칙</h4>
            </div>
            
            <div className={styles['guidelines-content']}>
              <div className={styles['guideline-item']}>
                <Check size={16} className={styles['guideline-icon']} />
                <div className={styles['guideline-text']}>
                  <strong>매너 채팅</strong>
                  <p>상대방을 존중하고 예의를 지켜주세요</p>
                </div>
              </div>
              
              <div className={styles['guideline-item']}>
                <AlertTriangle size={16} className={styles['guideline-icon']} />
                <div className={styles['guideline-text']}>
                  <strong>건전한 대화</strong>
                  <p>불쾌감을 주는 언어나 내용은 피해주세요</p>
                </div>
              </div>
              
              <div className={styles['guideline-item']}>
                <Ban size={16} className={styles['guideline-icon']} />
                <div className={styles['guideline-text']}>
                  <strong>스팸 금지</strong>
                  <p>같은 메시지 반복이나 광고성 내용은 금지입니다</p>
                </div>
              </div>
            </div>
          </div>

          <div className={styles['notice-section']}>
            <p className={styles['notice-text']}>
              위 규칙을 지키지 않을 경우 채팅방에서 제재를 받을 수 있습니다.
            </p>
          </div>
        </div>

        <div className={styles['modal-actions']}>
          <button
            className={styles['cancel-btn']}
            onClick={onClose}
            disabled={isLoading}
          >
            취소
          </button>
          <button
            className={styles['confirm-btn']}
            onClick={handleConfirm}
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <div style={{ 
                  width: '16px', 
                  height: '16px', 
                  border: '2px solid rgba(255,255,255,0.3)', 
                  borderTop: '2px solid white', 
                  borderRadius: '50%', 
                  animation: 'spin 1s linear infinite' 
                }} />
                참여 중...
              </>
            ) : (
              <>
                <Users size={16} />
                참여하기
              </>
            )}
          </button>
        </div>
      </div>
      
      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default PublicRoomJoinModal;