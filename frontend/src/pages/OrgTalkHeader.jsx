import React from 'react';
import { Code2, Users, LogOut } from 'lucide-react';
import '../css/OrgTalkHeader.css';
import { useAuth } from '../hooks/useAuth.ts';

const OrgTalkHeader = () => {
  const { auth, loading, logout } = useAuth();

  console.log('🧐 OrgTalkHeader render — auth:', auth, ' logout:', logout);

  if (loading) {
    return (
      <header className="orgtalk-header">
        <div className="orgtalk-header-content">로딩 중...</div>
      </header>
    );
  }

  const { authenticated, login: username, avatarUrl } = auth;

  return (
    <header className="orgtalk-header">
      <div className="orgtalk-header-content">
        {/* 로고 섹션 */}
        <div className="orgtalk-logo">
          <div className="orgtalk-logo-icon">
            <Code2 />
          </div>
          <div>
            <h1 className="orgtalk-title">OrgTalk</h1>
            <p className="orgtalk-subtitle">GitHub Organization Community</p>
          </div>
        </div>

        {/* 로그인 상태일 때만 사용자 정보 표시 */}
        {authenticated && (
          <div className="orgtalk-user">
            {/* 통합된 사용자 카드 */}
            <div className="orgtalk-user-card">
              {avatarUrl ? (
                <img src={avatarUrl} alt="Avatar" className="orgtalk-avatar" />
              ) : (
                <div className="orgtalk-avatar-placeholder">
                  <Users />
                </div>
              )}
              <div className="orgtalk-user-info">
                <p className="orgtalk-username">{username}</p>
              </div>
            </div>

            <button
  className="orgtalk-logout-btn"
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

export default OrgTalkHeader;