import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'; 
import './App.css';
import LoginPage from './pages/LoginPage'; 
import OrganizationSelectPage from './pages/OrganizationSelectPage';

function App() {
  return (
    <Router>
      <Routes>
        {/* 로그인 페이지 */}
        <Route path="/login" element={<LoginPage />} />

        {/* 조직 선택 페이지 */}
        <Route path="/organizations" element={<OrganizationSelectPage />} />

        {/* 루트 접속 시 /login으로 리다이렉트(원하시면) */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* 그 외 404 처리 */}
        <Route path="*" element={<div>페이지를 찾을 수 없습니다</div>} />
      </Routes>
    </Router>
  );
}

export default App;
