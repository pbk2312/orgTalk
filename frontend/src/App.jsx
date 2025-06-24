// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import OrganizationSelectPage from './pages/OrganizationSelectPage';
import ChatRoomsPage from './pages/ChatRoomsPage';
import ChatRoom from './pages/ChatRoom/ChatRoom';
import ServerErrorPage from './pages/ServerErrorPage';
import Error404Page from './pages/Error404Page';

function App() {
  return (
    <Router>
      <Routes>
        {/* 공개 페이지 */}
        <Route path="/login" element={<LoginPage />} />

        {/* ProtectedRoute 하위에 들어간 경로는 auth 검사 후 렌더링 */}
        <Route element={<ProtectedRoute />}>
          <Route path="/organizations" element={<OrganizationSelectPage />} />
          <Route path="/chat-rooms/:orgId" element={<ChatRoomsPage />} />
          <Route path="/chatroom/:roomId" element={<ChatRoom />} />
        </Route>

        {/* 기본 리디렉션 및 그 외 페이지 */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/server-error" element={<ServerErrorPage />} />
        <Route path="*" element={<Error404Page />} />
      </Routes>
    </Router>
  );
}

export default App;
