// src/hooks/useAuth.ts
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom'; // 추가
import api from '../lib/axios.ts';

export interface MemberResponse {
  id: number;  
  login: string | null;
  avatarUrl: string | null;
  authenticated: boolean;
}

export function useAuth() {
  const [auth, setAuth] = useState<MemberResponse>({
    id: 0,
    login: null,
    avatarUrl: null,
    authenticated: false,
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate(); // 추가

  const fetchAuth = useCallback(async () => {
    try {
      const res = await api.get<MemberResponse>('/api/auth/me');
      setAuth(res.data);
    } catch (err: any) {
      if (err.response?.status === 401) {
        setAuth({ login: null, avatarUrl: null, authenticated: false });
      } else {
        console.error('Failed to fetch auth info:', err);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    await api.post('/api/auth/logout');
    setAuth({ login: null, avatarUrl: null, authenticated: false });
    navigate('/login'); // 로그아웃 후 리다이렉트
  }, [navigate]);

  useEffect(() => {
    fetchAuth();
  }, [fetchAuth]);

  return { auth, loading, logout };
}
