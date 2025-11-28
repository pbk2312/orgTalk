# DevTalk

GitHub 계정 기반 실시간 채팅 플랫폼

## 📖 프로젝트 소개

DevTalk은 GitHub 계정으로 로그인하여 사용할 수 있는 실시간 채팅 서비스입니다. 사용자는 GitHub OAuth2를 통해 로그인하고, 채팅방을 생성하고 참여하여
실시간으로 소통할 수 있습니다. **Redis Pub/Sub을 활용한 분산 메시징 아키텍처**로 확장 가능한 실시간 채팅을 제공하며, **OpenAI GPT-3.5 Turbo 기반
AI 멘토** 기능으로 개발 관련 질문에 즉시 답변을 받을 수 있습니다.

### 아키텍처

- **마이크로서비스 아키텍처**: `main-server`와 `chat-server`로 분리
- **Frontend** (포트 80/443): React 기반 SPA, Nginx로 서빙
- **Main Server** (포트 8080): 인증, 멤버 관리, **AI 멘토**
- **Chat Server** (포트 8081): 실시간 채팅 및 채팅방 관리
- **Redis Pub/Sub**: 멀티 인스턴스 채팅 서버 간 메시지 동기화

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

### Database & 메시징

- **MySQL**: 멤버, 채팅방 메타데이터
- **MongoDB**: 채팅 메시지 영구 저장
- **Redis**: Refresh Token 저장, 사용자 Presence 정보
- **Redis Pub/Sub**: 분산 채팅 메시지 브로드캐스팅 (멀티 인스턴스 지원)

### 외부 API & 기타

- **OpenAI API**: GPT-3.5 Turbo를 활용한 AI 멘토링
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

| Method | Endpoint            | Description       |
|--------|---------------------|-------------------|
| GET    | `/api/auth/me`      | 현재 로그인한 사용자 정보 조회 |
| POST   | `/api/auth/refresh` | Access Token 재발급  |
| POST   | `/api/auth/logout`  | 로그아웃              |

### 멤버 (Main Server - `/api/member`)

| Method | Endpoint                  | Description   |
|--------|---------------------------|---------------|
| POST   | `/api/member/chatMembers` | 채팅방 멤버 정보 조회  |
| GET    | `/api/member/profile-url` | 멤버 프로필 URL 조회 |

### 채팅방 (Chat Server - `/api/chatroom`)

| Method | Endpoint                            | Description     |
|--------|-------------------------------------|-----------------|
| POST   | `/api/chatroom/create`              | 채팅방 생성          |
| GET    | `/api/chatroom/list`                | 채팅방 목록 조회 (페이징) |
| GET    | `/api/chatroom/search`              | 채팅방 검색          |
| GET    | `/api/chatroom/{roomId}`            | 채팅방 정보 조회       |
| POST   | `/api/chatroom/{roomId}/join`       | 채팅방 입장          |
| POST   | `/api/chatroom/{roomId}/delete`     | 채팅방 삭제          |
| PATCH  | `/api/chatroom/{roomId}/update`     | 채팅방 수정          |
| POST   | `/api/chatroom/{roomId}/kickMember` | 채팅방 멤버 강퇴       |

### 채팅 (Chat Server - `/api/chat`)

| Method | Endpoint             | Description         |
|--------|----------------------|---------------------|
| GET    | `/api/chat/{roomId}` | 채팅 히스토리 조회 (커서 페이징) |

### WebSocket (Chat Server)

**연결**: `ws://localhost:8081/api/chat/ws-stomp`

**구독 (Subscribe)**:

- `/topic/chat.{roomId}` - 채팅 메시지 수신
- `/topic/presence.{roomId}` - 사용자 입장/퇴장 정보

**발송 (Send)**:

- `/send/chat.{roomId}` - 채팅 메시지 전송

### AI 채팅 (Main Server - `/api/ai`)

| Method | Endpoint         | Description  |
|--------|------------------|--------------|
| POST   | `/api/ai/chat`   | AI 멘토에게 질문하기 |
| GET    | `/api/ai/health` | AI 서비스 상태 확인 |

## ✨ 주요 기능

