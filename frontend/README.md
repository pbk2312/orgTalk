# DevTalk Frontend

DevTalk의 프론트엔드 애플리케이션입니다. React 기반의 SPA(Single Page Application)로, 실시간 채팅과 AI 멘토링 기능을 제공합니다.

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [프로젝트 구조](#프로젝트-구조)
- [설치 및 실행](#설치-및-실행)
- [환경 변수](#환경-변수)
- [빌드 및 배포](#빌드-및-배포)

## 📖 프로젝트 개요

DevTalk Frontend는 GitHub OAuth2 기반의 실시간 채팅 플랫폼의 사용자 인터페이스를 제공합니다.
WebSocket을 통한 실시간 채팅, OpenAI GPT-3.5 Turbo 기반 AI 멘토 기능, 그리고 반응형 디자인을 특징으로 합니다.

## 🛠 기술 스택

### 핵심 라이브러리

- **React 19.1.0** - UI 라이브러리
- **React Router DOM 6.30.1** - 클라이언트 사이드 라우팅

### 통신

- **Axios 1.9.0** - HTTP 클라이언트
- **@stomp/stompjs 7.1.1** - WebSocket/STOMP 클라이언트
- **SockJS-client 1.6.1** - WebSocket 폴백 지원

### UI/UX

- **Framer Motion 12.14.0** - 애니메이션
- **React Syntax Highlighter 15.6.1** - 코드 하이라이팅
- **Emoji Picker React 4.15.3** - 이모지 선택기
- **React Toastify 11.0.2** - 알림 토스트

### 기타

- **Nginx** - 프로덕션 빌드 서빙

## ✨ 주요 기능

### 1. 인증

- GitHub OAuth2 로그인
- JWT 토큰 기반 인증
- 자동 토큰 갱신

### 2. 실시간 채팅

- WebSocket/STOMP를 통한 실시간 메시징
- 채팅 히스토리 무한 스크롤
- 사용자 입장/퇴장 알림
- 이모지 지원
- 코드 블록 하이라이팅

### 3. 채팅방 관리

- 채팅방 생성/수정/삭제
- 공개/비공개 채팅방 설정
- 채팅방 검색 및 필터링
- 멤버 관리 및 강퇴 기능

### 4. AI 멘토 시스템 🤖

- **OpenAI GPT-3.5 Turbo** 기반 개발 멘토
- 전용 AI 멘토 페이지 (`/ai-mentor`)
- 플로팅 버튼으로 모든 페이지에서 접근 가능
- 실시간 질문/답변
- 코드 예제 제공
- 한국어 완벽 지원
- 토큰 사용량 표시

### 5. 반응형 디자인

- 모바일/태블릿/데스크톱 최적화
- 다크 모드 지원
- 부드러운 애니메이션 (Framer Motion)

## 📁 프로젝트 구조

```
frontend/
├── public/                      # 정적 파일
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── App.jsx                  # 메인 앱 컴포넌트
│   ├── index.js                 # 엔트리 포인트
│   ├── components/              # 공통 컴포넌트
│   │   ├── AIFloatingButton.jsx    # AI 멘토 플로팅 버튼
│   │   ├── LoadingScreen.jsx       # 로딩 화면
│   │   └── ProtectedRoute.jsx      # 인증 라우트
│   ├── pages/                   # 페이지 컴포넌트
│   │   ├── LoginPage.jsx           # 로그인 페이지
│   │   ├── ChatRoomsPage.jsx       # 채팅방 목록
│   │   ├── AIMentorPage.jsx        # AI 멘토 페이지 🤖
│   │   ├── Error404Page.jsx        # 404 에러
│   │   └── ChatRoom/               # 채팅방 관련
│   │       ├── ChatRoom.jsx
│   │       ├── ChatHeader.jsx
│   │       ├── ChatMessages.jsx
│   │       └── ChatInput.jsx
│   ├── service/                 # API 서비스
│   │   ├── api.js                  # Axios 인스턴스
│   │   ├── authService.js          # 인증 API
│   │   ├── chatService.js          # 채팅 API
│   │   ├── chatRoomService.js      # 채팅방 API
│   │   └── aiService.js            # AI 멘토 API 🤖
│   ├── hooks/                   # 커스텀 훅
│   │   ├── useAuth.js              # 인증 훅
│   │   ├── useWebSocket.js         # WebSocket 훅
│   │   └── useChatRoom.js          # 채팅방 훅
│   ├── util/                    # 유틸리티
│   │   └── tokenManager.js         # JWT 토큰 관리
│   ├── css/                     # 스타일시트
│   │   ├── AIMentorPage.module.css # AI 멘토 스타일 🤖
│   │   └── ...
│   └── constants/               # 상수
│       └── chatConstants.js
├── Dockerfile                   # 도커 이미지 빌드
├── nginx.conf                   # Nginx 설정
└── package.json                 # 프로젝트 의존성
```

## 🚀 설치 및 실행

### 필수 요구사항

- Node.js 18 이상
- npm 또는 yarn

### 개발 모드 실행

1. **의존성 설치**

```bash
npm install
```

2. **환경 변수 설정** (`.env` 파일 생성)

```env
REACT_APP_MAIN_SERVER_URL=http://localhost:8080
REACT_APP_CHAT_SERVER_URL=http://localhost:8081
REACT_APP_CHAT_WS_URL=ws://localhost:8081/api/chat/ws-stomp
```

3. **개발 서버 실행**

```bash
npm start
```

4. **브라우저에서 접속**

```
http://localhost:3000
```

## 🌐 환경 변수

### 개발 환경 (`.env`)

```env
REACT_APP_MAIN_SERVER_URL=http://localhost:8080
REACT_APP_CHAT_SERVER_URL=http://localhost:8081
REACT_APP_CHAT_WS_URL=ws://localhost:8081/api/chat/ws-stomp
```

### 프로덕션 환경 (`.env.production`)

```env
REACT_APP_MAIN_SERVER_URL=https://api.orgtalk.shop
REACT_APP_CHAT_SERVER_URL=https://chat.orgtalk.shop
REACT_APP_CHAT_WS_URL=wss://chat.orgtalk.shop/api/chat/ws-stomp
```

## 📦 빌드 및 배포

### 프로덕션 빌드

```bash
npm run build
```

빌드된 파일은 `build/` 디렉토리에 생성됩니다.

### Docker 빌드

```bash
docker build -t devtalk-frontend .
docker run -p 80:80 devtalk-frontend
```

### Nginx 설정

프로덕션 환경에서는 Nginx를 사용하여 정적 파일을 서빙합니다.

- React Router의 히스토리 모드 지원
- API 요청 프록시
- Gzip 압축

## 🎨 주요 페이지

### 로그인 페이지 (`/login`)

- GitHub OAuth2 로그인 버튼
- 애니메이션 효과

### 채팅방 목록 (`/chatrooms`)

- 조직별 채팅방 목록
- 검색 및 필터링
- 채팅방 생성/수정/삭제
- AI 멘토 플로팅 버튼 🤖

### 채팅방 (`/chatroom/:roomId`)

- 실시간 메시징
- 무한 스크롤
- 이모지 및 코드 블록 지원
- 멤버 목록 및 관리

### AI 멘토 페이지 (`/ai-mentor`) 🤖

- **OpenAI GPT-3.5 Turbo** 기반 대화
- 개발 관련 질문/답변
- 코드 예제 하이라이팅
- 한국어 지원
- 토큰 사용량 표시
- 대화 히스토리 관리

## 🔧 주요 스크립트

```bash
# 개발 서버 실행
npm start

# 프로덕션 빌드
npm run build

# 테스트 실행
npm test

# 코드 포맷팅
npm run format

# 린트 검사
npm run lint
```

## 🌟 핵심 기능 구현

### WebSocket 연결

```javascript
// hooks/useWebSocket.js
const client = new Client({
  brokerURL: CHAT_WS_URL,
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`
  },
  onConnect: () => {
    // 채팅 메시지 구독
    client.subscribe(`/topic/chat.${roomId}`, callback);
    // Presence 구독
    client.subscribe(`/topic/presence.${roomId}`, callback);
  }
});
```

### AI 멘토 API 호출

```javascript
// service/aiService.js
export const askAIMentor = async (question) => {
  const response = await api.post('/api/ai/chat', {question});
  return response.data; // { answer, totalTokens }
};
```

### JWT 토큰 자동 갱신

```javascript
// util/tokenManager.js
api.interceptors.response.use(
    response => response,
    async error => {
      if (error.response?.status === 401) {
        // Refresh Token으로 재발급
        await refreshAccessToken();
        // 원래 요청 재시도
        return api.request(error.config);
      }
    }
);
```

## 🎯 성능 최적화

- **코드 스플리팅**: React.lazy()로 페이지별 분할 로딩
- **메모이제이션**: React.memo, useMemo, useCallback 활용
- **이미지 최적화**: WebP 포맷 및 Lazy Loading
- **번들 사이즈 최적화**: Tree Shaking 및 압축

## 📝 라이선스

이 프로젝트는 비공개 프로젝트입니다.

## 👥 기여

프로젝트 기여에 대한 문의는 프로젝트 관리자에게 연락해주세요.
