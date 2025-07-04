import axios from 'axios';
import { API_BASE_URL, CHAT_BASE_URL } from './constants.ts';

let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

let errorShown = false;
let refreshErrorShown = false;

const PUBLIC_ENDPOINTS = [
  '/api/auth/refresh',
  '/api/auth/login'
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
      if (!refreshErrorShown) {
        alert('세션이 만료되었습니다. 다시 로그인해주세요.');
        refreshErrorShown = true;
      }
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

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 5000,
});

export const chatApi = axios.create({
  baseURL: CHAT_BASE_URL,
  withCredentials: true,
  timeout: 5000,
});

const authApi = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 5000,
});

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
    (error) => Promise.reject(error),
  );

  instance.interceptors.response.use(
    (res) => {
      errorShown = false;
      return res;
    },
    (error) => {
      const status = error.response?.status;

      if (!errorShown && (!error.response || status >= 500 || error.code === 'ECONNABORTED')) {
        alert('서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
        errorShown = true;
      }

      if (status === 401) {
        if (!refreshErrorShown) {
          const msg = error.response.data?.message || '인증이 필요합니다.';
          alert(msg);
        }
        window.location.href = '/login';
      } else if (status === 400 || status === 404) {
        const msg = error.response.data?.message || '요청 처리 중 오류가 발생했습니다.';
        alert(msg);
      } else if (status && status < 400) {
        const msg = error.response.data?.message || '알 수 없는 오류가 발생했습니다.';
        alert(msg);
      }

      return Promise.reject(error);
    },
  );
}

attachInterceptors(api);
attachInterceptors(chatApi);

export function resetErrorShown() {
  errorShown = false;
  refreshErrorShown = false;
}

export default api;
