# OrgTalk Backend

조직 기반 실시간 채팅 서비스를 위한 백엔드 시스템입니다. GitHub OAuth2 인증을 통한 멤버 관리, 조직 관리, 그리고 WebSocket 기반의 실시간 채팅 기능을 제공합니다.

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 문서](#api-문서)
- [배포](#배포)
- [모니터링](#모니터링)
- [보안](#보안)

## 📖 프로젝트 개요

OrgTalk은 GitHub 조직을 기반으로 한 실시간 채팅 플랫폼입니다. 사용자는 GitHub OAuth2를 통해 로그인하고, 자신이 속한 GitHub 조직의 채팅방에서 실시간으로 소통할 수 있습니다.

### 아키텍처

- **멀티 모듈 구조**: `main-server`와 `chat-server`로 분리된 마이크로서비스 아키텍처
- **메인 서버 (main-server)**: 인증, 멤버 관리, 조직 관리 담당 (포트 8080)
- **채팅 서버 (chat-server)**: 실시간 채팅 및 채팅방 관리 담당 (포트 8081)

## 🛠 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Security** + **OAuth2 Client** (GitHub)
- **Spring WebSocket** / **STOMP** (실시간 채팅)
- **Spring WebFlux** / **WebClient** (비동기 HTTP 통신)

### Database
- **MySQL** (JPA/Hibernate) - 멤버, 조직, 채팅방 메타데이터
- **MongoDB** - 채팅 메시지 저장
- **Redis** - 세션 관리 및 사용자 Presence 정보

### 인증/보안
- **JWT** (Access Token + Refresh Token)
- **GitHub OAuth2**

### 기타
- **Resilience4j** - Circuit Breaker 패턴
- **Prometheus** + **Spring Actuator** - 모니터링 및 메트릭 수집
- **Docker** - 컨테이너화
- **Gradle** - 빌드 도구

## 📁 프로젝트 구조

```
backend/
├── main-server/          # 메인 서버 (인증, 멤버, 조직 관리)
│   ├── src/main/java/yuhan/pro/mainserver/
│   │   ├── domain/
│   │   │   ├── auth/        # 인증 도메인
│   │   │   ├── member/      # 멤버 도메인
│   │   │   └── organization/# 조직 도메인
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
│   │       ├── infra/socket/# WebSocket 설정
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
- 채팅 히스토리 조회 (커서 기반 페이징)
- 사용자 Presence 정보 (입장/퇴장) 실시간 업데이트
- Redis를 통한 Presence 상태 관리

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

## 📝 라이선스

이 프로젝트는 비공개 프로젝트입니다.

## 👥 기여

프로젝트 기여에 대한 문의는 프로젝트 관리자에게 연락해주세요.

