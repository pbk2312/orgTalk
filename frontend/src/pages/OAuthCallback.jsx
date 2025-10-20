import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { setAccessToken } from '../lib/axios.ts';

const OAuthCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    // URL에서 accessToken 추출
    const accessToken = searchParams.get('accessToken');
    const expiresIn = searchParams.get('expiresIn');

    if (accessToken) {
      console.log('OAuth 로그인 성공, Access Token 수신');
      
      // Access Token을 메모리 캐시에만 저장
      // Refresh Token은 이미 HttpOnly 쿠키로 저장되어 있음
      setAccessToken(accessToken);
      
      console.log('Access Token 저장 완료, 만료 시간:', expiresIn, '초');
      
      // Organizations 페이지로 리다이렉트
      navigate('/organizations', { replace: true });
    } else {
      console.error('URL에 Access Token이 없음');
      navigate('/login', { replace: true });
    }
  }, [searchParams, navigate]);

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      flexDirection: 'column',
      gap: '20px'
    }}>
      <div className="loading-spinner" style={{
        border: '4px solid #f3f3f3',
        borderTop: '4px solid #3498db',
        borderRadius: '50%',
        width: '50px',
        height: '50px',
        animation: 'spin 1s linear infinite'
      }} />
      <p>로그인 처리 중...</p>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default OAuthCallback;

