import React, { useState } from 'react';
import { ArrowLeft, Hash, Lock, Globe, Users, Trash2, Edit3 } from 'lucide-react';
import styles from '../../css/ChatRoom/ChatRoomHeader.module.css';

const ChatHeader = ({ roomInfo, participants, connected, onBack, onDeleteRoom, onUpdateRoom }) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({
    name: '',
    description: '',
    type: 'PUBLIC',
    password: ''
  });

  const getTypeIcon = type =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  const handleDeleteClick = () => setShowDeleteConfirm(true);
  const handleConfirmDelete = () => {
    onDeleteRoom?.(roomInfo.id);
    setShowDeleteConfirm(false);
  };
  const handleCancelDelete = () => setShowDeleteConfirm(false);

  const handleEditClick = () => {
    setEditForm({
      name: roomInfo.name || '',
      description: roomInfo.description || '',
      type: roomInfo.type || 'PUBLIC',
      password: ''
    });
    setShowEditModal(true);
  };

  const handleEditFormChange = (field, value) => {
    setEditForm(prev => ({ ...prev, [field]: value }));
  };

  const handleConfirmEdit = () => {
    // PUBLIC -> PRIVATE 전환 시 비밀번호 입력 필수
    if (editForm.type === 'PRIVATE' && roomInfo.type !== 'PRIVATE' && !editForm.password.trim()) {
      alert('PRIVATE 채팅방으로 변경하려면 비밀번호를 입력해주세요.');
      return;
    }

    // PATCH 요청: 변경 혹은 기존 값을 모두 포함
    const updateData = {
      name: editForm.name.trim() || roomInfo.name,
      description: editForm.description || roomInfo.description,
      type: editForm.type
    };

    // PRIVATE인 경우 비밀번호 변경 시에만 포함
    if (editForm.type === 'PRIVATE' && editForm.password.trim()) {
      updateData.password = editForm.password.trim();
    }

    onUpdateRoom?.(updateData);
    setShowEditModal(false);
  };

  const handleCancelEdit = () => setShowEditModal(false);

  return (
    <>
      <header className={styles.chatHeader}>
        <div className={styles.headerLeft}>
          <button className={styles.backButton} onClick={onBack} aria-label="뒤로가기">
            <ArrowLeft size={20} />
          </button>
          <div className={styles.roomInfo}>
            <div className={styles.roomIcon}><Hash size={20} /></div>
            <div className={styles.roomDetails}>
              <div className={styles.roomNameRow}>
                <h1 className={styles.roomName}>{roomInfo.name}</h1>
                <div className={styles.roomType}>{getTypeIcon(roomInfo.type)}</div>
              </div>
              <p className={styles.roomDescription}>
                {roomInfo.description || '설명 없음'}
              </p>
            </div>
          </div>
        </div>
        <div className={styles.headerRight}>
          <div className={styles.memberCount} title="현재 참여자 수">
            <Users size={18} /><span>{participants.length}명</span>
          </div>
          <div className={styles.connectionStatus} title={connected ? '실시간 연결됨' : '연결 중...'}>
            <div className={`${styles.statusDot} ${connected ? styles.active : styles.inactive}`} />
            <span>{connected ? '실시간' : '연결 중...'}</span>
          </div>
          <button className={styles.editButton} onClick={handleEditClick} title="채팅방 수정" aria-label="채팅방 수정">
            <Edit3 size={18} />
          </button>
          <button className={styles.deleteButton} onClick={handleDeleteClick} title="채팅방 삭제" aria-label="채팅방 삭제">
            <Trash2 size={18} />
          </button>
        </div>
      </header>

      {showEditModal && (
        <div className={styles.editModal}>
          <div className={styles.editModalBackdrop} onClick={handleCancelEdit} />
          <div className={styles.editModalContent}>
            <div className={styles.editModalHeader}>
              <Edit3 size={24} className={styles.editModalIcon} />
              <h3 className={styles.editModalTitle}>채팅방 수정</h3>
            </div>
            <div className={styles.editModalForm}>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>채팅방 이름</label>
                <input
                  type="text"
                  className={styles.formInput}
                  value={editForm.name}
                  onChange={e => handleEditFormChange('name', e.target.value)}
                  placeholder="채팅방 이름을 입력하세요"
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>설명</label>
                <textarea
                  className={styles.formTextarea}
                  value={editForm.description}
                  onChange={e => handleEditFormChange('description', e.target.value)}
                  placeholder="채팅방 설명을 입력하세요"
                  rows={3}
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>채팅방 타입</label>
                <div className={styles.typeSelector}>
                  <label className={styles.typeOption}>
                    <input
                      type="radio"
                      name="roomType"
                      value="PUBLIC"
                      checked={editForm.type === 'PUBLIC'}
                      onChange={e => handleEditFormChange('type', e.target.value)}
                    />
                    <Globe size={16} />
                    <span>공개</span>
                  </label>
                  <label className={styles.typeOption}>
                    <input
                      type="radio"
                      name="roomType"
                      value="PRIVATE"
                      checked={editForm.type === 'PRIVATE'}
                      onChange={e => handleEditFormChange('type', e.target.value)}
                    />
                    <Lock size={16} />
                    <span>비공개</span>
                  </label>
                </div>
              </div>
              {editForm.type === 'PRIVATE' && (
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>
                    비밀번호
                    {roomInfo.type === 'PRIVATE' ? (
                      <span className={styles.optional}>(비밀번호 변경 시에만 입력)</span>
                    ) : (
                      <span className={styles.required}>*</span>
                    )}
                  </label>
                  <input
                    type="password"
                    className={styles.formInput}
                    value={editForm.password}
                    onChange={e => handleEditFormChange('password', e.target.value)}
                    placeholder={
                      roomInfo.type === 'PRIVATE'
                        ? '새 비밀번호 (변경하지 않으려면 빈칸으로 두세요)'
                        : '비밀번호를 입력하세요'
                    }
                  />
                </div>
              )}
            </div>
            <div className={styles.editModalActions}>
              <button className={styles.cancelButton} onClick={handleCancelEdit}>취소</button>
              <button className={styles.confirmEditButton} onClick={handleConfirmEdit}>수정하기</button>
            </div>
          </div>
        </div>
      )}

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
              <button className={styles.cancelButton} onClick={handleCancelDelete}>취소</button>
              <button className={styles.confirmDeleteButton} onClick={handleConfirmDelete}>삭제하기</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatHeader;
