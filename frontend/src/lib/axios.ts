// src/lib/axios.ts
import axios from 'axios';

/** 토큰 / 리프레시 로직에 필요한 전역 변수들 */
let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (error: any) => void;
}> = [];

/** 인증(또는 토큰 갱신) 없이 호출할 엔드포인트 목록 */
const PUBLIC_ENDPOINTS = [
  '/api/auth/refresh',
  '/api/auth/login',
  '/api/auth/signUp',
];

/** 외부에서 토큰을 세팅할 수 있도록 export */
export function setAccessToken(token: string) {
  accessToken = token;
}

/** 1) 8080용 REST API 인스턴스 */
export const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

/** 2) 8081용 Chat API 전용 인스턴스 */
export const chatApi = axios.create({
  baseURL: 'http://localhost:8081',
  withCredentials: true,
});

/** 리프레시 전용 인스턴스 (인터셉터 미적용) */
const authApi = axios.create({
  baseURL: api.defaults.baseURL, // (리프레시 요청은 8080에서 처리된다고 가정)
  withCredentials: true,
});

/** 실제로 리프레시 토큰을 요청해서 새 액세스 토큰을 반환하는 함수 */
async function refreshToken(): Promise<string> {
  const { data } = await authApi.post('/api/auth/refresh');
  return data.accessToken;
}

/**
 * 공통 인터셉터를 한 번에 설정해주는 헬퍼 함수
 * @param instance axios.create(...)으로 생성된 인스턴스
 */
function attachInterceptors(instance: ReturnType<typeof axios.create>) {
  // --- Request 인터셉터: 토큰이 없으면 refresh 후 큐 처리, 있으면 Authorization 헤더 추가 ---
  instance.interceptors.request.use(
    async (config) => {
      const url = config.url ?? '';

      // 1) PUBLIC_ENDPOINTS인 경우, 토큰/큐 처리 없이 바로 통과
      if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
        return config;
      }

      // 2) accessToken이 비어 있고, 현재 refresh가 진행 중이 아니면
      if (!accessToken && !isRefreshing) {
        isRefreshing = true;
        try {
          const newToken = await refreshToken();
          setAccessToken(newToken);
          // 큐에 걸려 있던 요청들에 새로운 토큰을 전달
          failedQueue.forEach(({ resolve }) => resolve(newToken));
        } catch (err) {
          failedQueue.forEach(({ reject }) => reject(err));
          throw err;
        } finally {
          failedQueue = [];
          isRefreshing = false;
        }
      }

      // 3) accessToken이 있으면, Authorization 헤더 추가
      if (accessToken) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${accessToken}`;
      }
      // 4) 아직 토큰이 없고, refresh 중이라면 큐에 넣어서 토큰을 받은 뒤 헤더 추가
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

  // --- Response 인터셉터: 401이 오면 refresh 시도 후 재요청 ---
  instance.interceptors.response.use(
    (res) => res,
    async (error) => {
      const originalRequest = (error.config as any) ?? {};
      const url = originalRequest.url ?? '';

      // 1) “/api/auth/refresh” 에서 401 → 무조건 로그인 페이지로 리다이렉트
      if (
        error.response?.status === 401 &&
        url.endsWith('/api/auth/refresh')
      ) {
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // 2) PUBLIC_ENDPOINTS 로 지정된 URL은 재시도 대상이 아님
      if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
        return Promise.reject(error);
      }

      // 3) 그 외 401 에러 → _retry 플래그 없으면 토큰 리프레시 후 원래 요청 재시도
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;
        try {
          const newToken = await refreshToken();
          setAccessToken(newToken);
          originalRequest.headers = originalRequest.headers ?? {};
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return instance(originalRequest);
        } catch (err) {
          window.location.href = '/login';
          return Promise.reject(err);
        }
      }

      return Promise.reject(error);
    }
  );
}

// api와 chatApi 두 인스턴스 모두에 동일 인터셉터 적용
attachInterceptors(api);
attachInterceptors(chatApi);

/** 
 * 기존에 별도로 export default 하던 api 인스턴스 (8080용)
 * 필요하다면 `import { api, chatApi, setAccessToken } from '@/lib/axios'` 형태로 사용 가능합니다.
 */
export default api;
