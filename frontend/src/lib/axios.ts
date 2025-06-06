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
      const response = await authApi.post('/api/auth/refresh');
      const newToken: string = response.data.accessToken;
      accessToken = newToken;

      failedQueue.forEach(({ resolve }) => resolve(newToken));
      failedQueue = [];
      isRefreshing = false;

      return newToken;
    } catch (err) {
      // Refresh 자체에서 401이 뜨면 로그인 페이지로 이동
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = '/login';
      failedQueue.forEach(({ reject }) => reject(err));
      failedQueue = [];
      isRefreshing = false;
      throw err;
    }
  }

  // 3) 이미 리프레시 중이면 큐에 추가
  return new Promise<string>((resolve, reject) => {
    failedQueue.push({ resolve, reject });
  });
}

/** 8080번 포트용 일반 API 인스턴스 */
export const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
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
  instance.interceptors.request.use(
    async (config) => {
      const url = config.url ?? '';

      if (PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep))) {
        return config;
      }

      const token = await getAccessToken();
      if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  instance.interceptors.response.use(
    (res) => res,
    (error) => {
      const originalRequest = error.config ?? {};
      const status = error.response?.status;
      const data = error.response?.data;
      const errorMessage = data?.message || '알 수 없는 오류가 발생했습니다.';

      // 401: 인증 오류 시 alert 후 로그인 페이지로 이동
      if (status === 401) {
        alert(errorMessage);
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // 400, 404: 서버에서 내려준 메시지를 alert로 표시
      if (status === 400 || status === 404) {
        alert(errorMessage);
        return Promise.reject(error);
      }

      // 500: 서버 내부 오류 시 서버 에러 페이지로 이동
      if (status === 500) {
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        window.location.href = '/server-error';
        return Promise.reject(error);
      }

      // 기타 예외
      alert(errorMessage);
      return Promise.reject(error);
    }
  );
}

attachInterceptors(api);
attachInterceptors(chatApi);

export default api;
