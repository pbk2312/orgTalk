// src/pages/OrganizationSelectPage.tsx
import React, { useState, useEffect } from 'react';
import { Code2, Users, ArrowRight, Loader2, Building2, Check } from 'lucide-react';
import '../css/OrganizationSelectPage.css';
import { getOrganizations } from '../service/MemberService';
import OrgTalkHeader from './OrgTalkHeader'; 

const OrganizationSelectPage = () => {
  const [organizations, setOrganizations] = useState([]);
  const [selectedOrg, setSelectedOrg] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isJoining, setIsJoining] = useState(false);
  const [hoveredOrg, setHoveredOrg] = useState(null);

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
    // 실제 Join API 호출 또는 라우팅
    setTimeout(() => {
      console.log(`Joining organization: ${selectedOrg.login}`);
      // e.g. navigate(`/org/${selectedOrg.login}`);
      setIsJoining(false);
    }, 1500);
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
        </div>

        {/* 메인 콘텐츠 */}
        <div className="main-content">
          <div className="org-select-container">
            {/* (기존) 헤더 섹션 */}
            <div className="header-section">
              <div className="main-logo">
                <Code2 size={40} />
              </div>
              <h1 className="main-title">
                조직을 <span className="title-highlight">선택</span>하세요
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
              </div>
            )}

            {/* 조직 목록 */}
            {!isLoading && organizations.length > 0 && (
              <div className="organizations-grid">
                {organizations.map((org) => (
                  <div
                    key={org.id}
                    onClick={() => handleOrgSelect(org)}
                    onMouseEnter={() => setHoveredOrg(org.id)}
                    onMouseLeave={() => setHoveredOrg(null)}
                    className={`org-card ${selectedOrg?.id === org.id ? 'selected' : ''} ${hoveredOrg === org.id ? 'hovered' : ''}`}
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
                      {selectedOrg?.id === org.id && (
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
                ))}
              </div>
            )}

            {/* 조직이 없는 경우 */}
            {!isLoading && organizations.length === 0 && (
              <div className="empty-state">
                <Building2 size={48} className="empty-icon" />
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
