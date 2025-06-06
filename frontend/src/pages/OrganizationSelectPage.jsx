// src/pages/OrganizationSelectPage.tsx
import React, { useState, useEffect } from 'react';
import { Code2, Users, ArrowRight, Loader2, Building2, Check, Sparkles, Zap } from 'lucide-react';
import '../css/OrganizationSelectPage.css';
import { getOrganizations } from '../service/MemberService';
import OrgTalkHeader from './OrgTalkHeader'; 
import { useNavigate } from 'react-router-dom';

// 플로팅 파티클 컴포넌트
const FloatingParticle = ({ delay = 0, size = 'small', color = 'blue' }) => {
  const colors = {
    blue: 'rgba(59, 130, 246, 0.3)',
    purple: 'rgba(139, 92, 246, 0.3)',
    pink: 'rgba(236, 72, 153, 0.3)'
  };

  return (
    <div 
      className="floating-particle"
      style={{
        position: 'absolute',
        width: size === 'small' ? '4px' : size === 'medium' ? '6px' : '8px',
        height: size === 'small' ? '4px' : size === 'medium' ? '6px' : '8px',
        background: colors[color],
        borderRadius: '50%',
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animation: `float ${8 + Math.random() * 4}s ease-in-out infinite`,
        animationDelay: `${delay}s`,
        pointerEvents: 'none'
      }}
    />
  );
};

// 조직 카드 컴포넌트
const OrganizationCard = ({ org, isSelected, isHovered, onSelect, onMouseEnter, onMouseLeave }) => {
  return (
    <div
      onClick={() => onSelect(org)}
      onMouseEnter={() => onMouseEnter(org.id)}
      onMouseLeave={() => onMouseLeave()}
      className={`org-card ${isSelected ? 'selected' : ''} ${isHovered ? 'hovered' : ''}`}
    >
      <div className="org-card-header">
        <div className="org-avatar">
          <img 
            src={org.avatarUrl} 
            alt={org.login}
            onError={(e) => {
              e.currentTarget.style.display = 'none';
              e.currentTarget.nextSibling.style.display = 'flex';
            }}
          />
          <div className="avatar-fallback">
            <Building2 size={20} />
          </div>
        </div>
        <div className="org-info">
          <h3 className="org-name">{org.login}</h3>
          <p className="org-id">Organization ID: {org.id}</p>
        </div>
        {isSelected && (
          <div className="selected-badge">
            <Check size={16} />
          </div>
        )}
      </div>
      <div className="org-card-footer">
        <Users size={16} />
        <span>멤버와 함께 소통하기</span>
      </div>
    </div>
  );
};

