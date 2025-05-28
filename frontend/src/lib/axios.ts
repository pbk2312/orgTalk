import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',  // 모든 요청을 로컬 API 서버로 보냅니다.
  withCredentials: true,               // 쿠키 자동 전송
});

// 401 Unauthorized 받으면 로그인 페이지로 이동
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;