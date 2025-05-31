// src/components/CreateChatRoomModal.jsx
import React, { useState } from 'react';
import { useParams } from 'react-router-dom'; // URL 파라미터를 읽기 위해 추가
import { X, Edit3, Lock, Globe, Users, Hash, Sparkles } from 'lucide-react';
import styles from '../css/CreateChatRoomModal.module.css';
import { createChatRoom } from '../service/ChatService.jsx';

/**
 * URL 경로가 /chat-rooms/:orgId 인 상태에서,
 * 모달 안에서 직접 orgId를 읽어와 payload에 포함합니다.
 *
 * @param {Object} props
 * @param {boolean} props.isOpen
 * @param {() => void} props.onClose
 * @param {(newRoom: any) => void} props.onCreate
 */
const CreateChatRoomModal = ({ isOpen, onClose, onCreate }) => {
  const { orgId } = useParams();                 // URL에서 orgId 읽어옴
  const orgIdNum = orgId ? Number(orgId) : null;  // 숫자형으로 변환

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState('public');      // "public"/"private"
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!orgIdNum) {
      setErrorMsg('유효한 조직 ID가 존재하지 않습니다.');
      return;
    }

    setIsSubmitting(true);
    setErrorMsg('');

    // "public" / "private" 을 백엔드 RoomType enum으로 매핑
    const enumType = type === 'private' ? 'PRIVATE' : 'PUBLIC';

    const payload = {
      organizationId: orgIdNum,                // URL에서 가져온 숫자형 orgId
      name: name.trim(),                       // 필수
      description: description.trim() || null, // 선택사항
      type: enumType,                          // "PUBLIC" or "PRIVATE"
    };

    try {
      const newRoom = await createChatRoom(payload);
      onCreate(newRoom);

      // 폼 초기화 및 모달 닫기
      setName('');
      setDescription('');
      setType('public');
      onClose();
    } catch (err) {
      console.error(err);
      setErrorMsg('채팅방 생성 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      {/* Background Effects */}
      <div className={styles['background-effects']}>
        <div className={styles['bg-circle-1']} />
        <div className={styles['bg-circle-2']} />
      </div>

      <div className={styles.container} onClick={(e) => e.stopPropagation()}>
        <div className={styles['gradient-border']} />

        <div className={styles.content}>
          {/* Header */}
          <div className={styles.header}>
            <div className={styles['header-left']}>
              <div className={styles['icon-container']}>
                <Sparkles className={styles['header-icon']} />
              </div>
              <div className={styles['header-text']}>
                <h2 className={styles.title}>새 채팅방</h2>
                <p className={styles.subtitle}>팀과 소통할 공간을 만들어보세요</p>
              </div>
            </div>

            <button onClick={onClose} className={styles['close-button']}>
              <X size={20} />
            </button>
          </div>

          {/* Form */}
          <div className={styles.form}>
            {/* 에러 메시지 */}
            {errorMsg && (
              <div className={styles['error-message']}>{errorMsg}</div>
            )}

            {/* 방 이름 */}
            <div className={styles['form-group']}>
              <label className={styles.label}>
                <Hash className={styles['label-icon']} />
                방 이름
              </label>
              <div className={styles['input-wrapper']}>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  placeholder="예: 개발팀 회의방"
                  className={styles.input}
                />
                <div className={styles['input-glow']} />
              </div>
            </div>

            {/* 설명 (선택사항) */}
            <div className={styles['form-group']}>
              <label className={styles.label}>
                <Edit3 className={styles['label-icon']} />
                설명 (선택사항)
              </label>
              <div className={styles['input-wrapper']}>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="채팅방에 대한 간단한 설명을 입력하세요"
                  rows={3}
                  className={styles.textarea}
                />
                <div className={styles['input-glow']} />
              </div>
            </div>

            {/* 공개 설정 (public / private) */}
            <div className={styles['form-group']}>
              <label className={styles.label}>
                <Users className={styles['label-icon']} />
                공개 설정
              </label>
              <div className={styles['radio-grid']}>
                <label
                  className={`${styles['radio-card']} ${type === 'public' ? styles.selected : ''}`}
                >
                  <input
                    type="radio"
                    name="type"
                    value="public"
                    checked={type === 'public'}
                    onChange={() => setType('public')}
                    className={styles['radio-input']}
                  />
                  <div className={styles['radio-content']}>
                    <div className={`${styles['radio-icon']} ${type === 'public' ? styles.active : ''}`}>
                      <Globe size={16} />
                    </div>
                    <div className={styles['radio-text']}>
                      <div className={styles['radio-title']}>공개</div>
                      <div className={styles['radio-subtitle']}>모든 멤버가 참여</div>
                    </div>
                  </div>
                  {type === 'public' && <div className={styles['selection-dot']} />}
                </label>

                <label
                  className={`${styles['radio-card']} ${type === 'private' ? styles.selected : ''}`}
                >
                  <input
                    type="radio"
                    name="type"
                    value="private"
                    checked={type === 'private'}
                    onChange={() => setType('private')}
                    className={styles['radio-input']}
                  />
                  <div className={styles['radio-content']}>
                    <div className={`${styles['radio-icon']} ${type === 'private' ? styles.active : ''}`}>
                      <Lock size={16} />
                    </div>
                    <div className={styles['radio-text']}>
                      <div className={styles['radio-title']}>비공개</div>
                      <div className={styles['radio-subtitle']}>초대된 멤버만</div>
                    </div>
                  </div>
                  {type === 'private' && <div className={styles['selection-dot']} />}
                </label>
              </div>
            </div>

            {/* 생성 버튼 */}
            <button
              onClick={handleSubmit}
              disabled={isSubmitting || !name.trim()}
              className={`${styles['submit-button']} ${isSubmitting ? styles.loading : ''}`}
            >
              {isSubmitting ? (
                <>
                  <div className={styles['loading-spinner']} />
                  <span>생성 중...</span>
                </>
              ) : (
                <>
                  <Edit3 size={18} />
                  <span>채팅방 생성하기</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateChatRoomModal;
