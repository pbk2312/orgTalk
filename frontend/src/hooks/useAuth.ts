// src/hooks/useAuth.ts
import { useState, useEffect, useCallback } from 'react';
import api from '../lib/axios.ts';

export interface MemberResponse {
  login: string | null;
  avatarUrl: string | null;
  authenticated: boolean;
}

export function useAuth() {
  const [auth, setAuth] = useState<MemberResponse>({
    login: null,
    avatarUrl: null,
    authenticated: false,
  });
  const [loading, setLoading] = useState(true);

  const fetchAuth = useCallback(async () => {
    try {
      const res = await api.get<MemberResponse>('/api/auth/me');
      setAuth(res.data);
    } catch {
      setAuth({ login: null, avatarUrl: null, authenticated: false });
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    await api.post('/api/auth/logout');
    setAuth({ login: null, avatarUrl: null, authenticated: false });
  }, []);

  useEffect(() => {
    fetchAuth();
  }, [fetchAuth]);

  return { auth, loading, logout };
}
