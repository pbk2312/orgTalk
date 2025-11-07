import React, { useState, useEffect } from 'react';
import { Code2, Users, ArrowRight, Loader2, Building2, Check, Sparkles, Zap, Plus } from 'lucide-react';
import '../css/OrganizationSelectPage.css';
import { getOrganizations } from '../service/MemberService';
import { createOrganization } from '../service/OrganizationService';
import { setAccessToken } from '../lib/axios.ts';
import OrgTalkHeader from './OrgTalkHeader';
import { useNavigate } from 'react-router-dom';
import Pagination from './Pagination';


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


const OrganizationCard = ({ org, isSelected, isHovered, onSelect, onMouseEnter, onMouseLeave }) => (
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

const OrganizationSelectPage = () => {
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrg, setSelectedOrg] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isJoining, setIsJoining] = useState(false);
  const [hoveredOrg, setHoveredOrg] = useState(null);
  const [particles, setParticles] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(3);
  const [totalPages, setTotalPages] = useState(0);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newOrgName, setNewOrgName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  const navigate = useNavigate();


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
        const data = await getOrganizations(currentPage, pageSize);
        setOrganizations(data.content);
        setTotalPages(data.totalPages);
      } catch (error) {
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrgData();
  }, [currentPage, pageSize]);

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

  const handleCreateOrganization = async () => {
    if (!newOrgName.trim()) {
      setCreateError('조직 이름을 입력해주세요');
      return;
    }

    if (newOrgName.length < 2 || newOrgName.length > 50) {
      setCreateError('조직 이름은 2자 이상 50자 이하여야 합니다');
      return;
    }

    setIsCreating(true);
    setCreateError('');

    try {
      const response = await createOrganization(newOrgName);
      
      // 새로운 Access Token을 메모리 캐시에 저장
      // Refresh Token은 서버가 자동으로 HttpOnly 쿠키로 갱신함
      if (response.accessToken) {
        setAccessToken(response.accessToken);
        console.log('조직 생성 완료, 새로운 Access Token 적용');
      }
      
      // 생성 성공 시 조직 목록 새로고침 (업데이트된 토큰으로 호출)
      const data = await getOrganizations(0, pageSize);
      setOrganizations(data.content);
      setTotalPages(data.totalPages);
      setCurrentPage(0);
      setNewOrgName('');
      setShowCreateForm(false);
      
      // 생성된 조직 자동 선택
      setSelectedOrg({
        id: response.id,
        login: response.login,
        avatarUrl: response.avatarUrl
      });
    } catch (error) {
      console.error('Failed to create organization:', error);
      setCreateError('조직 생성에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <>
      <OrgTalkHeader />
      <div className="org-select-page">
        {/* 배경 효과 */}
        <div className="background-effects">
          <div className="bg-circle-1"></div>
          <div className="bg-circle-2"></div>
          <div className="bg-circle-3"></div>
          {particles.map(p => (
            <FloatingParticle 
              key={p.id} 
              size={p.size} 
              color={p.color}
              delay={p.delay} 
            />
          ))}
        </div>

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
            {isLoading ? (
              <div className="loading-container">
                <Loader2 size={48} className="loading-icon" />
                <p className="loading-text">조직 목록을 불러오는 중...</p>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                  <Sparkles size={16} style={{ color: '#60a5fa', animation: 'pulse 1s ease-in-out infinite' }} />
                  <Sparkles size={16} style={{ color: '#a78bfa', animation: 'pulse 1s ease-in-out infinite 0.2s' }} />
                  <Sparkles size={16} style={{ color: '#ec4899', animation: 'pulse 1s ease-in-out infinite 0.4s' }} />
                </div>
              </div>
            ) : (
              <>
                {/* 조직 목록 */}
                {organizations.length > 0 ? (
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
                ) : (
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
                      GitHub Organization에 먼저 참여하거나<br />
                      테스트용 조직을 생성해보세요
                    </p>

                    {!showCreateForm ? (
                      <button
                        onClick={() => setShowCreateForm(true)}
                        className="create-org-button"
                        style={{
                          marginTop: '1.5rem',
                          padding: '0.75rem 1.5rem',
                          background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                          color: 'white',
                          border: 'none',
                          borderRadius: '12px',
                          fontSize: '0.95rem',
                          fontWeight: '600',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.5rem',
                          transition: 'all 0.3s ease',
                          boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
                          margin: '1.5rem auto 0'
                        }}
                        onMouseOver={(e) => {
                          e.currentTarget.style.transform = 'translateY(-2px)';
                          e.currentTarget.style.boxShadow = '0 6px 20px rgba(59, 130, 246, 0.4)';
                        }}
                        onMouseOut={(e) => {
                          e.currentTarget.style.transform = 'translateY(0)';
                          e.currentTarget.style.boxShadow = '0 4px 12px rgba(59, 130, 246, 0.3)';
                        }}
                      >
                        <Plus size={20} />
                        <span>테스트 조직 만들기</span>
                      </button>
                    ) : (
                      <div 
                        className="create-org-form"
                        style={{
                          marginTop: '1.5rem',
                          padding: '1.5rem',
                          background: 'rgba(255, 255, 255, 0.05)',
                          borderRadius: '16px',
                          border: '1px solid rgba(255, 255, 255, 0.1)',
                          maxWidth: '400px',
                          margin: '1.5rem auto 0'
                        }}
                      >
                        <input
                          type="text"
                          value={newOrgName}
                          onChange={(e) => {
                            setNewOrgName(e.target.value);
                            setCreateError('');
                          }}
                          placeholder="조직 이름 입력 (예: MyTestOrg)"
                          disabled={isCreating}
                          style={{
                            width: '100%',
                            padding: '0.75rem 1rem',
                            background: 'rgba(255, 255, 255, 0.05)',
                            border: '1px solid rgba(255, 255, 255, 0.1)',
                            borderRadius: '8px',
                            color: 'white',
                            fontSize: '0.95rem',
                            marginBottom: '0.75rem',
                            outline: 'none',
                            transition: 'all 0.3s ease'
                          }}
                          onFocus={(e) => {
                            e.currentTarget.style.border = '1px solid #3b82f6';
                            e.currentTarget.style.background = 'rgba(255, 255, 255, 0.08)';
                          }}
                          onBlur={(e) => {
                            e.currentTarget.style.border = '1px solid rgba(255, 255, 255, 0.1)';
                            e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                          }}
                        />
                        
                        {createError && (
                          <p style={{
                            color: '#ef4444',
                            fontSize: '0.875rem',
                            marginBottom: '0.75rem',
                            textAlign: 'left'
                          }}>
                            {createError}
                          </p>
                        )}

                        <div style={{ display: 'flex', gap: '0.75rem' }}>
                          <button
                            onClick={handleCreateOrganization}
                            disabled={isCreating}
                            style={{
                              flex: 1,
                              padding: '0.75rem',
                              background: isCreating 
                                ? 'rgba(59, 130, 246, 0.5)' 
                                : 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                              color: 'white',
                              border: 'none',
                              borderRadius: '8px',
                              fontSize: '0.95rem',
                              fontWeight: '600',
                              cursor: isCreating ? 'not-allowed' : 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              gap: '0.5rem',
                              transition: 'all 0.3s ease'
                            }}
                          >
                            {isCreating ? (
                              <>
                                <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} />
                                <span>생성 중...</span>
                              </>
                            ) : (
                              <>
                                <Check size={18} />
                                <span>생성</span>
                              </>
                            )}
                          </button>

                          <button
                            onClick={() => {
                              setShowCreateForm(false);
                              setNewOrgName('');
                              setCreateError('');
                            }}
                            disabled={isCreating}
                            style={{
                              padding: '0.75rem 1.25rem',
                              background: 'rgba(255, 255, 255, 0.05)',
                              color: 'rgba(255, 255, 255, 0.7)',
                              border: '1px solid rgba(255, 255, 255, 0.1)',
                              borderRadius: '8px',
                              fontSize: '0.95rem',
                              fontWeight: '600',
                              cursor: isCreating ? 'not-allowed' : 'pointer',
                              transition: 'all 0.3s ease'
                            }}
                          >
                            취소
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* 페이징 */}
                {totalPages > 1 && (
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                  />
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
              </>
            )}
          </div>
        </div>
        <div className="bottom-decoration"></div>
      </div>
    </>
  );
};

export default OrganizationSelectPage;
