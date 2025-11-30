import axios from 'axios';
import {API_BASE_URL, CHAT_BASE_URL, CHAT_ROOM_BASE_URL} from './constants.ts';

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
  '/auth/login',
  '/auth/me'
];

export function setAccessToken(token: string) {
  accessToken = token;
}

export async function getAccessToken(): Promise<string> {
  console.log('getAccessToken 호출됨');

  // 로그인 페이지나 OAuth 콜백 페이지에서는 refresh를 시도하지 않음
  const currentPath = window.location.pathname;
  if (currentPath === '/login' || currentPath === '/oauth/callback') {
    console.log('로그인 페이지 또는 OAuth 콜백 페이지에서는 refresh 시도하지 않음');
    throw new Error('No access token available');
  }

  // 캐시된 액세스 토큰이 있으면 바로 반환
  if (accessToken) {
    console.log('캐시된 액세스 토큰이 있음');
    return accessToken;
  }

  // 리프레시 토큰으로 새 액세스 토큰 갱신
  if (!isRefreshing) {
    console.log('리프레시 토큰으로 액세스 토큰 갱신 시작');
    isRefreshing = true;

    try {
      // 리프레시 토큰은 HttpOnly 쿠키로 자동 전송됨
      const response = await authApi.post('/auth/refresh');
      console.log('액세스 토큰 갱신 성공');

      const newToken: string = response.data.accessToken;
      accessToken = newToken;
      // refresh 성공 시 에러 플래그 리셋 (다음 refresh 시도 가능하도록)
      refreshErrorShown = false;

      // 대기 중인 요청들 처리
      if (failedQueue.length > 0) {
        console.log(`${failedQueue.length}개의 대기 중인 요청 처리`);
        failedQueue.forEach(({resolve}) => resolve(newToken));
      }

      return newToken;
    } catch (err: any) {
      console.error('토큰 갱신 실패:', err);

      // 대기 중인 요청들 reject
      if (failedQueue.length > 0) {
        failedQueue.forEach(({reject}) => reject(err));
      }

      // 세션 만료 처리 (한 번만, 로그인 페이지가 아닐 때만, 실제 refresh token 만료일 때만)
      const isRefreshTokenExpired = err.response?.status === 401 || err.response?.status === 403;
      if (!refreshErrorShown && window.location.pathname !== '/login' && isRefreshTokenExpired) {
        console.log('세션 만료, 로그인 페이지로 이동');
        refreshErrorShown = true;
        // 네트워크 오류 등이 아닌 실제 인증 실패일 때만 로그인 페이지로 이동
        if (err.response) {
          alert('세션이 만료되었습니다. 다시 로그인해주세요.');
          window.location.href = '/login';
        }
      }

      throw err;
    } finally {
      failedQueue = [];
      isRefreshing = false;
    }
  } else {
    console.log('토큰 갱신 대기 중');
  }

  // 다른 요청이 갱신 중일 때 대기
  return new Promise<string>((resolve, reject) => {
    failedQueue.push({resolve, reject});
  });
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 15000,
});

export const chatApi = axios.create({
  baseURL: CHAT_BASE_URL,
  withCredentials: true,
  timeout: 15000,
});

export const chatRoomApi = axios.create({
  baseURL: CHAT_ROOM_BASE_URL,
  withCredentials: true,
  timeout: 15000,
});

const authApi = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 15000,
});


function attachInterceptors(instance: ReturnType<typeof axios.create>) {
  instance.interceptors.request.use(
      async (config) => {
        const url = config.url ?? '';
        const isPublicEndpoint = PUBLIC_ENDPOINTS.some(ep => url.endsWith(ep));

        // /auth/me는 토큰이 있으면 보내고, 없으면 refresh 시도 (로그인 페이지가 아닐 때만)
        if (url.endsWith('/auth/me')) {
          const currentPath = window.location.pathname;
          const isLoginPage = currentPath === '/login' || currentPath === '/oauth/callback';

          if (accessToken) {
            config.headers = config.headers ?? {};
            config.headers.Authorization = `Bearer ${accessToken}`;
          } else if (!isLoginPage && !refreshErrorShown) {
            // 로그인 페이지가 아니고, 에러가 표시되지 않았을 때 refresh 시도
            // isRefreshing이 true여도 getAccessToken()이 자동으로 대기 큐에 추가함
            try {
              const token = await getAccessToken();
              if (token) {
                config.headers = config.headers ?? {};
                config.headers.Authorization = `Bearer ${token}`;
              }
            } catch (err: any) {
              // refresh 실패해도 /auth/me는 계속 호출 (인증되지 않은 상태로)
              // refresh token이 실제로 만료된 경우 /auth/me가 401을 반환할 것이고,
              // 그때 useAuth에서 처리할 것임
              console.log('토큰 갱신 실패, 비인증 상태로 /auth/me 호출 시도');
              // refreshErrorShown을 true로 설정하지 않음 (실제 401 응답을 받아야 함)
            }
          }
          // 로그인 페이지에서는 토큰 없이 호출 (인증되지 않은 상태로 응답 받음)
          return config;
        }

        // 다른 public 엔드포인트는 토큰 없이 호출
        if (isPublicEndpoint) {
          return config;
        }

        // 그 외 엔드포인트는 토큰 필수
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
        const url = error.config?.url ?? '';

        // /auth/refresh 엔드포인트는 별도로 처리 (alert 중복 방지)
        if (url.endsWith('/auth/refresh')) {
          // refresh 실패는 getAccessToken()에서 이미 처리했으므로 여기서는 조용히 reject만
          return Promise.reject(error);
        }

        // 502 Bad Gateway 에러 처리 (백엔드 서버 연결 실패)
        if (status === 502) {
          if (!errorShown) {
            alert('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
            errorShown = true;
          }
          return Promise.reject(error);
        }

        if (!errorShown && (!error.response || status >= 500 || error.code === 'ECONNABORTED')) {
          alert('서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
          console.log(error.response?.status)
          errorShown = true;
        }

        if (status === 401) {
          // refresh 실패가 아닌 다른 401 에러만 처리
          if (!refreshErrorShown) {
            const msg = error.response.data?.message || '인증이 필요합니다.';
            alert(msg);
            refreshErrorShown = true;
          }
          // 로그인 페이지로 이동은 한 번만
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
          }
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
