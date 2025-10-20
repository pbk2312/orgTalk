import React, { useState } from 'react';
import { Sparkles } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.ts';
import '../css/AIFloatingButton.css';

const AIFloatingButton = () => {
  const [isHovered, setIsHovered] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { auth, loading } = useAuth();

  // 로딩 중이거나 인증되지 않은 경우 숨김
  if (loading || !auth.authenticated) {
    return null;
  }

  // AI 멘토 페이지, 로그인 페이지, 채팅방에서는 버튼 숨김
  const hiddenPaths = ['/ai-mentor', '/login', '/oauth/callback'];
  const isChatRoom = location.pathname.startsWith('/chatroom/');
  
  if (hiddenPaths.includes(location.pathname) || isChatRoom) {
    return null;
  }

  const handleClick = () => {
    navigate('/ai-mentor');
  };

  return (
    <button
      className="ai-floating-button"
      onClick={handleClick}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      title="AI 멘토에게 질문하기"
    >
      <Sparkles className="ai-floating-icon" size={24} />
      {isHovered && (
        <span className="ai-floating-text">AI 멘토</span>
      )}
    </button>
  );
};

export default AIFloatingButton;