### 1. GitHub OAuth2 인증

- GitHub 계정으로 간편 로그인
- JWT 기반 토큰 인증 (Access Token + Refresh Token)
- 자동 토큰 갱신 및 로그아웃

### 2. 실시간 채팅

- **WebSocket/STOMP** 기반 실시간 메시징
- **Redis Pub/Sub**를 통한 분산 메시징 (멀티 서버 인스턴스 지원)
- 채팅 히스토리 조회 및 무한 스크롤
- 사용자 입장/퇴장 알림
- 이모지 및 코드 블록 지원

### 3. 채팅방 관리

- 공개/비공개 채팅방 생성
- 채팅방 검색 및 필터링
- 채팅방 멤버 관리 및 강퇴 기능
- 조직별 채팅방 그룹화

### 4. AI 멘토 시스템

- **OpenAI GPT-3.5 Turbo** 기반 개발 멘토
- 프로그래밍, 알고리즘, 프레임워크 질문 답변
- 한국어 지원 및 코드 예제 제공
- 실시간 답변 및 토큰 사용량 표시
- 플로팅 버튼으로 언제든지 접근 가능

### 5. 반응형 UI/UX

- 모던한 디자인과 부드러운 애니메이션 (Framer Motion)
- 다크 모드 지원
- 모바일 최적화
- 직관적인 사용자 인터페이스

## 🔄 Redis Pub/Sub 아키텍처

DevTalk은 **Redis Pub/Sub**을 활용하여 분산 환경에서 확장 가능한 실시간 메시징을 구현합니다.

### 왜 Redis Pub/Sub인가?

- **수평 확장**: 채팅 서버를 여러 인스턴스로 확장 가능
- **메시지 동기화**: 서로 다른 서버에 연결된 사용자 간 실시간 메시지 전달
- **고성능**: 초당 수만 건의 메시지 처리 가능
- **단순한 구조**: Pub/Sub 패턴으로 간단하게 구현

### 동작 흐름

```
User A (Server 1) → WebSocket → Chat Server 1 → Redis Publish
                                                      ↓
                                                  Redis Channel
                                                      ↓
                                    ┌─────────────────┴─────────────────┐
                                    ↓                                   ↓
                            Chat Server 1                        Chat Server 2
                                    ↓                                   ↓
                            WebSocket Broadcast                 WebSocket Broadcast
                                    ↓                                   ↓
                                User A                              User B
```

1. 사용자가 메시지를 WebSocket으로 전송
2. 채팅 서버가 Redis 채널에 메시지 발행
3. 모든 채팅 서버 인스턴스가 메시지 수신
4. 각 서버가 연결된 WebSocket 클라이언트에 브로드캐스트

## 🤖 AI 멘토 시스템

**OpenAI GPT-3.5 Turbo**를 활용한 개발자 전용 AI 멘토링 서비스

### 특징

- **전문화된 프롬프트**: 개발자를 위한 맞춤형 시스템 프롬프트
- **한국어 완벽 지원**: 모든 질문과 답변이 한국어로 처리
- **코드 예제 제공**: 실용적인 코드 스니펫과 설명
- **비동기 처리**: Spring WebFlux로 빠른 응답 제공
- **토큰 추적**: 각 요청의 토큰 사용량 확인 가능

### 사용 예시

**질문**: "Spring Boot에서 Redis Pub/Sub을 어떻게 구현하나요?"

**AI 답변**: 상세한 구현 방법, 코드 예제, 설정 방법 등을 포함한 완전한 답변 제공

### 프론트엔드 구현

- **플로팅 버튼**: 모든 페이지에서 쉽게 접근 가능한 AI 버튼
- **전용 페이지**: `/ai-mentor` 경로로 AI 멘토 채팅 페이지 제공
- **실시간 응답**: 질문 후 즉시 답변 표시
- **히스토리 관리**: 세션 내에서 이전 대화 기록 유지

## 📖 API 문서

각 서버는 SpringDoc OpenAPI를 통해 API 문서를 제공합니다.

- **Main Server**: `http://localhost:8080/swagger-ui.html`
- **Chat Server**: `http://localhost:8081/swagger-ui.html`

