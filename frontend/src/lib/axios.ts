// src/lib/axios.ts
import axios from 'axios';

/** 메모리 전용 Access Token */
let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

/** 인증 없이 호출할 엔드포인트 (Refresh, Login, Signup) */
const PUBLIC_ENDPOINTS = [
  '/api/auth/refresh',
  '/api/auth/login',
  '/api/auth/signUp',
];

/**
 * 외부에서 로그인 성공 시 호출:
 * - 메모리 변수 accessToken에 현재 JWT를 세팅
 */
export function setAccessToken(token: string) {
  accessToken = token;
}

/**
 * getAccessToken(): 메모리에 토큰이 없으면 자동으로 refresh 호출
 * - (서버가 HttpOnly 쿠키에 저장한 refresh 토큰을 보고, 새로운 accessToken을 응답)
 */
export async function getAccessToken(): Promise<string> {
  // 1) 메모리에 이미 accessToken이 남아 있으면 바로 반환
  if (accessToken) {
    return accessToken;
  }

  // 2) 메모리에 없고, 아직 리프레시 작업이 진행 중이 아니면
  if (!accessToken && !isRefreshing) {
    isRefreshing = true;
    try {
      // 서버가 HTTP 쿠키(Refresh Token) 기반으로 accessToken을 새로 발급
      const response = await authApi.post('/api/auth/refresh');
      const newToken: string = response.data.accessToken;
      accessToken = newToken;

      // 큐에 보류 중이던 요청들에 새 토큰을 전달
      failedQueue.forEach(({ resolve }) => resolve(newToken));
      failedQueue = [];
      isRefreshing = false;

      return newToken;
    } catch (err) {
      // Refresh 자체가 401(유효하지 않음)이라면, 클라이언트 측 로그인 페이지로 리다이렉트
      window.location.href = '/login';
      failedQueue.forEach(({ reject }) => reject(err));
      failedQueue = [];
      isRefreshing = false;
      throw err;
    }
  }

  // 3) 이미 리프레시 중이면, 큐에 보류했다가 토큰 받아 반환
  return new Promise<string>((resolve, reject) => {
    failedQueue.push({ resolve, reject });
  });
}

/** 8080번 포트용 일반 API 인스턴스 */
export const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Refresh Token 쿠키를 자동으로 보내기 위함
});

/** 8081번 포트용 채팅(API) 인스턴스 */
export const chatApi = axios.create({
  baseURL: 'http://localhost:8081',
  withCredentials: true,
});

/** 리프레시 전용 인스턴스 (인터셉터 미적용) */
const authApi = axios.create({
  baseURL: api.defaults.baseURL,
  withCredentials: true,
});

/**
 * 인터셉터를 붙여주는 헬퍼
 */
function attachInterceptors(instance: ReturnType<typeof axios.create>) {
  // --- Request 인터셉터: accessToken 없으면 자동 refresh 후 헤더 추가 ---
  instance.interceptors.request.use(
    async (config) => {
      const url = config.url ?? '';

      // PUBLIC_ENDPOINTS는 그대로 통과
      if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
        return config;
      }

      // getAccessToken()이 메모리 없으면 refresh 호출하고, 토큰 세팅 후 resolve
      const token = await getAccessToken();
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // --- Response 인터셉터: 401 → 재시도는 getAccessToken()에서 이미 처리하므로 그대로 reject ---
  instance.interceptors.response.use(
    (res) => res,
    (error) => {
      // 만약 /api/auth/refresh 요청에서 401이 떴다면, 로그인으로 리다이렉트
      const originalRequest = error.config ?? {};
      const url = originalRequest.url ?? '';
      if (
        error.response?.status === 401 &&
        url.endsWith('/api/auth/refresh')
      ) {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );
}

// 두 인스턴스에 모두 인터셉터 붙이기
attachInterceptors(api);
attachInterceptors(chatApi);

export default api;
