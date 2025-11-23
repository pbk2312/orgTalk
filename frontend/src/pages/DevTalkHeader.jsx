import React from 'react';
import { Code2, Users, LogOut } from 'lucide-react';
import '../css/DevTalkHeader.css';
import { useAuth } from '../hooks/useAuth.ts';

const DevTalkHeader = () => {
  const { auth, loading, logout } = useAuth();

  if (loading) {
    return (
      <header className="devtalk-header">
        <div className="devtalk-header-content">로딩 중...</div>
      </header>
    );
  }

  const { authenticated, login: username, avatarUrl } = auth;

  return (
    <header className="devtalk-header">
      <div className="devtalk-header-content">
        {/* 로고 섹션 */}
        <div className="devtalk-logo">
          <div className="devtalk-logo-icon">
            <Code2 />
          </div>
          <div>
            <h1 className="devtalk-title">DevTalk</h1>
            <p className="devtalk-subtitle">Developer Community</p>
          </div>
        </div>

        {/* 로그인 상태일 때만 사용자 정보 표시 */}
        {authenticated && (
          <div className="devtalk-user">
            {/* 통합된 사용자 카드 */}
            <div className="devtalk-user-card">
              {avatarUrl ? (
                <img src={avatarUrl} alt="Avatar" className="devtalk-avatar" />
              ) : (
                <div className="devtalk-avatar-placeholder">
                  <Users />
                </div>
              )}
              <div className="devtalk-user-info">
                <p className="devtalk-username">{username}</p>
              </div>
            </div>

            <button
  className="devtalk-logout-btn"
  onClick={() => {
    console.log('로그아웃 버튼 클릭!');
    logout();
  }}
>
  <LogOut size={16} /> 로그아웃
</button>
          </div>
        )}
      </div>
    </header>
  );
};

export default DevTalkHeader;