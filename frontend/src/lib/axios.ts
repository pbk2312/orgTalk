// src/lib/axios.ts
import axios from 'axios';
import { API_BASE_URL, CHAT_BASE_URL } from './constants.ts';


let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (error: any) => void }> = [];

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
      failedQueue.forEach(({ resolve }) => resolve(newToken));
      return newToken;
    } catch (err) {
      failedQueue.forEach(({ reject }) => reject(err));
      alert('세션이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = '/login';
      throw err;
    } finally {
      failedQueue = [];
      isRefreshing = false;
    }
  }
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
      if (!error.response) {
        alert('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
        window.location.href = '/server-error';
        return Promise.reject(error);
      }
      if (error.code === 'ECONNABORTED') {
        alert('요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.');
        window.location.href = '/server-error';
        return Promise.reject(error);
      }
      const status = error.response.status;
      const errorMessage = error.response.data?.message || '알 수 없는 오류가 발생했습니다.';
      if (status === 401) {
        alert(errorMessage);
        window.location.href = '/login';
      } else if (status === 400 || status === 404) {
        alert(errorMessage);
      } else if (status === 500) {
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        window.location.href = '/server-error';
      } else {
        alert(errorMessage);
      }
      return Promise.reject(error);
    }
  );
}

attachInterceptors(api);
attachInterceptors(chatApi);

export default api;
