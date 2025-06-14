import React from 'react';
import styles from '../../css/ChatRoom/ChatSidebar.module.css';

const Sidebar = ({ participants }) => {
  console.log('Sidebar participants:', participants); 

  return (
    <div className={styles.sidebar}>
      <div className={styles.sidebarHeader}>
        <h3>접속중</h3>
        <span className={styles.onlineCount}>{participants.length}명</span>
      </div>
      <div className={styles.membersList}>
        {participants.length === 0 ? (
          <div className={styles.noMembers}>아직 접속한 멤버가 없습니다.</div>
        ) : (
          participants.map(member => (
            <div key={`${member.userId}-${member.login}`} className={styles.memberItem}>
              <div className={styles.memberAvatar}>
                {member.avatarUrl ? (
                  <img
                    src={member.avatarUrl}
                    alt={member.login}
                    className={styles.avatarImage}
                  />
                ) : (
                  <div className={styles.avatarCircle}>
                    {member.login?.[0] || '🤖'}
                  </div>
                )}
              </div>
              <div className={styles.memberInfo}>
                <span className={styles.memberNickname}>{member.login}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Sidebar;
