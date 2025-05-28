// src/services/AuthService.jsx
import axios from "axios";

const API_BASE_URL = "http://localhost:8080"; 

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // JWT 쿠키 자동 포함
});

const AuthService = {
};

export default AuthService;