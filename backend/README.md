# DevTalk Backend

실시간 채팅 서비스를 위한 백엔드 시스템입니다. GitHub OAuth2 인증을 통한 멤버 관리, **Redis Pub/Sub 기반의 분산 메시징 아키텍처**, 그리고 *
*OpenAI GPT-3.5 Turbo 기반 AI 멘토링** 기능을 제공합니다.

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [Redis Pub/Sub 아키텍처](#redis-pubsub-아키텍처)
- [AI 멘토 시스템](#ai-멘토-시스템)
- [API 문서](#api-문서)
- [환경 설정](#환경-설정)
- [로컬 개발 환경 설정](#로컬-개발-환경-설정)
- [배포](#배포)
- [모니터링](#모니터링)
- [보안](#보안)

## 📖 프로젝트 개요

DevTalk은 GitHub 계정으로 로그인하여 사용할 수 있는 실시간 채팅 플랫폼입니다. 사용자는 GitHub OAuth2를 통해 로그인하고, 채팅방을 생성하고 참여하여
실시간으로 소통할 수 있습니다. 또한 **OpenAI GPT-3.5 Turbo 기반 AI 멘토 기능**을 통해 개발 관련 질문에 대한 답변을 받을 수 있습니다.

### 아키텍처

- **멀티 모듈 구조**: `main-server`와 `chat-server`로 분리된 마이크로서비스 아키텍처
- **메인 서버 (main-server)**: 인증, 멤버 관리, 조직 관리, **AI 멘토** 담당 (포트 8080)
- **채팅 서버 (chat-server)**: 실시간 채팅 및 채팅방 관리 담당 (포트 8081)
- **Redis Pub/Sub**: 분산 환경에서의 실시간 메시지 브로드캐스팅 및 서버 간 통신 (멀티 인스턴스 지원)
- **MongoDB**: 채팅 메시지 영구 저장 및 히스토리 관리
- **OpenAI API**: GPT-3.5 Turbo 모델을 활용한 AI 멘토링 서비스

## 🛠 기술 스택

### Backend

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Security** + **OAuth2 Client** (GitHub)
- **Spring WebSocket** / **STOMP** (실시간 채팅)
- **Spring WebFlux** / **WebClient** (비동기 HTTP 통신)

### Database & 메시징

- **MySQL** (JPA/Hibernate) - 멤버, 조직, 채팅방 메타데이터
- **MongoDB** - 채팅 메시지 영구 저장
- **Redis** - 세션 관리, 사용자 Presence 정보
- **Redis Pub/Sub** - 분산 메시징 및 실시간 브로드캐스팅 (채팅 메시지, Presence 업데이트)

### 인증/보안

- **JWT** (Access Token + Refresh Token)
- **GitHub OAuth2**

### 외부 API & 기타

- **OpenAI API** - GPT-3.5 Turbo를 활용한 AI 멘토링
- **Resilience4j** - Circuit Breaker 패턴
- **Prometheus** + **Spring Actuator** - 모니터링 및 메트릭 수집
- **Docker** - 컨테이너화
- **Gradle** - 빌드 도구

## 📁 프로젝트 구조

```
backend/
├── main-server/          # 메인 서버 (인증, 멤버, 조직, AI 멘토)
│   ├── src/main/java/yuhan/pro/mainserver/
│   │   ├── domain/
│   │   │   ├── auth/        # 인증 도메인
│   │   │   ├── member/      # 멤버 도메인
│   │   │   ├── organization/# 조직 도메인
│   │   │   └── ai/          # AI 멘토 도메인 (OpenAI GPT-3.5 Turbo)
│   │   ├── core/            # 보안 설정, CORS 설정
│   │   └── sharedkernel/    # 공통 유틸리티 (JWT, 필터 등)
│   └── Dockerfile
│
├── chat-server/         # 채팅 서버 (실시간 채팅)
│   ├── src/main/java/yuhan/pro/chatserver/
│   │   ├── domain/
│   │   │   ├── entity/       # 채팅, 채팅방 엔티티
│   │   │   └── service/      # 채팅 비즈니스 로직
│   │   ├── core/            # WebClient 설정
│   │   └── sharedkernel/
│   │       ├── infra/
│   │       │   ├── socket/  # WebSocket 설정
│   │       │   └── redis/   # Redis Pub/Sub 설정 및 서비스
│   │       └── jwt/         # JWT 인증
│   └── Dockerfile
│
├── build.gradle         # 루트 빌드 설정
├── settings.gradle      # 멀티 모듈 설정
└── prometheus.yml       # Prometheus 설정
```

## ✨ 주요 기능

### 1. 인증 및 인가

- **GitHub OAuth2 로그인**: GitHub 계정으로 소셜 로그인
- **JWT 기반 인증**: Access Token (1시간) + Refresh Token (24시간)
- **토큰 재발급**: Refresh Token을 통한 Access Token 갱신
- **로그아웃**: Refresh Token 블랙리스트 관리

### 2. 멤버 관리

- GitHub 사용자 정보 동기화
- 멤버 프로필 조회
- 멤버가 속한 조직 목록 조회

### 3. 조직 관리

- GitHub 조직 정보 조회
- 조직 생성 (테스트용)
- 멤버-조직 다대다 관계 관리

### 4. 채팅방 관리

- 공개/비공개 채팅방 생성
- 조직별 채팅방 목록 조회 (페이징)
- 채팅방 검색
- 채팅방 멤버 관리

### 5. 실시간 채팅

- **WebSocket/STOMP** 기반 실시간 메시지 전송
- **Redis Pub/Sub**를 통한 분산 메시징 (멀티 인스턴스 환경 지원)
- 채팅 히스토리 조회 (커서 기반 페이징)
- 사용자 Presence 정보 (입장/퇴장) 실시간 업데이트
- Redis를 통한 Presence 상태 관리
- 비동기 메시지 전송으로 성능 최적화

### 6. AI 멘토 시스템

- **OpenAI GPT-3.5 Turbo** 기반 AI 멘토링
- 개발 관련 질문에 대한 실시간 답변
- 한국어 지원
- 코드 예제 및 실용적인 조언 제공
- WebClient를 통한 비동기 API 호출

## 🔄 Redis Pub/Sub 아키텍처

DevTalk은 Redis Pub/Sub을 활용하여 분산 환경에서의 실시간 메시징을 구현합니다.

### 아키텍처 특징

- **멀티 인스턴스 지원**: 채팅 서버를 여러 인스턴스로 확장 가능
- **메시지 브로드캐스팅**: 한 서버에서 발행한 메시지가 모든 서버 인스턴스로 전파
- **채널 기반 통신**:
    - `chat:{roomId}` - 채팅 메시지 채널
    - `presence:{roomId}` - 사용자 입장/퇴장 정보 채널

### 동작 방식

1. **메시지 발행 (Publish)**
    - 클라이언트가 WebSocket을 통해 메시지 전송
    - 채팅 서버가 Redis 채널에 메시지 발행
    - `RedisPubSubService.publishChatMessage()` 호출

2. **메시지 구독 (Subscribe)**
    - 모든 채팅 서버 인스턴스가 Redis 채널 구독
    - `RedisMessageListenerContainer`가 메시지 수신
    - 전용 스레드 풀(`redisPubSubExecutor`)에서 비동기 처리

3. **WebSocket 브로드캐스팅**
    - 수신한 메시지를 해당 채팅방의 모든 WebSocket 연결에 전송
    - `SimpMessagingTemplate.convertAndSend()` 사용

### 성능 최적화

- **전용 스레드 풀**: CorePoolSize 100, MaxPoolSize 500
- **비동기 처리**: Redis 메시지 수신 및 WebSocket 전송 비동기화
- **대용량 큐**: QueueCapacity 10,000으로 대량 트래픽 대응

## 🤖 AI 멘토 시스템

OpenAI GPT-3.5 Turbo를 활용한 개발자 전문 AI 멘토링 기능을 제공합니다.

### 주요 기능

- **개발 질문 답변**: 프로그래밍, 알고리즘, 프레임워크 등 개발 관련 질문에 답변
- **코드 예제 제공**: 실용적인 코드 예제와 설명
- **한국어 지원**: 모든 답변이 한국어로 제공
- **토큰 사용량 추적**: 각 요청의 토큰 사용량을 응답에 포함

### 기술 구현

- **모델**: GPT-3.5 Turbo (OpenAI API)
- **Temperature**: 0.7 (균형잡힌 창의성)
- **Max Tokens**: 2000 (충분한 답변 길이)
- **비동기 처리**: Spring WebFlux의 WebClient 사용
- **에러 핸들링**: API 키 미설정, 네트워크 오류 등 모든 예외 처리

### API 엔드포인트

```
POST /api/ai/chat
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "question": "Spring Boot에서 Redis Pub/Sub을 어떻게 구현하나요?"
}

Response:
{
  "answer": "Spring Boot에서 Redis Pub/Sub을 구현하려면...",
  "totalTokens": 450
}
```

### 시스템 프롬프트

AI는 다음과 같은 역할로 설정되어 있습니다:

- 개발자들을 위한 전문 AI 멘토
- 명확하고 정확한 답변 제공
- 복잡한 개념을 쉽게 설명
- 실용적인 조언 제공

## 📚 API 문서

각 서버는 SpringDoc OpenAPI를 통해 API 문서를 제공합니다.

- **Main Server API 문서**: `http://localhost:8080/swagger-ui.html`
- **Chat Server API 문서**: `http://localhost:8081/swagger-ui.html`

### 주요 API 엔드포인트

#### 인증 (Main Server)

- `GET /api/auth/me` - 현재 로그인한 사용자 정보 조회
- `POST /api/auth/refresh` - Access Token 재발급
- `POST /api/auth/logout` - 로그아웃

#### 멤버 (Main Server)

- `GET /api/member/organizations` - 사용자가 속한 조직 목록 조회
- `POST /api/member/chatMembers` - 채팅방 멤버 정보 조회
- `GET /api/member/profile-url` - 멤버 프로필 URL 조회

#### 조직 (Main Server)

- `GET /api/organization/{organizationId}` - 조직 정보 조회
- `POST /api/organization` - 조직 생성

#### 채팅방 (Chat Server)

- `POST /api/chatroom/create` - 채팅방 생성
- `GET /api/chatroom/list/{organizationId}` - 채팅방 목록 조회
- `GET /api/chatroom/search` - 채팅방 검색

#### 채팅 (Chat Server)

- `GET /api/chat/{roomId}` - 채팅 히스토리 조회
- WebSocket: `ws://localhost:8081/api/chat/ws-stomp`
    - Subscribe: `/topic/chat.{roomId}`, `/presence.{roomId}`
    - Send: `/send/chat.{roomId}`

#### AI 멘토 (Main Server)

- `POST /api/ai/chat` - AI 멘토에게 질문하기
- `GET /api/ai/health` - AI 서비스 상태 확인

## ⚙️ 환경 설정

### 필수 환경 변수

각 서버를 실행하기 전에 다음 환경 변수를 설정해야 합니다.

#### Main Server

```bash
# GitHub OAuth2
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# JWT Secret
JWT_SECRET=your_jwt_secret_key

# OpenAI API (AI 멘토 기능)
OPENAI_API_KEY=your_openai_api_key

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/devtalk
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

#### Chat Server

```bash
# JWT Secret (Main Server와 동일)
JWT_SECRET=your_jwt_secret_key

# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/devtalk

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Main Server URL (서버 간 통신)
MAIN_SERVER_URL=http://localhost:8080
```

### application.yml 설정

각 서버의 `src/main/resources/application.yml` 파일에서 프로파일별 설정을 관리합니다.

- `application-dev.yml` - 개발 환경
- `application-test.yml` - 테스트 환경
- `application-prod.yml` - 운영 환경

## 🛠 로컬 개발 환경 설정

### 1. 필수 소프트웨어 설치

- Java 21
- Docker & Docker Compose
- Gradle 8.x

### 2. 인프라 실행 (Docker Compose)

루트 디렉토리에서 다음 명령어를 실행하여 MySQL, MongoDB, Redis를 시작합니다:

```bash
docker-compose up -d
```

### 3. 환경 변수 설정

위의 [환경 설정](#환경-설정) 섹션을 참고하여 환경 변수를 설정합니다.

### 4. 서버 실행

#### Main Server 실행

```bash
cd backend/main-server
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Chat Server 실행

```bash
cd backend/chat-server
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5. 실행 확인

- Main Server: http://localhost:8080
- Chat Server: http://localhost:8081
- Main Server Swagger UI: http://localhost:8080/swagger-ui.html
- Chat Server Swagger UI: http://localhost:8081/swagger-ui.html

## 🚢 배포

프로젝트는 GitHub Actions를 통해 자동으로 빌드 및 배포됩니다.

## 📊 모니터링

- **Prometheus 메트릭**: `/actuator/prometheus` 엔드포인트를 통해 메트릭 수집
- **Health Check**: `/actuator/health` 엔드포인트를 통해 헬스 체크
- **Prometheus 설정**: 루트 디렉토리의 `prometheus.yml` 참고

## 🔒 보안

- JWT 토큰은 HTTP-only 쿠키에 저장 (Refresh Token)
- CORS 설정을 통한 크로스 오리진 요청 제어
- Spring Security를 통한 엔드포인트 보호
- WebSocket 연결 시 JWT 인증 필수
- OpenAI API 키는 환경 변수로 관리 (코드에 하드코딩 금지)
- AI 멘토 API는 인증된 사용자만 접근 가능

## 📝 라이선스

이 프로젝트는 비공개 프로젝트입니다.

## 👥 기여

프로젝트 기여에 대한 문의는 프로젝트 관리자에게 연락해주세요.

