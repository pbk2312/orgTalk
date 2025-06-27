import React, { useState, useEffect } from 'react';
import { Github, Users, MessageCircle, ArrowRight, Code2, GitBranch, Sparkles, Zap, Globe } from 'lucide-react';
import '../css/LoginPage.css';
import throttle from 'lodash.throttle';


const FloatingParticle = ({ delay = 0, size = 'small' }) => {
  const sizeClasses = {
    small: 'particle-small',
    medium: 'particle-medium',
    large: 'particle-large'
  };

  return (
    <div 
      className={`floating-particle ${sizeClasses[size]}`}
      style={{
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${delay}s`,
        animationDuration: `${8 + Math.random() * 4}s`
      }}
    />
  );
};


const LoginPage = () => {
  const [isHovered, setIsHovered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [particles, setParticles] = useState([]);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });


  useEffect(() => {
    const generateParticles = () => {
      const newParticles = [];
      for (let i = 0; i < 20; i++) {
        newParticles.push({
          id: i,
          size: ['small', 'medium', 'large'][Math.floor(Math.random() * 3)],
          delay: Math.random() * 5
        });
      }
      setParticles(newParticles);
    };

    generateParticles();
  }, []);


  useEffect(() => {
  const handleMouseMove = throttle((e) => {
    setMousePosition({ x: e.clientX, y: e.clientY });
  }, 50); 

  window.addEventListener('mousemove', handleMouseMove);
  return () => window.removeEventListener('mousemove', handleMouseMove);
}, []);

  const handleGitHubLogin = () => {
    setIsLoading(true);
    setTimeout(() => {
      window.location.href = "http://localhost:8080/oauth2/authorization/github";
    }, 500);
  };

  const features = [
    { 
      icon: Users, 
      title: "Organization 기반", 
      desc: "같은 Org 멤버들과 소통", 
      colorClass: "feature-blue",
      delay: "0.1s" 
    },
    { 
      icon: MessageCircle, 
      title: "실시간 채팅", 
      desc: "즉시 협업하고 아이디어 공유", 
      colorClass: "feature-green",
      delay: "0.2s" 
    },
    { 
      icon: GitBranch, 
      title: "개발자 친화적", 
      desc: "GitHub과 완벽하게 연동", 
      colorClass: "feature-purple",
      delay: "0.3s" 
    }
  ];

  return (
    <div className="login-page">

      {/* 동적 배경 효과 */}
      <div className="background-effects">
        {/* 큰 원형 그라데이션 */}
        <div className="bg-gradient-1" />
        <div className="bg-gradient-2" />
        
        {/* 플로팅 파티클들 */}
        {particles.map(particle => (
          <FloatingParticle key={particle.id} size={particle.size} delay={particle.delay} />
        ))}
        
        {/* 마우스 따라다니는 그라데이션 */}
        <div 
          className="mouse-follower"
          style={{
            left: mousePosition.x - 128,
            top: mousePosition.y - 128,
          }}
        />
      </div>

      {/* 메인 콘텐츠 */}
      <div className="main-content">
        <div className="login-container">
          
          {/* 로고 섹션 */}
          <div className="logo-section">
            <div className="logo-wrapper">
              <div className="main-logo">
                <Code2 />
              </div>
              <div className="sparkle-icon">
                <Sparkles />
              </div>
            </div>
            
            <h1 className="main-title">
              OrgTalk<span className="title-highlight"></span>
            </h1>
            
            <p className="main-description">
              GitHub Organization 멤버들과 함께하는
            </p>
            <p className="main-description-highlight">
              개발자 커뮤니티 플랫폼
            </p>
          </div>

          {/* 기능 카드들 */}
          <div className="feature-cards">
            {features.map((feature, index) => (
              <div 
                key={index}
                className="feature-card"
                style={{ animationDelay: feature.delay }}
              >
                <div className="feature-card-content">
                  <div className={`feature-icon ${feature.colorClass}`}>
                    <feature.icon />
                  </div>
                  <div className="feature-text">
                    <h3 className="feature-title">{feature.title}</h3>
                    <p className="feature-description">{feature.desc}</p>
                  </div>
                </div>
              </div>
            ))}
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
              <div className="btn-background" />
              
              {isLoading ? (
                <div className="loading-content">
                  <div className="loading-spinner" />
                  <span>연결 중...</span>
                </div>
              ) : (
                <div className="btn-content">
                  <Github />
                  <span>GitHub로 시작하기</span>
                  <ArrowRight className={`btn-arrow ${isHovered ? 'hovered' : ''}`} />
                </div>
              )}
            </button>
            
            <p className="login-description">
              GitHub 계정으로 로그인하여 소속 Organization의<br />
              전용 커뮤니티에 참여하세요
            </p>
          </div>

          {/* 상태 표시 */}
          <div className="status-indicator">
            <div className="status-badge">
              <div className="status-dot" />
              <span className="status-text">안전한 OAuth 인증</span>
              <Zap className="zap-icon" />
            </div>
          </div>

          {/* 추가 장식 요소 */}
          <div className="decoration-globe">
            <Globe />
          </div>
        </div>
      </div>

      {/* 하단 그라데이션 라인 */}
      <div className="bottom-gradient-line" />
    </div>
  );
};

export default LoginPage;