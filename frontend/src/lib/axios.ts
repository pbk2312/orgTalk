// src/lib/axios.ts
import axios from 'axios';

let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (error: any) => void;
}> = [];

const PUBLIC_ENDPOINTS = [
  '/api/auth/refresh',
  '/api/auth/login',
  '/api/auth/signUp',
];

export function setAccessToken(token: string) {
  accessToken = token;
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

// 리프레시 전용 인스턴스 (인터셉터 미적용)
const authApi = axios.create({
  baseURL: api.defaults.baseURL,
  withCredentials: true,
});

async function refreshToken(): Promise<string> {
  const { data } = await authApi.post('/api/auth/refresh');
  return data.accessToken;
}

api.interceptors.request.use(
  async (config) => {
    const url = config.url ?? '';
    // 1) public endpoint인 경우, 토큰/큐 처리 없이 바로 통과
    if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
      return config;
    }
    // 2) 토큰이 비어 있고, 리프레시 중이 아니면 한 번만 시도
    if (!accessToken && !isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        failedQueue.forEach(({ resolve }) => resolve(newToken));
      } catch (err) {
        failedQueue.forEach(({ reject }) => reject(err));
        throw err;
      } finally {
        failedQueue = [];
        isRefreshing = false;
      }
    }
    // 3) 토큰이 있으면 헤더 추가
    if (accessToken) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    // 4) 리프레시 중이면 큐에 대기
    else if (isRefreshing) {
      await new Promise<string>((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then(token => {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      });
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const originalRequest = error.config as any;
    const url = originalRequest.url ?? '';

    // 리프레시 엔드포인트가 401 → 바로 로그인
    if (
      error.response?.status === 401 &&
      url.endsWith('/api/auth/refresh')
    ) {
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // public endpoint는 재시도 대상이 아님
    if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
      return Promise.reject(error);
    }

    // 기타 401 → 토큰 한 번 갱신 후 재시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        originalRequest.headers = originalRequest.headers ?? {};
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } catch (err) {
        window.location.href = '/login';
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
