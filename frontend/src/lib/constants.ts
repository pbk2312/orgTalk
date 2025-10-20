// src/lib/constants.ts

const origin = window.location.origin;

// Dev 환경에서는 백엔드 URL을 직접 지정, Prod 환경에서는 상대 경로 사용
export const API_BASE_URL = process.env.REACT_APP_MAIN_SERVER_URL || 
  (process.env.NODE_ENV === 'development' ? 'http://localhost:8080/api' : '/api');

export const CHAT_BASE_URL = process.env.REACT_APP_CHAT_SERVER_URL || 
  (process.env.NODE_ENV === 'development' ? 'http://localhost:8081/api/chat' : '/api/chat');

export const CHAT_ROOM_BASE_URL = process.env.REACT_APP_CHAT_ROOM_SERVER_URL || 
  (process.env.NODE_ENV === 'development' ? 'http://localhost:8081/api/chatroom' : '/api/chatroom');

// Dev 환경에서는 백엔드 URL을 직접 지정
// Prod 환경에서는 환경변수나 origin 사용
export const OAUTH_BASE_URL = process.env.REACT_APP_OAUTH_BASE_URL || 
  (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : origin);