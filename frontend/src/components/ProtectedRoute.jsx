// src/components/ProtectedRoute.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth.ts';

const ProtectedRoute = () => {
  const { auth, loading } = useAuth();

  if (loading) {

    return <div>Loading...</div>;
  }


  if (!auth.authenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
