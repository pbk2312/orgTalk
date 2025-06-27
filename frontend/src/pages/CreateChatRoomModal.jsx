// src/components/CreateChatRoomModal.jsx
import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom'; 
import { X, Edit3, Lock, Globe, Users, Hash, Sparkles } from 'lucide-react';
import styles from '../css/CreateChatRoomModal.module.css';
import { createChatRoom } from '../service/ChatService.jsx';

const CreateChatRoomModal = ({ isOpen, onClose /* onCreate 제거하거나 유지 */ }) => {
  const { orgId } = useParams();
  const navigate = useNavigate(); // ← navigate 훅

  const orgIdNum = orgId ? Number(orgId) : null;
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState('public');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!orgIdNum) {
      setErrorMsg('유효한 조직 ID가 존재하지 않습니다.');
      return;
    }

    if (type === 'private' && password.trim() === '') {
      setErrorMsg('비공개 방의 비밀번호를 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    setErrorMsg('');

    const enumType = type === 'private' ? 'PRIVATE' : 'PUBLIC';
    const payload = {
      organizationId: orgIdNum,
      name: name.trim(),
      description: description.trim() || null,
      type: enumType,
      password: type === 'private' ? password.trim() : null
    };

    try {
 
      const { id: newRoomId } = await createChatRoom(payload);

 
      navigate(`/chatroom/${newRoomId}`);

   
      onClose();

    } catch (err) {
      console.error('CreateChatRoomModal handleSubmit error:', err);
      setErrorMsg(err.message || '채팅방 생성 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles['background-effects']}>
        <div className={styles['bg-circle-1']} />
        <div className={styles['bg-circle-2']} />
      </div>

      <div className={styles.container} onClick={(e) => e.stopPropagation()}>
        <div className={styles['gradient-border']} />
        <div className={styles.content}>
          {/* 헤더 */}
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

          {/* 폼 */}
          <div className={styles.form}>
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

            {/* 설명 (선택) */}
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

            {/* 공개 설정 */}
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

            {type === 'private' && (
              <div className={styles['form-group']}>
                <label className={styles.label}>
                  <Lock className={styles['label-icon']} />
                  비밀번호
                </label>
                <div className={styles['input-wrapper']}>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required={type === 'private'}
                    placeholder="비공개 방 비밀번호를 입력하세요"
                    className={styles.input}
                  />
                  <div className={styles['input-glow']} />
                </div>
              </div>
            )}

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
