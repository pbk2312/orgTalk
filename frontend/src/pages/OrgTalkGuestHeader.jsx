import React from 'react';
import { Code2 } from 'lucide-react';
import '../css/OrgTalkHeader.css';

/**
 * OrgTalkGuestHeader 컴포넌트
 * 로그인하지 않은 사용자를 위한 헤더 — 로고만 표시
 */
const OrgTalkGuestHeader = () => {
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
      </div>
    </header>
  );
};

export default OrgTalkGuestHeader;
