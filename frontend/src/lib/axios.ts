import axios from 'axios';
import { API_BASE_URL, CHAT_BASE_URL, CHAT_ROOM_BASE_URL} from './constants.ts';

let accessToken = '';
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

let errorShown = false;
let refreshErrorShown = false;

const PUBLIC_ENDPOINTS = [
  '/auth/refresh',
  '/auth/login'
];

export function setAccessToken(token: string) {
  accessToken = token;
}

export async function getAccessToken(): Promise<string> {
  console.log('getAccessToken 호출됨');

  // 캐시된 액세스 토큰이 있으면 바로 반환
  if (accessToken) {
    console.log('캐시된 액세스 토큰이 있음:', accessToken);
    return accessToken;
  }

  // 리프레시 토큰을 갱신 중이 아니면 갱신 시작
  if (!isRefreshing) {
    console.log('리프레시 토큰 갱신 시작');
    isRefreshing = true;

    try {
      // 토큰 갱신 요청
      console.log('토큰 갱신 요청 중...');
      const response = await authApi.post('/auth/refresh');
      console.log('토큰 갱신 성공, 새로운 토큰:', response.data.accessToken);

      const newToken: string = response.data.accessToken;
      accessToken = newToken;

      // 실패한 요청이 있다면 새로운 토큰으로 해결
      if (failedQueue.length > 0) {
        console.log('실패한 요청들이 있음, 새로운 토큰으로 해결');
        failedQueue.forEach(({ resolve }) => resolve(newToken));
      }

      return newToken;
    } catch (err) {
      console.error('토큰 갱신 실패:', err);
      
      // 실패한 요청들에 대해 reject 처리
      if (failedQueue.length > 0) {
        console.log('실패한 요청들에 대해 reject 처리');
        failedQueue.forEach(({ reject }) => reject(err));
      }

      // 세션 만료 경고 한 번만 띄우기
      if (!refreshErrorShown) {
        console.log('세션 만료, 로그인 화면으로 리디렉션');
        alert('세션이 만료되었습니다. 다시 로그인해주세요.');
        refreshErrorShown = true;
      }

      // 로그인 화면으로 리디렉션
      window.location.href = '/login';
      throw err;
    } finally {
      console.log('토큰 갱신 처리 완료, 실패한 요청 큐 초기화');
      failedQueue = [];
      isRefreshing = false;
    }
  } else {
    console.log('이미 리프레시 토큰 갱신 중');
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

export const chatRoomApi = axios.create({
  baseURL: CHAT_ROOM_BASE_URL,  
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
        console.log(error.response?.status)
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
attachInterceptors(chatRoomApi); 

export function resetErrorShown() {
  errorShown = false;
  refreshErrorShown = false;
}

export default api;
