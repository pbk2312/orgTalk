// src/lib/axios.ts
import axios from 'axios';
import { API_BASE_URL, CHAT_BASE_URL } from './constants.ts';

let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

// 첫 번째 에러 알림을 이미 보였는지 여부
let errorShown = false;

/** 인증 없이 호출할 엔드포인트 (Refresh, Login, Signup) */
const PUBLIC_ENDPOINTS = [
  '/api/auth/refresh',
  '/api/auth/login',
  '/api/auth/signUp',
];

export function setAccessToken(token: string) {
  accessToken = token;
}

export async function getAccessToken(): Promise<string> {
  if (accessToken) return accessToken;

  if (!isRefreshing) {
    isRefreshing = true;
    try {
      const response = await authApi.post('/api/auth/refresh');
      const newToken: string = response.data.accessToken;
      accessToken = newToken;
      // 대기 중이던 요청들에 토큰 전달
      failedQueue.forEach(({ resolve }) => resolve(newToken));
      return newToken;
    } catch (err) {
      // 대기 중이던 요청들에 에러 전달
      failedQueue.forEach(({ reject }) => reject(err));
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = '/login';
      throw err;
    } finally {
      failedQueue = [];
      isRefreshing = false;
    }
  }

  // 토큰 갱신 중이라면 대기 큐에 추가
  return new Promise<string>((resolve, reject) => {
    failedQueue.push({ resolve, reject });
  });
}

/** 8080번 포트용 일반 API 인스턴스 */
export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 5000, // 5초 이상 응답 없으면 타임아웃
});

/** 8081번 포트용 채팅(API) 인스턴스 */
export const chatApi = axios.create({
  baseURL: CHAT_BASE_URL,
  withCredentials: true,
  timeout: 5000,
});

/** 리프레시 전용 인스턴스 (인터셉터 미적용) */
const authApi = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 5000,
});

/**
 * 인터셉터를 붙여주는 헬퍼
 */
function attachInterceptors(instance: ReturnType<typeof axios.create>) {
  // 요청 인터셉터: 토큰 자동 주입
  instance.interceptors.request.use(
    async (config) => {
      const url = config.url ?? '';
      // 공개 엔드포인트는 토큰 없이 호출
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
    (error) => Promise.reject(error),
  );

  // 응답 인터셉터: 에러 통합 처리
  instance.interceptors.response.use(
    (res) => res,
    (error) => {
      const status = error.response?.status;

      // 네트워크 에러/타임아웃/서버 에러(5xx) 시 한 번만 알림
      if (
        !errorShown &&
        (!error.response || status >= 500 || error.code === 'ECONNABORTED')
      ) {
        alert('서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
        errorShown = true;
      }

      // 인증 오류 처리
      if (status === 401) {
        const msg = error.response.data?.message || '인증이 필요합니다.';
        alert(msg);
        window.location.href = '/login';
      }
      // 클라이언트 요청 오류 처리 (실패 시 곧바로 알림)
      else if (status === 400 || status === 404) {
        const msg = error.response.data?.message || '요청 처리 중 오류가 발생했습니다.';
        alert(msg);
      }
      // 기타 알수없는 오류
      else if (status && status < 400) {
        const msg = error.response.data?.message || '알 수 없는 오류가 발생했습니다.';
        alert(msg);
      }

      return Promise.reject(error);
    },
  );
}

// 인터셉터 등록
attachInterceptors(api);
attachInterceptors(chatApi);

// 페이지 전환 시 또는 필요 시 errorShown 플래그 초기화
export function resetErrorShown() {
  errorShown = false;
}

export default api;