## 🚀 배포

### 프로덕션 환경

- **배포 URL**: http://orgtalk.shop/
- **운영 시간**: 매일 10:00 ~ 18:00 (AWS 비용 절감을 위한 운영 시간 제한)

> ⚠️ 운영 시간 외에는 서비스가 중단됩니다.

## ⚡ 기술적 특징 및 성능 최적화

### 마이크로서비스 아키텍처

- Main Server와 Chat Server로 관심사 분리
- 각 서버의 독립적인 스케일링 가능
- 서버 간 WebClient를 통한 비동기 통신

### Redis Pub/Sub을 통한 확장성

- 채팅 서버의 수평 확장 가능 (멀티 인스턴스)
- 전용 스레드 풀로 고성능 메시지 처리
    - CorePoolSize: 100
    - MaxPoolSize: 500
    - QueueCapacity: 10,000
- 비동기 메시지 전송으로 블로킹 최소화

### WebSocket 최적화

- STOMP 프로토콜을 통한 효율적인 메시지 라우팅
- 채팅방별 토픽 구독으로 불필요한 메시지 전송 방지
- JWT 기반 WebSocket 인증으로 보안 강화

### 데이터베이스 전략

- **MySQL**: 관계형 데이터 (멤버, 채팅방 메타데이터)
- **MongoDB**: 대용량 채팅 메시지 저장 및 빠른 조회
- **Redis**: 캐싱 및 세션 관리로 DB 부하 감소

### AI 멘토 최적화

- WebClient를 통한 비동기 API 호출
- Circuit Breaker 패턴으로 장애 격리
- 에러 핸들링 및 폴백 처리

## 🔐 보안

- **JWT 인증**: Access Token (1시간) + Refresh Token (24시간)
- **HTTP-only 쿠키**: XSS 공격 방지를 위한 Refresh Token 저장
- **CORS 정책**: 허용된 Origin만 접근 가능
- **Spring Security**: 엔드포인트별 접근 제어
- **WebSocket 인증**: 연결 시 JWT 검증 필수
- **환경 변수**: API 키 및 시크릿 키 안전 관리

## 📊 모니터링 및 관찰성

- **Prometheus**: 메트릭 수집 및 모니터링
- **Spring Actuator**: 헬스 체크 및 애플리케이션 상태 확인
- **로깅**: SLF4J + Logback을 통한 구조화된 로그

## 🛠 개발 환경 설정

### 필수 요구사항

- Java 21
- Node.js 18+
- Docker & Docker Compose
- Git

### 로컬 실행 방법

1. **저장소 클론**

```bash
git clone https://github.com/your-repo/orgTalk.git
cd orgTalk
```

2. **인프라 실행**

```bash
docker-compose up -d
```

3. **환경 변수 설정**

```bash
# backend/main-server/.env
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
JWT_SECRET=your_jwt_secret
OPENAI_API_KEY=your_openai_api_key

# backend/chat-server/.env
JWT_SECRET=your_jwt_secret
```

4. **백엔드 실행**

```bash
# Main Server
cd backend/main-server
./gradlew bootRun

# Chat Server (새 터미널)
cd backend/chat-server
./gradlew bootRun
```

5. **프론트엔드 실행**

```bash
cd frontend
npm install
npm start
```

6. **접속**

- Frontend: http://localhost:3000
- Main Server: http://localhost:8080
- Chat Server: http://localhost:8081

## 📝 상세 문서

- [Backend README](./backend/README.md) - 백엔드 상세 문서
- [Frontend README](./frontend/README.md) - 프론트엔드 상세 문서
- [로컬 설정 가이드](./LOCAL_SETUP.md) - 로컬 환경 설정 가이드
- [WebSocket 성능 개선](./backend/WEBSOCKET_PERFORMANCE_IMPROVEMENT.md) - Redis Pub/Sub 도입 및 최적화

## 👥 팀

이 프로젝트는 유한대학교 학생들이 개발한 프로젝트입니다.

## 📄 라이선스

이 프로젝트는 비공개 프로젝트입니다.

