// src/lib/axios.ts
import axios from 'axios';

let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (error: any) => void;
}> = [];

// 메모리에 토큰 세팅
export function setAccessToken(token: string) {
  accessToken = token;
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

// 토큰 리프레시 API 호출
async function refreshToken(): Promise<string> {
  const response = await axios.post(
    '/api/auth/refresh',
    {},
    { baseURL: api.defaults.baseURL, withCredentials: true }
  );
  return response.data.accessToken; // 실제 응답 구조에 맞게 조정하세요
}

// REQUEST 인터셉터
api.interceptors.request.use(
  async (config) => {
    // 토큰이 없고 리프레시 중이 아니면
    if (!accessToken && !isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await refreshToken();
        setAccessToken(newToken);
        // 대기 중인 요청들에 새 토큰 적용
        failedQueue.forEach(({ resolve }) => resolve(newToken));
      } catch (err) {
        failedQueue.forEach(({ reject }) => reject(err));
        throw err;
      } finally {
        failedQueue = [];
        isRefreshing = false;
      }
    }

    // 토큰이 이미 있으면 헤더에 추가
    if (accessToken) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    // 리프레시 중인 경우 큐에 보관했다가 나중에 헤더 추가
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

// RESPONSE 인터셉터: 401 에러 시 토큰 리프레시 후 재시도
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = (error.config as any);
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
