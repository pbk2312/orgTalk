// src/lib/constants.ts

const origin = window.location.origin;

export const API_BASE_URL = process.env.REACT_APP_MAIN_SERVER_URL || '/api';
export const CHAT_BASE_URL = process.env.REACT_APP_CHAT_SERVER_URL || '/api/chat';
export const OAUTH_BASE_URL = process.env.REACT_APP_OAUTH_BASE_URL || origin;