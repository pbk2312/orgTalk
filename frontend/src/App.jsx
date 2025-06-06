// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

import LoginPage from './pages/LoginPage';
import OrganizationSelectPage from './pages/OrganizationSelectPage';
import ChatRoomsPage from './pages/ChatRoomsPage';
import ChatRoom from './pages/ChatRoom'; 
import ServerErrorPage from './pages/ServerErrorPage';
import Error404Page from './pages/Error404Page'; 


function App() {
  return (
    <Router>
      <Routes>
        {/* 로그인 페이지 */}
        <Route path="/login" element={<LoginPage />} />

        {/* 조직 선택 페이지 */}
        <Route path="/organizations" element={<OrganizationSelectPage />} />

        {/* 채팅방 목록 페이지 (orgId 파라미터 포함) */}
        <Route path="/chat-rooms/:orgId" element={<ChatRoomsPage />} />
           <Route 
          path="/chatroom/:roomId" 
          element={<ChatRoom />} 
        />

        {/* 루트 접속 시 /login으로 리다이렉트 */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* 그 외 404 처리 */}
        <Route path="*" element={<Error404Page />} />
        <Route path="/server-error" element={<ServerErrorPage />} /> {/* 서버 에러(500) 전용 */}
      </Routes>
    </Router>
  );
}

export default App;
