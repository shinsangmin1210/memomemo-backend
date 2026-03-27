# memo_memo_design

# memo_memo — 시스템 설계서

> 버전: v0.1 | 작성일: 2026-03-26 | 상태: 설계 초안
기획서 버전: v0.1 기반
> 

---

## 목차

1. [시스템 아키텍처 설계](#1-시스템-아키텍처-설계)
    - 1.1 [전체 레이어 구성](#11-전체-레이어-구성)
    - 1.2 [컴포넌트 간 의존관계](#12-컴포넌트-간-의존관계)
    - 1.3 [네트워크 구성](#13-네트워크-구성)
2. [API 설계](#2-api-설계)
    - 2.1 [공통 규칙](#21-공통-규칙)
    - 2.2 [에러 코드 정의](#22-에러-코드-정의)
    - 2.3 [REST API 엔드포인트](#23-rest-api-엔드포인트)
    - 2.4 [STOMP WebSocket 엔드포인트](#24-stomp-websocket-엔드포인트)
    - 2.5 [SSE 엔드포인트](#25-sse-엔드포인트)
3. [데이터베이스 설계](#3-데이터베이스-설계)
    - 3.1 [ERD (주요 관계)](#31-erd-주요-관계)
    - 3.2 [테이블 정의서](#32-테이블-정의서)
    - 3.3 [Flyway 마이그레이션 전략](#33-flyway-마이그레이션-전략)
    - 3.4 [풀텍스트 검색 설계](#34-풀텍스트-검색-설계)
4. [인증 / 보안 설계](#4-인증--보안-설계)
    - 4.1 [인증 흐름](#41-인증-흐름)
    - 4.2 [JWT 구성](#42-jwt-구성)
    - 4.3 [Spring Security 필터 체인](#43-spring-security-필터-체인)
    - 4.4 [Refresh Token 관리](#44-refresh-token-관리)
5. [실시간 통신 설계](#5-실시간-통신-설계)
    - 5.1 [STOMP 메시지 흐름](#51-stomp-메시지-흐름)
    - 5.2 [Redis Pub/Sub 구조](#52-redis-pubsub-구조)
    - 5.3 [SSE 알림 스트림](#53-sse-알림-스트림)
    - 5.4 [Virtual Threads 적용 범위](#54-virtual-threads-적용-범위)
6. [파일 처리 설계](#6-파일-처리-설계)
    - 6.1 [업로드 흐름](#61-업로드-흐름)
    - 6.2 [저장 경로 구조](#62-저장-경로-구조)
    - 6.3 [제한 정책](#63-제한-정책)
7. [프론트엔드 컴포넌트 설계](#7-프론트엔드-컴포넌트-설계)
    - 7.1 [컴포넌트 트리](#71-컴포넌트-트리)
    - 7.2 [Zustand 전역 상태 구조](#72-zustand-전역-상태-구조)
    - 7.3 [STOMP.js 연결 관리](#73-stompjs-연결-관리)
    - 7.4 [스타일링 전략](#74-스타일링-전략)
    - 7.5 [Shiki 코드 블록 렌더링](#75-shiki-코드-블록-렌더링)
8. [배포 / 인프라 설계](#8-배포--인프라-설계)
    - 8.1 [Docker Compose 서비스 구성](#81-docker-compose-서비스-구성)
    - 8.2 [환경변수 목록](#82-환경변수-목록)
    - 8.3 [백업 및 복구](#83-백업-및-복구)
    - 8.4 [Spring Actuator 헬스체크 및 메트릭](#84-spring-actuator-헬스체크-및-메트릭)
9. [테스트 설계](#9-테스트-설계)
    - 9.1 [테스트 범위](#91-테스트-범위)
    - 9.2 [Testcontainers 환경](#92-testcontainers-환경)
    - 9.3 [JMeter 부하 테스트 시나리오](#93-jmeter-부하-테스트-시나리오)

---

## 1. 시스템 아키텍처 설계

### 1.1 전체 레이어 구성

```
┌─────────────────────────────────────────────────────────┐
│  Client Layer                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Electron App │  │  Web Client  │  │  CLI Client  │  │
│  │ React + Vite │  │  React SPA   │  │  터미널 I/F  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼─────────────────┼─────────────────┼───────────┘
          └─────────────────┼─────────────────┘
                            │ REST / WebSocket(STOMP) / SSE
┌─────────────────────────────────────────────────────────┐
│  Backend Layer · Java 21 + Spring Boot 3.3              │
│  ┌─────────────────────────────────────────────────┐    │
│  │              Nginx (리버스 프록시 · TLS)          │    │
│  └──────────────────────┬──────────────────────────┘    │
│                         │                               │
│  ┌──────────────────────▼──────────────────────────┐    │
│  │           Spring Boot API Gateway               │    │
│  └──────┬──────────────┬──────────────┬────────────┘    │
│         │              │              │                  │
│  ┌──────▼──────┐ ┌─────▼──────┐ ┌────▼────────┐         │
│  │  Message    │ │    Auth    │ │    File     │         │
│  │  Service    │ │  Service   │ │   Service   │         │
│  │ STOMP · WS  │ │ JWT·OAuth2 │ │  Multipart  │         │
│  └─────────────┘ └────────────┘ └─────────────┘         │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │       Notification Service (SSE · Spring)        │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│  Data Layer                                             │
│  ┌────────────────┐  ┌────────────┐  ┌──────────────┐  │
│  │ PostgreSQL 16  │  │  Redis 7   │  │Local Storage │  │
│  │ JPA/Hibernate  │  │ 세션·Pub/Sub│  │ 파일·이미지  │  │
│  │ Flyway 마이그  │  │ Spring Data│  │ 온프레미스볼륨│  │
│  └────────────────┘  └────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 1.2 컴포넌트 간 의존관계

| 컴포넌트 | 의존 대상 | 통신 방식 |
| --- | --- | --- |
| Electron App | Spring Boot API | REST, STOMP, SSE |
| Message Service | PostgreSQL, Redis | JPA, Pub/Sub |
| Auth Service | PostgreSQL, OAuth2 Provider | JPA, OAuth2 프로토콜 |
| File Service | Local Storage | 파일 I/O |
| Notification Service | Redis | Sub |

### 1.3 네트워크 구성

- 외부 요청은 모두 **Nginx**를 통해 진입
- Nginx → Spring Boot `8080` 포트로 프록시
- TLS 인증서는 Nginx에서 종단 처리 (SSL Termination)
- 내부 서비스 간 통신은 Docker 내부 네트워크 사용

---

## 2. API 설계

### 2.1 공통 규칙

- Base URL: `/api/v1`
- 인증: `Authorization: Bearer {accessToken}` 헤더
- 응답 포맷:

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

- 에러 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

### 2.2 에러 코드 정의

| 코드 | HTTP 상태 | 설명 |
| --- | --- | --- |
| `UNAUTHORIZED` | 401 | 인증 토큰 없음 또는 만료 |
| `FORBIDDEN` | 403 | 권한 없음 |
| `NOT_FOUND` | 404 | 리소스 없음 |
| `VALIDATION_ERROR` | 400 | 요청 유효성 오류 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

### 2.3 REST API 엔드포인트

### 인증 (Auth)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/auth/register` | 로컬 회원가입 |
| `POST` | `/auth/login` | 로컬 로그인 |
| `GET` | `/auth/oauth2/{provider}` | OAuth 소셜 로그인 시작 (google · github) |
| `POST` | `/auth/refresh` | Access Token 갱신 |
| `POST` | `/auth/logout` | 로그아웃 (Refresh Token 무효화) |

### 사용자 (User)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/users/me` | 내 프로필 조회 |
| `PUT` | `/users/me` | 내 프로필 수정 |
| `GET` | `/users/{userId}` | 사용자 프로필 조회 |
| `GET` | `/users` | 사용자 목록 조회 |

### 채널 (Channel)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/workspaces/{wsId}/channels` | 채널 목록 조회 |
| `POST` | `/workspaces/{wsId}/channels` | 채널 생성 |
| `GET` | `/channels/{channelId}` | 채널 상세 조회 |
| `PUT` | `/channels/{channelId}` | 채널 수정 |
| `DELETE` | `/channels/{channelId}` | 채널 삭제 |
| `POST` | `/channels/{channelId}/members` | 채널 멤버 추가 |
| `DELETE` | `/channels/{channelId}/members/{userId}` | 채널 멤버 제거 |

### 메시지 (Message)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/channels/{channelId}/messages` | 메시지 목록 조회 (페이지네이션) |
| `PUT` | `/messages/{messageId}` | 메시지 수정 |
| `DELETE` | `/messages/{messageId}` | 메시지 삭제 |
| `GET` | `/messages/{messageId}/threads` | 스레드 메시지 목록 |
| `POST` | `/messages/{messageId}/reactions` | 이모지 반응 추가 |
| `DELETE` | `/messages/{messageId}/reactions/{emoji}` | 이모지 반응 제거 |
| `POST` | `/messages/{messageId}/pin` | 메시지 핀 |
| `DELETE` | `/messages/{messageId}/pin` | 메시지 핀 해제 |
| `GET` | `/channels/{channelId}/pins` | 핀 메시지 목록 |

### 검색 (Search)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/search/messages?q={keyword}&channelId={id}` | 메시지 풀텍스트 검색 |

### 파일 (File)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `POST` | `/files/upload` | 파일 업로드 |
| `GET` | `/files/{fileId}` | 파일 다운로드 |

### 2.4 STOMP WebSocket 엔드포인트

- 연결 URL: `ws://host/ws` (Nginx 프록시 경유)
- 인증: STOMP CONNECT 헤더에 `Authorization: Bearer {token}`

| 구분 | 경로 | 설명 |
| --- | --- | --- |
| Publish (송신) | `/app/channel/{channelId}/send` | 채널 메시지 전송 |
| Publish (송신) | `/app/channel/{channelId}/thread/{parentId}/send` | 스레드 메시지 전송 |
| Subscribe (수신) | `/topic/channel/{channelId}` | 채널 메시지 수신 |
| Subscribe (수신) | `/user/queue/notification` | 개인 알림 수신 |

### 2.5 SSE 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| `GET` | `/notifications/stream` | 실시간 알림 스트림 구독 |

SSE 이벤트 타입:

| 이벤트 | 설명 |
| --- | --- |
| `MENTION` | 멘션 알림 |
| `THREAD_REPLY` | 스레드 답글 알림 |
| `CHANNEL_MESSAGE` | 채널 새 메시지 알림 |

---

## 3. 데이터베이스 설계

### 3.1 ERD (주요 관계)

```
users ──────────────────────────────────────────┐
  │                                             │
  │ 1:N                                         │
workspaces                                      │
  │                                             │
  │ 1:N                                         │
channels ──── channel_members (N:M) ────────── users
  │
  │ 1:N
messages ──── reactions (N:M) ────────────── users
  │     │
  │     └── attachments (1:N)
  │
  └── pins (1:N)
```

### 3.2 테이블 정의서

### users

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 사용자 ID |
| `username` | `VARCHAR(50)` | UNIQUE, NOT NULL | 로그인 ID |
| `email` | `VARCHAR(255)` | UNIQUE, NOT NULL | 이메일 |
| `display_name` | `VARCHAR(100)` | NOT NULL | 표시 이름 |
| `avatar_url` | `VARCHAR(500)` | NULL | 프로필 이미지 경로 |
| `oauth_provider` | `VARCHAR(20)` | NULL | OAuth 제공자 (google · github, 로컬 계정은 NULL) |
| `oauth_id` | `VARCHAR(255)` | NULL | OAuth 제공자의 사용자 고유 ID |
| `password_hash` | `VARCHAR(255)` | NULL | 로컬 계정 비밀번호 해시 |
| `is_active` | `BOOLEAN` | DEFAULT true | 활성 여부 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

인덱스: `username`, `email`

### workspaces

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 워크스페이스 ID |
| `name` | `VARCHAR(100)` | NOT NULL | 이름 |
| `slug` | `VARCHAR(100)` | UNIQUE, NOT NULL | URL용 슬러그 |
| `owner_id` | `BIGINT` | FK(users) | 소유자 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

### channels

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 채널 ID |
| `workspace_id` | `BIGINT` | FK(workspaces) | 워크스페이스 |
| `name` | `VARCHAR(100)` | NOT NULL | 채널명 |
| `description` | `TEXT` | NULL | 설명 |
| `is_private` | `BOOLEAN` | DEFAULT false | 비공개 여부 |
| `created_by` | `BIGINT` | FK(users) | 생성자 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

인덱스: `workspace_id`, `name`

### channel_members

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `channel_id` | `BIGINT` | FK(channels), PK | 채널 |
| `user_id` | `BIGINT` | FK(users), PK | 사용자 |
| `role` | `VARCHAR(20)` | NOT NULL | 역할 (OWNER/MEMBER) |
| `joined_at` | `TIMESTAMPTZ` | NOT NULL | 참여일시 |

### messages

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 메시지 ID |
| `channel_id` | `BIGINT` | FK(channels) | 채널 |
| `user_id` | `BIGINT` | FK(users) | 작성자 |
| `content` | `TEXT` | NOT NULL | 내용 |
| `parent_id` | `BIGINT` | FK(messages), NULL | 스레드 부모 메시지 |
| `is_edited` | `BOOLEAN` | DEFAULT false | 편집 여부 |
| `search_vector` | `TSVECTOR` | NULL | 풀텍스트 검색 벡터 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | 수정일시 |

인덱스: `channel_id`, `parent_id`, `search_vector` (GIN)

### attachments

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 첨부파일 ID |
| `message_id` | `BIGINT` | FK(messages) | 메시지 |
| `file_name` | `VARCHAR(255)` | NOT NULL | 원본 파일명 |
| `file_size` | `BIGINT` | NOT NULL | 파일 크기 (bytes) |
| `mime_type` | `VARCHAR(100)` | NOT NULL | MIME 타입 |
| `storage_path` | `VARCHAR(500)` | NOT NULL | 저장 경로 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

### reactions

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 반응 ID |
| `message_id` | `BIGINT` | FK(messages) | 메시지 |
| `user_id` | `BIGINT` | FK(users) | 사용자 |
| `emoji` | `VARCHAR(50)` | NOT NULL | 이모지 코드 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

유니크 제약: `(message_id, user_id, emoji)`

### pins

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | PK | 핀 ID |
| `channel_id` | `BIGINT` | FK(channels) | 채널 |
| `message_id` | `BIGINT` | FK(messages) | 메시지 |
| `pinned_by` | `BIGINT` | FK(users) | 핀한 사용자 |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | 생성일시 |

### 3.3 Flyway 마이그레이션 전략

- 파일 위치: `src/main/resources/db/migration/`
- 파일명 규칙: `V{버전}__{설명}.sql` (예: `V1__init_schema.sql`)
- 운영 환경에서 `spring.flyway.validate-on-migrate=true` 적용

### 3.4 풀텍스트 검색 설계

```sql
-- search_vector 자동 갱신 트리거
CREATE FUNCTION messages_search_vector_update() RETURNS trigger AS $$
BEGIN
  NEW.search_vector := to_tsvector('simple', NEW.content);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER messages_search_vector_trigger
BEFORE INSERT OR UPDATE ON messages
FOR EACH ROW EXECUTE FUNCTION messages_search_vector_update();

-- GIN 인덱스
CREATE INDEX messages_search_vector_idx ON messages USING GIN(search_vector);
```

검색 쿼리 예시:

```sql
SELECT * FROM messages
WHERE channel_id = :channelId
  AND search_vector @@ plainto_tsquery('simple', :keyword)
ORDER BY created_at DESC;
```

---

## 4. 인증 / 보안 설계

### 4.1 인증 흐름

**로컬 회원가입 / 로그인**

```
[1] 회원가입 (POST /auth/register) 또는 로그인 (POST /auth/login)
        │
        ▼
[2] DB에서 사용자 조회 및 비밀번호 검증 (BCrypt)
        │
        ▼
[3] 인증 성공
  - Access Token 발급 (JWT, 15분)
  - Refresh Token 발급 (JWT, 7일)
  - DB refresh_tokens 테이블에 저장 (추후 Redis 전환 가능)
        │
        ▼
[4] 이후 API 요청: Authorization 헤더 JWT 검증
```

**OAuth 소셜 로그인 (Google · GitHub)**

```
[1] GET /auth/oauth2/{provider} → OAuth Provider로 리다이렉트
        │
        ▼
[2] 사용자가 Google/GitHub에서 로그인 승인
        │
        ▼
[3] Provider가 콜백 URL로 인가 코드 전달
        │
        ▼
[4] Spring Security OAuth2가 사용자 정보 조회
  - 신규: users 테이블에 자동 회원가입 (oauth_provider, oauth_id 저장)
  - 기존: 기존 계정으로 로그인
        │
        ▼
[5] JWT 발급 후 클라이언트로 전달
```

### 4.2 JWT 구성

**Access Token Payload:**

```json
{
  "sub": "userId",
  "username": "kim",
  "roles": ["ROLE_USER"],
  "iat": 1234567890,
  "exp": 1234568790
}
```

- 서명 알고리즘: `HS256`
- Secret: 환경변수 `JWT_SECRET`로 주입

### 4.3 Spring Security 필터 체인

```
요청 진입
  → JwtAuthenticationFilter (토큰 파싱 및 SecurityContext 설정)
  → 인가 처리 (채널 멤버십 등 비즈니스 권한은 Service 레이어에서 검증)
```

### 4.4 Refresh Token 관리

- 저장소: `refresh_tokens` 테이블 (DB) — 추후 Redis 전환 가능
- userId 기준 1:1 저장 (로그인 시 갱신, 로그아웃 시 삭제)
- 로그아웃 시 해당 레코드 삭제 → 토큰 무효화

---

## 5. 실시간 통신 설계

### 5.1 STOMP 메시지 흐름

```
클라이언트 A (발신)
  → STOMP SEND /app/channel/{id}/send
  → MessageController 수신
  → PostgreSQL 저장
  → Redis Pub/Sub publish (channel:{id})
  → MessageBroker subscribe
  → STOMP SEND /topic/channel/{id}
  → 클라이언트 B, C, D (수신)
```

### 5.2 Redis Pub/Sub 구조

- Publish 채널명: `channel:{channelId}`
- 메시지 페이로드 (JSON):

```json
{
  "type": "MESSAGE",
  "channelId": 1,
  "messageId": 100,
  "userId": 5,
  "content": "메시지 내용",
  "createdAt": "2026-03-26T10:00:00Z"
}
```

### 5.3 SSE 알림 스트림

- 클라이언트가 `/notifications/stream` GET 요청으로 연결 유지
- 서버는 `SseEmitter`를 userId 기준으로 Map에 보관
- 이벤트 발생 시 해당 userId의 Emitter로 push

### 5.4 Virtual Threads 적용 범위

Spring Boot 3.3에서 `spring.threads.virtual.enabled=true` 설정으로 활성화

| 적용 영역 | 효과 |
| --- | --- |
| WebSocket 연결 처리 | 연결당 Virtual Thread 할당, 플랫폼 스레드 풀 고갈 방지 |
| SSE 장기 연결 | 블로킹 I/O 비용 최소화 |
| REST API 요청 처리 | 동시 처리량 향상 |

---

## 6. 파일 처리 설계

### 6.1 업로드 흐름

```
클라이언트 → POST /files/upload (multipart/form-data)
  → FileService
  → 파일 유효성 검사 (크기, MIME 타입)
  → 로컬 볼륨 저장 (/app/files/{yyyy}/{MM}/{dd}/{uuid}_{filename})
  → DB attachments 테이블 저장
  → 응답: { fileId, fileUrl }
```

### 6.2 저장 경로 구조

```
/app/files/
  └── 2026/
        └── 03/
              └── 26/
                    └── {uuid}_{originalFilename}
```

### 6.3 제한 정책

| 항목 | 제한 |
| --- | --- |
| 최대 파일 크기 | 50MB |
| 허용 MIME 타입 | image/*, text/*, application/pdf, application/zip 등 |
| 이미지 미리보기 | `image/*` MIME 타입에 한해 인라인 렌더링 |
| 코드 파일 인라인 | `.js .ts .py .java .go .sql` 등 텍스트 파일 |

---

## 7. 프론트엔드 컴포넌트 설계

### 7.1 컴포넌트 트리

```
App
├── AuthProvider (Zustand 인증 상태)
├── MainLayout
│   ├── ChannelSidebar
│   │   ├── WorkspaceName
│   │   ├── ChannelList
│   │   │   └── ChannelItem (안읽음 배지)
│   │   ├── DirectMessageList
│   │   │   └── DmItem (온라인 상태)
│   │   └── MyProfile
│   ├── ChatArea
│   │   ├── ChannelHeader
│   │   ├── MessageFeed
│   │   │   └── MessageBubble
│   │   │       ├── Avatar
│   │   │       ├── MessageContent
│   │   │       │   ├── MarkdownRenderer
│   │   │       │   └── CodeBlock (Shiki)
│   │   │       ├── ReactionBar
│   │   │       └── ThreadPreview
│   │   └── InputArea
│   │       ├── FormatToolbar (B / I / </> / Link / 첨부)
│   │       └── MessageInput
│   └── RightPanel
│       ├── PinnedMessages
│       └── MemberList
└── ThreadDrawer (스레드 열릴 때 표시)
```

### 7.2 Zustand 전역 상태 구조

```tsx
// Auth Store
{
  user: User | null,
  accessToken: string | null,
  login: (credentials) => void,
  logout: () => void,
}

// Channel Store
{
  channels: Channel[],
  activeChannelId: number | null,
  setActiveChannel: (id: number) => void,
}

// Message Store
{
  messages: Record<channelId, Message[]>,
  appendMessage: (channelId, message) => void,
  updateMessage: (messageId, content) => void,
  deleteMessage: (messageId) => void,
}

// Notification Store
{
  unreadCounts: Record<channelId, number>,
  increment: (channelId) => void,
  reset: (channelId) => void,
}
```

### 7.3 STOMP.js 연결 관리

- 앱 초기화 시 `useStompClient` 훅에서 연결 수립
- Access Token 만료 시 Refresh 후 재연결
- 채널 변경 시 이전 구독 해제 → 새 채널 구독

### 7.4 스타일링 전략

- 스타일링 도구: **CSS Modules** (Vite 기본 지원, 별도 설정 불필요)
- 전역 스타일(`global.css`): 폰트, CSS 변수(색상·간격·폰트 크기), reset 스타일만 정의
- 공통 컴포넌트(`ui/`): 각 컴포넌트마다 `*.module.css` 파일을 1:1로 작성
- 화면 컴포넌트: 직접 CSS 작성 금지 — `ui/` 공통 컴포넌트를 조립하여 구현
- 파일 구조 예시:

```
ui/
├── Button.jsx
├── Button.module.css
├── Avatar.jsx
├── Avatar.module.css
...
```

### 7.5 Shiki 코드 블록 렌더링

- 서버 사이드 하이라이팅: Shiki를 번들에 포함, 클라이언트에서 렌더링
- 언어 자동 감지: 마크다운 코드 펜스의 언어 태그 우선, 없으면 `plaintext`
- 복사 버튼: 클립보드 API 사용

---

## 8. 배포 / 인프라 설계

### 8.1 Docker Compose 서비스 구성

```yaml
version:"3.9"

services:
api:
image: memo_memo-api:latest
ports:
-"8080:8080"
environment:
- DB_HOST=postgres
- REDIS_HOST=redis
- JWT_SECRET=${JWT_SECRET}
- GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
- GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
- GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}
- GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}
- SPRING_THREADS_VIRTUAL_ENABLED=true
volumes:
- ./data/files:/app/files
depends_on:
- postgres
- redis
healthcheck:
test:["CMD","curl","-f","http://localhost:8080/actuator/health"]
interval: 30s
timeout: 10s
retries:3

postgres:
image: postgres:16
environment:
- POSTGRES_DB=memo_memo
- POSTGRES_USER=memo_memo
- POSTGRES_PASSWORD=${DB_PASSWORD}
volumes:
- postgres_data:/var/lib/postgresql/data

redis:
image: redis:7-alpine
volumes:
- redis_data:/data

nginx:
image: nginx:alpine
ports:
-"80:80"
-"443:443"
volumes:
- ./nginx.conf:/etc/nginx/nginx.conf
- ./certs:/etc/nginx/certs
depends_on:
- api

volumes:
postgres_data:
redis_data:
```

### 8.2 환경변수 목록

| 변수명 | 설명 | 예시 |
| --- | --- | --- |
| `JWT_SECRET` | JWT 서명 시크릿 (32자 이상 랜덤) | `openssl rand -hex 32` 결과 |
| `DB_PASSWORD` | PostgreSQL 비밀번호 | - |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID | Google Cloud Console에서 발급 |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 | Google Cloud Console에서 발급 |
| `GITHUB_CLIENT_ID` | GitHub OAuth 클라이언트 ID | GitHub Developer Settings에서 발급 |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth 클라이언트 시크릿 | GitHub Developer Settings에서 발급 |

### 8.3 백업 및 복구

**백업 스크립트 (`backup.sh`):**

```bash
# PostgreSQL 덤프
pg_dump -U memo_memo memo_memo > backup_$(date +%Y%m%d).sql

# 파일 볼륨 압축
tar -czf files_$(date +%Y%m%d).tar.gz ./data/files
```

**복구 절차:**
1. 컨테이너 중지
2. `psql -U memo_memo memo_memo < backup_{날짜}.sql`
3. 파일 볼륨 압축 해제
4. 컨테이너 재시작

### 8.4 Spring Actuator 헬스체크 및 메트릭

노출 엔드포인트:
- `GET /actuator/health` — 헬스체크 (Nginx 프록시 제외, 내부 전용)
- `GET /actuator/metrics` — JVM, HTTP 요청 메트릭

---

## 9. 테스트 설계

### 9.1 테스트 범위

| 레이어 | 도구 | 범위 |
| --- | --- | --- |
| 단위 테스트 | JUnit 5 + Mockito | Service, Util 클래스 |
| 통합 테스트 | Testcontainers | Repository, API 엔드포인트 (실제 DB) |
| 부하 테스트 | JMeter | 동시 접속 500명 시나리오 |

### 9.2 Testcontainers 환경

```java
@Testcontainers
class MessageServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
}
```

### 9.3 JMeter 부하 테스트 시나리오

| 시나리오 | 조건 | 목표 |
| --- | --- | --- |
| 동시 메시지 전송 | 가상 사용자 500명, 채널 10개 | 에러율 0%, 평균 응답 200ms 이하 |
| SSE 동시 연결 | 가상 사용자 500명 유지 | 연결 유지 안정성 확인 |
| 파일 업로드 | 10MB 파일, 동시 50명 | 에러율 0% |

---

*문서 끝 — memo_memo 설계서 v0.1*