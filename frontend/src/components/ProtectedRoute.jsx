// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.ts';
import LoadingScreen from './LoadingScreen';

const ProtectedRoute = () => {
  const { auth, loading } = useAuth();

  if (loading) {
    return <LoadingScreen message="인증 확인 중..." />;
  }

  if (!auth.authenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
