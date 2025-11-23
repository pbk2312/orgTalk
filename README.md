# DevTalk

GitHub 계정 기반 실시간 채팅 플랫폼

## 📖 프로젝트 소개

DevTalk은 GitHub 계정으로 로그인하여 사용할 수 있는 실시간 채팅 서비스입니다. 사용자는 GitHub OAuth2를 통해 로그인하고, 채팅방을 생성하고 참여하여 실시간으로 소통할 수 있습니다.

### 아키텍처

- **마이크로서비스 아키텍처**: `main-server`와 `chat-server`로 분리
- **Frontend** (포트 80/443): React 기반 SPA, Nginx로 서빙
- **Main Server** (포트 8080): 인증, 멤버 관리
- **Chat Server** (포트 8081): 실시간 채팅 및 채팅방 관리
ㄱ
## 🛠 기술 스택

### Frontend
- **React 19.1.0**
- **React Router DOM 6.30.1**
- **Axios 1.9.0** (HTTP 클라이언트)
- **@stomp/stompjs 7.1.1** (WebSocket/STOMP 클라이언트)
- **SockJS-client 1.6.1** (WebSocket 폴백)
- **Framer Motion 12.14.0** (애니메이션)
- **React Syntax Highlighter** (코드 하이라이팅)
- **Emoji Picker React** (이모지 선택)
- **React Toastify** (알림)
- **Nginx** (프로덕션 빌드 서빙)

### Backend
- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Security** + **OAuth2 Client** (GitHub)
- **Spring WebSocket** / **STOMP** (실시간 채팅)
- **Spring WebFlux** / **WebClient** (비동기 HTTP 통신)
- **Spring Data JPA** (MySQL)
- **Spring Data MongoDB**
- **Spring Data Redis**

### Database
- **MySQL**: 멤버, 채팅방 메타데이터
- **MongoDB**: 채팅 메시지 저장
- **Redis**: Refresh Token 저장, 사용자 Presence 정보

### 기타
- **JWT** (Access Token + Refresh Token)
- **Resilience4j** (Circuit Breaker)
- **Prometheus** + **Spring Actuator** (모니터링)
- **Docker** + **Docker Compose**
- **Gradle**

## 📁 패키지 구조

### Main Server
```
main-server/
└── src/main/java/yuhan/pro/mainserver/
    ├── domain/
    │   ├── auth/              # 인증 도메인
    │   │   ├── controller/
    │   │   ├── service/
    │   │   └── constants/
    │   ├── member/            # 멤버 도메인
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── entity/
    │   │   └── repository/
    │   ├── organization/      # 조직 도메인
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── entity/
    │   │   └── repository/
    │   └── ai/                # AI 채팅 도메인
    │       ├── controller/
    │       ├── service/
    │       └── dto/
    ├── core/                  # 보안 설정, CORS 설정
    └── sharedkernel/          # 공통 모듈
        ├── security/          # JWT 인증/인가
        ├── jwt/               # JWT 토큰 관리
        ├── exception/         # 예외 처리
        └── infra/             # Redis 설정
```

### Chat Server
```
chat-server/
└── src/main/java/yuhan/pro/chatserver/
    ├── domain/
    │   ├── controller/        # ChatController, ChatRoomController
    │   ├── service/           # ChatService, ChatRoomService
    │   ├── entity/            # Chat, ChatRoom, ChatRoomMember
    │   ├── repository/        # JPA, MongoDB Repository
    │   └── dto/
    ├── core/                  # WebClient 설정, MemberClient
    └── sharedkernel/
        ├── infra/
        │   ├── socket/         # WebSocket/STOMP 설정
        │   └── redis/         # Redis 설정
        └── jwt/               # JWT 인증
```

## 📚 API 명세

### 인증 (Main Server - `/api/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/me` | 현재 로그인한 사용자 정보 조회 |
| POST | `/api/auth/refresh` | Access Token 재발급 |
| POST | `/api/auth/logout` | 로그아웃 |

### 멤버 (Main Server - `/api/member`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/member/chatMembers` | 채팅방 멤버 정보 조회 |
| GET | `/api/member/profile-url` | 멤버 프로필 URL 조회 |

### 채팅방 (Chat Server - `/api/chatroom`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chatroom/create` | 채팅방 생성 |
| GET | `/api/chatroom/list` | 채팅방 목록 조회 (페이징) |
| GET | `/api/chatroom/search` | 채팅방 검색 |
| GET | `/api/chatroom/{roomId}` | 채팅방 정보 조회 |
| POST | `/api/chatroom/{roomId}/join` | 채팅방 입장 |
| POST | `/api/chatroom/{roomId}/delete` | 채팅방 삭제 |
| PATCH | `/api/chatroom/{roomId}/update` | 채팅방 수정 |
| POST | `/api/chatroom/{roomId}/kickMember` | 채팅방 멤버 강퇴 |

### 채팅 (Chat Server - `/api/chat`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/chat/{roomId}` | 채팅 히스토리 조회 (커서 페이징) |

### WebSocket (Chat Server)

**연결**: `ws://localhost:8081/api/chat/ws-stomp`

**구독 (Subscribe)**:
- `/topic/chat.{roomId}` - 채팅 메시지 수신
- `/topic/presence.{roomId}` - 사용자 입장/퇴장 정보

**발송 (Send)**:
- `/send/chat.{roomId}` - 채팅 메시지 전송

### AI 채팅 (Main Server - `/api/ai`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/chat` | AI 멘토 질문 |

## 📖 API 문서

각 서버는 SpringDoc OpenAPI를 통해 API 문서를 제공합니다.

- **Main Server**: `http://localhost:8080/swagger-ui.html`
- **Chat Server**: `http://localhost:8081/swagger-ui.html`

## 🚀 배포

### 프로덕션 환경

- **배포 URL**: http://orgtalk.shop/
- **운영 시간**: 매일 10:00 ~ 18:00 (AWS 비용 절감을 위한 운영 시간 제한)

> ⚠️ 운영 시간 외에는 서비스가 중단됩니다.
