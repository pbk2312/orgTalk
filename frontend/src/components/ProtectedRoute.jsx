// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.ts';

const ProtectedRoute = () => {
  const { auth, loading } = useAuth();

  if (loading) {
    // 로딩 스피너나 빈 화면 렌더링
    return <div>Loading...</div>;
  }

  // 인증되지 않은 경우 로그인 페이지로 리다이렉트
  if (!auth.authenticated) {
    return <Navigate to="/login" replace />;
  }

  // 인증된 경우 하위 라우트(Outlet)를 렌더링
  return <Outlet />;
};

export default ProtectedRoute;
