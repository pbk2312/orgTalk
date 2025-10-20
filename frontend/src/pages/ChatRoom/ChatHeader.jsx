import React, {useState} from 'react';
import {
  ArrowLeft,
  Crown,
  Edit3,
  Globe,
  Lock,
  Shield,
  Sparkles,
  Trash2,
  UserMinus,
  Users
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import styles from '../../css/ChatRoom/ChatRoomHeader.module.css';
import {kickMember} from '../../service/ChatService';

const ChatHeader = ({
  roomInfo,
  participants,
  connected,
  onBack,
  onDeleteRoom,
  onUpdateRoom,
  currentUserId,
  onMemberKicked,
  roomIdNum
}) => {
  console.log(roomInfo)
  const navigate = useNavigate();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [kickingMemberId, setKickingMemberId] = useState(null);
  const [editForm, setEditForm] = useState({
    name: '',
    description: '',
    type: 'PUBLIC',
    password: ''
  });

  const getTypeIcon = type =>
      type === 'PRIVATE' ? <Lock size={16}/> : <Globe size={16}/>;

  // 현재 사용자가 방장인지 확인
  const isOwner = roomInfo.ownerId === currentUserId;

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
    setEditForm(prev => ({...prev, [field]: value}));
  };

  const handleConfirmEdit = () => {

    if (editForm.type === 'PRIVATE' && roomInfo.type !== 'PRIVATE'
        && !editForm.password.trim()) {
      alert('PRIVATE 채팅방으로 변경하려면 비밀번호를 입력해주세요.');
      return;
    }

    const updateData = {
      name: editForm.name.trim() || roomInfo.name,
      description: editForm.description || roomInfo.description,
      type: editForm.type
    };

    if (editForm.type === 'PRIVATE' && editForm.password.trim()) {
      updateData.password = editForm.password.trim();
    }

    onUpdateRoom?.(updateData);
    setShowEditModal(false);
  };

  const handleCancelEdit = () => setShowEditModal(false);

  const handleMemberClick = () => setShowMemberModal(true);
  const handleCloseMemberModal = () => setShowMemberModal(false);

  const handleKickMember = async (memberId) => {
    if (!isOwner) {
      alert('방장만 멤버를 강퇴할 수 있습니다.');
      return;
    }

    if (memberId === currentUserId) {
      alert('자신을 강퇴할 수 없습니다.');
      return;
    }

    if (memberId === roomInfo.ownerId) {
      alert('방장을 강퇴할 수 없습니다.');
      return;
    }

    const member = participants.find(p => p.userId === memberId);
    if (!member) {
      return;
    }

    const confirmed = window.confirm(`'${member.login}' 님을 강퇴하시겠습니까?`);
    if (!confirmed) {
      return;
    }

    try {
      setKickingMemberId(memberId);
      await kickMember(roomIdNum, memberId);

      onMemberKicked?.(memberId);

      setShowMemberModal(false);

      alert('멤버가 성공적으로 강퇴되었습니다.');
    } catch (error) {
      console.error('멤버 강퇴 실패:', error);
      alert(error.message || '멤버 강퇴 중 오류가 발생했습니다.');
    } finally {
      setKickingMemberId(null);
    }
  };

  const getMemberRole = (member) => {
    if (member.userId === roomInfo.ownerId) {
      return {
        icon: <Crown size={14}/>,
        text: '방장',
        className: styles.ownerRole
      };
    }
    return null;
  };

  return (
      <>
        <header className={styles.chatHeader}>
          <div className={styles.headerLeft}>
            <button className={styles.backButton} onClick={onBack}
                    aria-label="뒤로가기">
              <ArrowLeft size={20}/>
            </button>
            <div className={styles.roomInfo}>
              <div className={styles.roomIcon}>
                {getTypeIcon(roomInfo.type)}
              </div>
              <div className={styles.roomDetails}>
                <div className={styles.roomNameRow}>
                  <h1 className={styles.roomName}>{roomInfo.name}</h1>
                  <div className={styles.roomType}>{getTypeIcon(
                      roomInfo.type)}</div>
                </div>
                <p className={styles.roomDescription}>
                  {roomInfo.description || '설명 없음'}
                </p>
              </div>
            </div>
          </div>
          <div className={styles.headerRight}>
            <button
                className={styles.aiMentorButton}
                onClick={() => navigate('/ai-mentor')}
                title="AI 멘토에게 질문하기"
                aria-label="AI 멘토"
            >
              <Sparkles size={18}/>
              <span>AI 멘토</span>
            </button>
            <button
                className={styles.memberButton}
                onClick={handleMemberClick}
                title="멤버 관리"
                aria-label="멤버 관리"
            >
              <Users size={18}/>
              <span>{participants.length}</span>
            </button>
            <div className={styles.connectionStatus}
                 title={connected ? '실시간 연결됨' : '연결 중...'}>
              <div className={`${styles.statusDot} ${connected ? styles.active
                  : styles.inactive}`}/>
              <span>{connected ? '실시간' : '연결 중...'}</span>
            </div>
            {isOwner && (
                <>
                  <button className={styles.editButton}
                          onClick={handleEditClick} title="채팅방 수정"
                          aria-label="채팅방 수정">
                    <Edit3 size={18}/>
                  </button>
                  <button className={styles.deleteButton}
                          onClick={handleDeleteClick} title="채팅방 삭제"
                          aria-label="채팅방 삭제">
                    <Trash2 size={18}/>
                  </button>
                </>
            )}
          </div>
        </header>

        {/* 멤버 관리 모달 */}
        {showMemberModal && (
            <div className={styles.memberModal}>
              <div className={styles.memberModalBackdrop}
                   onClick={handleCloseMemberModal}/>
              <div className={styles.memberModalContent}>
                <div className={styles.memberModalHeader}>
                  <Users size={24} className={styles.memberModalIcon}/>
                  <h3 className={styles.memberModalTitle}>
                    멤버 관리 ({participants.length}명)
                  </h3>
                  <button
                      className={styles.closeButton}
                      onClick={handleCloseMemberModal}
                      aria-label="닫기"
                  >
                    ×
                  </button>
                </div>
                <div className={styles.memberList}>
                  {participants.map((member) => {
                    const role = getMemberRole(member);
                    const isCurrentUser = member.userId === currentUserId;
                    const canKick = isOwner && !isCurrentUser && member.userId
                        !== roomInfo.ownerId;

                    return (
                        <div key={member.userId} className={styles.memberItem}>
                          <div className={styles.memberInfo}>
                            {member.avatarUrl ? (
                                <img
                                    src={member.avatarUrl}
                                    alt={`${member.login} 프로필`}
                                    className={styles.memberAvatar}
                                />
                            ) : (
                                <div className={styles.memberAvatarPlaceholder}>
                                  {member.login.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div className={styles.memberDetails}>
                              <div className={styles.memberNameRow}>
                          <span className={styles.memberName}>
                            {member.login}
                            {isCurrentUser && <span
                                className={styles.youLabel}> (나)</span>}
                          </span>
                                {role && (
                                    <div
                                        className={`${styles.memberRole} ${role.className}`}>
                                      {role.icon}
                                      <span>{role.text}</span>
                                    </div>
                                )}
                              </div>
                            </div>
                          </div>
                          <div className={styles.memberActions}>
                            {canKick && (
                                <button
                                    className={styles.kickButton}
                                    onClick={() => handleKickMember(
                                        member.userId)}
                                    disabled={kickingMemberId === member.userId}
                                    title="강퇴하기"
                                    aria-label={`${member.login} 강퇴하기`}
                                >
                                  {kickingMemberId === member.userId ? (
                                      <div className={styles.loadingSpinner}/>
                                  ) : (
                                      <UserMinus size={16}/>
                                  )}
                                </button>
                            )}
                          </div>
                        </div>
                    );
                  })}
                </div>
                {isOwner && (
                    <div className={styles.memberModalFooter}>
                      <div className={styles.ownerNotice}>
                        <Shield size={16}/>
                        <span>방장 권한으로 멤버를 관리할 수 있습니다.</span>
                      </div>
                    </div>
                )}
              </div>
            </div>
        )}

        {showEditModal && (
            <div className={styles.editModal}>
              <div className={styles.editModalBackdrop}
                   onClick={handleCancelEdit}/>
              <div className={styles.editModalContent}>
                <div className={styles.editModalHeader}>
                  <Edit3 size={24} className={styles.editModalIcon}/>
                  <h3 className={styles.editModalTitle}>채팅방 수정</h3>
                </div>
                <div className={styles.editModalForm}>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>채팅방 이름</label>
                    <input
                        type="text"
                        className={styles.formInput}
                        value={editForm.name}
                        onChange={e => handleEditFormChange('name',
                            e.target.value)}
                        placeholder="채팅방 이름을 입력하세요"
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>설명</label>
                    <textarea
                        className={styles.formTextarea}
                        value={editForm.description}
                        onChange={e => handleEditFormChange('description',
                            e.target.value)}
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
                            onChange={e => handleEditFormChange('type',
                                e.target.value)}
                        />
                        <Globe size={16}/>
                        <span>공개</span>
                      </label>
                      <label className={styles.typeOption}>
                        <input
                            type="radio"
                            name="roomType"
                            value="PRIVATE"
                            checked={editForm.type === 'PRIVATE'}
                            onChange={e => handleEditFormChange('type',
                                e.target.value)}
                        />
                        <Lock size={16}/>
                        <span>비공개</span>
                      </label>
                    </div>
                  </div>
                  {editForm.type === 'PRIVATE' && (
                      <div className={styles.formGroup}>
                        <label className={styles.formLabel}>
                          비밀번호
                          {roomInfo.type === 'PRIVATE' ? (
                              <span
                                  className={styles.optional}>(비밀번호 변경 시에만 입력)</span>
                          ) : (
                              <span className={styles.required}>*</span>
                          )}
                        </label>
                        <input
                            type="password"
                            className={styles.formInput}
                            value={editForm.password}
                            onChange={e => handleEditFormChange('password',
                                e.target.value)}
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
                  <button className={styles.cancelButton}
                          onClick={handleCancelEdit}>취소
                  </button>
                  <button className={styles.confirmEditButton}
                          onClick={handleConfirmEdit}>수정하기
                  </button>
                </div>
              </div>
            </div>
        )}

        {showDeleteConfirm && (
            <div className={styles.deleteModal}>
              <div className={styles.deleteModalBackdrop}
                   onClick={handleCancelDelete}/>
              <div className={styles.deleteModalContent}>
                <div className={styles.deleteModalHeader}>
                  <Trash2 size={24} className={styles.deleteModalIcon}/>
                  <h3 className={styles.deleteModalTitle}>채팅방 삭제</h3>
                </div>
                <p className={styles.deleteModalMessage}>
                  '<strong>{roomInfo.name}</strong>' 채팅방을 정말 삭제하시겠습니까?
                  <br/>
                  <span className={styles.deleteWarning}>
                이 작업은 되돌릴 수 없으며, 모든 메시지가 영구적으로 삭제됩니다.
              </span>
                </p>
                <div className={styles.deleteModalActions}>
                  <button className={styles.cancelButton}
                          onClick={handleCancelDelete}>취소
                  </button>
                  <button className={styles.confirmDeleteButton}
                          onClick={handleConfirmDelete}>삭제하기
                  </button>
                </div>
              </div>
            </div>
        )}
      </>
  );
};

export default ChatHeader;