const OrganizationSelectPage = () => {
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrg, setSelectedOrg] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isJoining, setIsJoining] = useState(false);
  const [hoveredOrg, setHoveredOrg] = useState(null);
  const [particles, setParticles] = useState([]);

  const navigate = useNavigate(); 

  // 파티클 생성
  useEffect(() => {
    const generateParticles = () => {
      const newParticles = [];
      const colors = ['blue', 'purple', 'pink'];
      const sizes = ['small', 'medium', 'large'];
      
      for (let i = 0; i < 15; i++) {
        newParticles.push({
          id: i,
          size: sizes[Math.floor(Math.random() * sizes.length)],
          color: colors[Math.floor(Math.random() * colors.length)],
          delay: Math.random() * 5
        });
      }
      setParticles(newParticles);
    };

    generateParticles();
  }, []);

  useEffect(() => {
    const fetchOrgData = async () => {
      setIsLoading(true);
      try {
        const orgs = await getOrganizations();
        setOrganizations(orgs);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrgData();
  }, []);

  const handleOrgSelect = (org) => {
    setSelectedOrg(prev => (prev?.id === org.id ? null : org));
  };

  const handleJoinOrganization = () => {
    if (!selectedOrg) return;

    setIsJoining(true);
    setTimeout(() => {
      navigate(`/chat-rooms/${selectedOrg.id}`);
    }, 500);
  };

  return (
    <>
      {/* 페이지 최상단에 OrgTalkHeader */}
      <OrgTalkHeader />

      <div className="org-select-page">
        {/* 배경 효과 */}
        <div className="background-effects">
          <div className="bg-circle-1"></div>
          <div className="bg-circle-2"></div>
          <div className="bg-circle-3"></div>
          
          {/* 플로팅 파티클들 */}
          {particles.map(particle => (
            <FloatingParticle 
              key={particle.id} 
              size={particle.size} 
              color={particle.color}
              delay={particle.delay} 
            />
          ))}
        </div>

        {/* 메인 콘텐츠 */}
        <div className="main-content">
          <div className="org-select-container">
            {/* 헤더 섹션 */}
            <div className="header-section">
              <div className="main-logo">
                <Code2 size={40} />
              </div>
              <h1 className="main-title">
                조직을 선택<span className="title-highlight"></span>하세요
              </h1>
              <p className="main-description">
                참여하고 싶은 GitHub Organization을 선택하여<br />
                <span className="description-highlight">전용 커뮤니티</span>에 참여하세요
              </p>
            </div>

            {/* 로딩 상태 */}
            {isLoading && (
              <div className="loading-container">
                <Loader2 size={48} className="loading-icon" />
                <p className="loading-text">조직 목록을 불러오는 중...</p>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                  <Sparkles size={16} style={{ color: '#60a5fa', animation: 'pulse 1s ease-in-out infinite' }} />
                  <Sparkles size={16} style={{ color: '#a78bfa', animation: 'pulse 1s ease-in-out infinite 0.2s' }} />
                  <Sparkles size={16} style={{ color: '#ec4899', animation: 'pulse 1s ease-in-out infinite 0.4s' }} />
                </div>
              </div>
            )}

            {/* 조직 목록 */}
            {!isLoading && organizations.length > 0 && (
              <div className="organizations-grid">
                {organizations.map((org, index) => (
                  <div
                    key={org.id}
                    style={{ 
                      animationDelay: `${index * 0.1}s`,
                      animation: 'scale-in 0.6s ease-out both'
                    }}
                  >
                    <OrganizationCard
                      org={org}
                      isSelected={selectedOrg?.id === org.id}
                      isHovered={hoveredOrg === org.id}
                      onSelect={handleOrgSelect}
                      onMouseEnter={setHoveredOrg}
                      onMouseLeave={() => setHoveredOrg(null)}
                    />
                  </div>
                ))}
              </div>
            )}

            {/* 조직이 없는 경우 */}
            {!isLoading && organizations.length === 0 && (
              <div className="empty-state">
                <div style={{ position: 'relative', display: 'inline-block' }}>
                  <Building2 size={48} className="empty-icon" />
                  <Sparkles 
                    size={20} 
                    style={{ 
                      position: 'absolute', 
                      top: '-8px', 
                      right: '-8px',
                      color: '#fbbf24',
                      animation: 'pulse 2s ease-in-out infinite'
                    }} 
                  />
                </div>
                <h3 className="empty-title">참여 가능한 조직이 없습니다</h3>
                <p className="empty-description">
                  GitHub Organization에 먼저 참여한 후 다시 시도해주세요
                </p>
              </div>
            )}

            {/* 참여 버튼 */}
            {selectedOrg && (
              <div className="join-button-container">
                <button
                  onClick={handleJoinOrganization}
                  disabled={isJoining}
                  className={`join-button ${isJoining ? 'loading' : ''}`}
                >
                  {isJoining ? (
                    <>
                      <Loader2 size={20} className="btn-loading-icon" />
                      <span>참여 중...</span>
                    </>
                  ) : (
                    <>
                      <Building2 size={20} />
                      <span>{selectedOrg.login}에 참여하기</span>
                      <ArrowRight size={18} />
                    </>
                  )}
                </button>
              </div>
            )}

            {/* 상태 표시 */}
            <div className="status-indicator">
              <div className="status-badge">
                <div className="status-dot"></div>
                <span className="status-text">GitHub 연동 활성화</span>
                <Zap size={16} style={{ color: '#fbbf24' }} />
              </div>
            </div>
          </div>
        </div>

        {/* 하단 장식 */}
        <div className="bottom-decoration"></div>
      </div>
    </>
  );
};

export default OrganizationSelectPage;