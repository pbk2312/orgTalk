import React, { useState } from 'react';
import { Github, Users, MessageCircle, ArrowRight, Code2, GitBranch } from 'lucide-react';
import OrgTalkHeader from './OrgTalkHeader';
import '../css/LoginPage.css';

const LoginPage = () => {
  const [isHovered, setIsHovered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleGitHubLogin = () => {
    setIsLoading(true);
    setTimeout(() => {
      window.location.href = "http://localhost:8080/oauth2/authorization/github";
    }, 500);
  };

  return (
    <div className="login-page">
      <OrgTalkHeader
        username="Guest"
        orgName="Org을 선택해주세요"
        avatarUrl="https://via.placeholder.com/40"
      />

      {/* 배경 패턴 */}
      <div className="background-effects">
        <div className="bg-circle-1"></div>
        <div className="bg-circle-2"></div>
        <div className="bg-circle-3"></div>
      </div>

      {/* 메인 콘텐츠 */}
      <div className="main-content">
        <div className="login-container">
          {/* 로고 및 타이틀 섹션 */}
          <div className="logo-section">
            <div className="main-logo">
              <Code2 />
            </div>
            <h1 className="main-title">
              Org<span className="title-highlight">Talk</span>
            </h1>
            <p className="main-description">
              GitHub Organization 멤버들과 함께하는<br />
              <span className="description-highlight">개발자 커뮤니티 플랫폼</span>
            </p>
          </div>

          {/* 기능 소개 카드들 */}
          <div className="feature-cards">
            <div className="feature-card">
              <div className="feature-card-content">
                <div className="feature-icon blue">
                  <Users />
                </div>
                <div>
                  <h3 className="feature-title">Organization 기반</h3>
                  <p className="feature-description">같은 Org 멤버들과 소통</p>
                </div>
              </div>
            </div>
            <div className="feature-card">
              <div className="feature-card-content">
                <div className="feature-icon green">
                  <MessageCircle />
                </div>
                <div>
                  <h3 className="feature-title">실시간 채팅</h3>
                  <p className="feature-description">즉시 협업하고 아이디어 공유</p>
                </div>
              </div>
            </div>
            <div className="feature-card">
              <div className="feature-card-content">
                <div className="feature-icon purple">
                  <GitBranch />
                </div>
                <div>
                  <h3 className="feature-title">개발자 친화적</h3>
                  <p className="feature-description">GitHub과 완벽하게 연동</p>
                </div>
              </div>
            </div>
          </div>

          {/* 로그인 버튼 */}
          <div className="login-button-container">
            <button
              onClick={handleGitHubLogin}
              onMouseEnter={() => setIsHovered(true)}
              onMouseLeave={() => setIsHovered(false)}
              disabled={isLoading}
              className="github-login-btn"
            >
              <div className="btn-background"></div>
              {isLoading ? (
                <div className="loading-content">
                  <div className="loading-spinner"></div>
                  <span>연결 중...</span>
                </div>
              ) : (
                <div className="btn-content">
                  <Github />
                  <span>GitHub로 시작하기</span>
                  <ArrowRight className="btn-arrow" />
                </div>
              )}
            </button>
            <p className="login-description">
              GitHub 계정으로 로그인하여 소속 Organization의<br />
              전용 커뮤니티에 참여하세요
            </p>
          </div>

          {/* 추가 정보 */}
          <div className="status-indicator">
            <div className="status-badge">
              <div className="status-dot"></div>
              <span className="status-text">안전한 OAuth 인증</span>
            </div>
          </div>
        </div>
      </div>

      {/* 부가 장식적 요소들 */}
      <div className="bottom-decoration"></div>
    </div>
  );
};

export default LoginPage;
