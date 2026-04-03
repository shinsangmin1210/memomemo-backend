memo_memo_dev_plan

memo_memo — 개발자 팀 특화 온프레미스 메신저 기획서

> 버전: v0.1 | 작성일: 2026-03-25 | 상태: 기획 초안
> 

---

## 1. 프로젝트 개요

### 1.1 목표

memomemo은 Slack / Mattermost를 대체하는 **개발자 팀 특화 온프레미스 협업 메신저**다. 핵심 지향점은 세 가지다.

- **경량성** — 필수 기능만 포함해 운영 부담을 최소화한다. Docker Compose 하나로 10분 내 설치를 목표로 한다.
- **개발자 UX** — 코드 블록 렌더링, 마크다운, 스레드 등 개발 업무에 필요한 기능을 플러그인 없이 기본 제공한다.
- **데이터 주권** — 모든 메시지와 파일을 사내 서버에만 저장한다. 외부 클라우드 의존성을 제거한다.

### 1.2 타겟 사용자

- 온프레미스 환경에서 운영하는 **개발자 팀 (10~200명 규모)**
- 외부 SaaS 사용이 제한된 금융, 공공, 제조, 게임 등의 조직
- 회원가입 또는 소셜 로그인(OAuth)으로 간편하게 팀을 구성하고자 하는 팀

### 1.3 경쟁 제품 대비 차별화

| 구분 | Slack | Mattermost | memomemo |
| --- | --- | --- | --- |
| 배포 방식 | 클라우드 SaaS | 온프레미스 가능 | **온프레미스 전용** |
| 설치 복잡도 | N/A | 높음 (Java 스택) | **낮음 (Docker Compose 단일 명령)** |
| 코드 블록 | 기본 지원 | 기본 지원 | **Shiki 기반 구문 강조 내장** |
| OAuth 로그인 | 지원 | 미지원 | **Google · GitHub 소셜 로그인 기본 제공** |
| 리소스 사용 | N/A | 높음 | **경량 (JVM 튜닝 + Virtual Threads)** |
| 라이선스 비용 | 유료 | 무료 (CE) | **무료 (자체 개발)** |

---

## 2. 핵심 기능 정의

### 2.1 채널 & 메시지

- 공개 / 비공개 채널 생성 및 관리
- 1:1 다이렉트 메시지 (DM)
- 메시지 스레드 (Thread Reply)
- 메시지 편집 및 삭제
- 멘션 (`@사용자`, `#채널`)
- 이모지 반응 (Reaction)
- 핀 메시지 (Pin)
- 풀텍스트 검색 (PostgreSQL 기반)

### 2.2 개발자 특화 기능

- **코드 블록** — Shiki 기반 구문 강조, 복사 버튼, 언어 자동 감지
- **마크다운 렌더링** — 인라인 코드, 굵게, 기울임, 링크, 테이블
- **파일 첨부** — 이미지 미리보기, 코드 파일 인라인 렌더링
- **메시지 서식 툴바** — Bold / Italic / Code / Link / 파일 첨부

### 2.3 온프레미스 운영

- 회원가입(로컬 계정) 및 OAuth 소셜 로그인(Google · GitHub) 지원
- 관리자 패널 (멤버 관리, 채널 관리, 권한 설정)
- 백업 & 복구 스크립트 (PostgreSQL dump + 파일 볼륨)
- Docker Compose 기반 단일 명령 배포
- Spring Boot Actuator 기반 헬스체크 및 메트릭 노출

### 2.4 알림

- 데스크탑 네이티브 알림 (Electron Notification API)
- SSE(Server-Sent Events) 기반 실시간 알림 스트림
- 멘션 / 스레드 답글 / 채널 알림 설정

---

## 3. 기술 스택

### 3.1 프론트엔드

| 기술 | 버전 | 용도 |
| --- | --- | --- |
| Electron | 28+ | 데스크탑 앱 셸 |
| React | 19 | UI 프레임워크 |
| Vite | 5+ | 번들러 |
| Zustand | 4+ | 전역 상태 관리 |
| CSS Modules | - | 스타일링 (공통 컴포넌트 단위 스코프 CSS) |
| Shiki | 최신 | 코드 구문 강조 |
| STOMP.js | 최신 | WebSocket 클라이언트 |

### 3.2 백엔드

| 기술 | 버전  | 용도 |
| --- |-----| --- |
| Java | 21  | 백엔드 언어 (Virtual Threads 활용) |
| Spring Boot | 3.5 | 애플리케이션 프레임워크 |
| Spring WebSocket | -   | STOMP 기반 실시간 메시지 브로커 |
| Spring Security | -   | JWT 인증 + OAuth2 연동 |
| Spring Data JPA | -   | ORM (Hibernate) |
| Spring Data Redis | -   | 캐시 및 Pub/Sub |
| Spring Security OAuth2 Client | -   | Google · GitHub 소셜 로그인 |
| Gradle | 8   | 빌드 도구 |

### 3.3 데이터 & 인프라

| 기술 | 버전 | 용도 |
| --- | --- | --- |
| PostgreSQL | 16 | 메인 데이터베이스 |
| Flyway | 최신 | DB 스키마 버전 관리 |
| Redis | 7 | 세션 관리 · Pub/Sub 큐 |
| Docker Compose | - | 온프레미스 배포 패키지 |
| Nginx | - | 리버스 프록시 · TLS 종단 |
| Spring Actuator + Loki | - | 모니터링 · 로그 수집 |

### 3.4 테스트 & 품질

| 기술 | 용도 |
| --- | --- |
| JUnit 5 | 단위 테스트 |
| Mockito | Mock 프레임워크 |
| Testcontainers | 통합 테스트 (실제 DB 컨테이너) |
| Checkstyle | 코드 컨벤션 |
| JMeter | 부하 테스트 |
| Swagger / SpringDoc | API 문서 자동 생성 |

---

## 4. 시스템 아키텍처

### 4.1 레이어 구성

```
┌─────────────────────────────────────────────────────────┐
│  Client Layer                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Electron App │  │  Web Client  │  │  CLI Client  │  │
│  │ React + Vite │  │  React SPA   │  │  터미널 I/F  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼─────────────────┼─────────────────┼───────────┘
          └─────────────────┼─────────────────┘
                            │ REST + WebSocket (STOMP)
┌─────────────────────────────────────────────────────────┐
│  Backend Layer · Java 21 + Spring Boot 3.3              │
│  ┌─────────────────────────────────────────────────┐    │
│  │         Spring Boot API Gateway + Nginx         │    │
│  └──────┬──────────────┬──────────────┬────────────┘    │
│         │              │              │                  │
│  ┌──────▼──────┐ ┌─────▼──────┐ ┌────▼─────────┐        │
│  │  Message    │ │    Auth    │ │    File      │        │
│  │  Service   │ │  Service   │ │   Service    │        │
│  │ STOMP·WS   │ │ Sec·JWT   │ │ Multipart    │        │
│  └─────────────┘ └────────────┘ └─────────────┘        │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Notification Service  (SSE · Spring)            │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│  Data Layer                                             │
│  ┌────────────────┐  ┌────────────┐  ┌──────────────┐  │
│  │ PostgreSQL 16  │  │  Redis 7   │  │Local Storage │  │
│  │ JPA/Hibernate  │  │ 세션·Pub/Sub│  │파일·이미지    │  │
│  │ Flyway 마이그   │  │ Spring Data│  │ 온프레미스볼륨│  │
│  └────────────────┘  └────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 4.2 실시간 메시지 흐름

1. 클라이언트가 STOMP over WebSocket으로 서버에 연결
2. 사용자가 메시지 전송 → `/app/channel/{id}/send` 엔드포인트로 publish
3. Message Service가 메시지를 PostgreSQL에 저장
4. Redis Pub/Sub을 통해 동일 채널 구독자에게 브로드캐스트
5. 클라이언트는 `/topic/channel/{id}` 구독으로 메시지 수신

### 4.3 인증 흐름

1. 로컬 회원가입 또는 OAuth 소셜 로그인(Google · GitHub)으로 인증
2. Spring Security가 JWT Access Token (15분) + Refresh Token (7일) 발급
3. Refresh Token은 DB에 저장 (추후 Redis 전환 가능)
4. 이후 모든 API 요청은 Authorization 헤더의 JWT로 검증

---

## 5. 개발 로드맵

### Phase 1 — 코어 (1~4주)

목표: 기본 채널 메시지와 실시간 통신이 동작하는 MVP 완성

- Spring Boot 프로젝트 셋업 및 모노레포 구성
- STOMP WebSocket 서버 구현
- 채널 · DM · 메시지 CRUD REST API
- Spring Security + JWT 인증
- JPA 엔티티 설계 및 Flyway 스키마 마이그레이션
- Electron 앱 기본 레이아웃 (사이드바 · 채팅 영역)
- Redis Pub/Sub 연동 및 실시간 메시지 브로드캐스트

### Phase 2 — 개발자 특화 기능 (5~8주)

목표: 개발자 업무 흐름에 최적화된 기능 완성

- 코드 블록 렌더링 (Shiki 구문 강조, 복사 버튼)
- 마크다운 파싱 및 인라인 렌더링
- 스레드 (Thread Reply) 기능
- 파일 업로드 API (Spring Multipart, 로컬 볼륨 저장)
- @멘션 · #채널 파싱 및 알림 연동
- 이모지 반응 기능
- 풀텍스트 메시지 검색 (PostgreSQL tsvector)

### Phase 3 — 온프레미스 완성 (9~12주)

목표: 사내 서버에서 안정적으로 운영 가능한 상태 완성

- OAuth 소셜 로그인 연동 (Google · GitHub)
- Docker Compose 배포 패키지 (api + db + redis + nginx)
- 관리자 패널 (멤버 관리, 채널 관리, 권한 설정)
- 백업 · 복구 자동화 스크립트
- 데스크탑 네이티브 알림 (Electron Notification API)
- SSE 기반 실시간 알림 서비스 구현
- SSO 지원 (SAML 2.0, 선택 사항)

### Phase 4 — 완성 및 릴리즈 (13~16주)

목표: 프로덕션 배포 가능한 v1.0 릴리즈

- 보안 감사 및 취약점 점검 (OWASP Top 10 기준)
- 성능 최적화 및 부하 테스트 (JMeter, 동시 접속 500명 기준)
- Actuator 기반 모니터링 대시보드 (Grafana + Loki)
- Electron 자동 업데이트 (electron-updater)
- API 문서 자동 생성 (SpringDoc / Swagger UI)
- 운영 가이드 및 설치 매뉴얼 작성
- v1.0 정식 릴리즈

---

## 6. UI 구성 (화면 설계)

### 6.1 메인 레이아웃

```
┌──────────────────────────────────────────────────────────────┐
│  [사이드바 220px]  │  [채팅 영역 flex-1]  │  [우측 패널 230px] │
│                   │                      │                    │
│  워크스페이스명    │  # general ─────────  │  📌 핀 & 멤버      │
│                   │  12명                  │                    │
│  ─ Channels ─     │                      │  핀된 메시지        │
│  # general ◀      │  [메시지 피드]         │  ─ 온콜 순서       │
│  # backend  3     │                      │  ─ 보안 가이드      │
│  # frontend       │  [코드 블록]           │                    │
│  # devops         │  [이모지 반응]         │  멤버 목록 (12)    │
│  # code-review 1  │  [스레드 미리보기]     │  ─ 김정훈 Admin ●  │
│  # incident       │                      │  ─ 박예린 Dev       │
│                   │  ─ 입력 툴바 ─────    │  ─ 이민석 DevOps ● │
│  ─ Direct ─       │  [메시지 입력창]       │                    │
│  ● 김정훈          │                      │                    │
│  박예린            │                      │                    │
│  ● 이민석          │                      │                    │
│                   │                      │                    │
│  [내 프로필]       │                      │                    │
└──────────────────────────────────────────────────────────────┘
```

### 6.2 공통 UI 컴포넌트 (ui/)

컴포넌트마다 스타일을 개별 관리하면 관리 포인트가 너무 많아지기 때문에,
**CSS Modules 기반의 재사용 가능한 공통 컴포넌트 레이어**를 `components/ui/` 디렉토리에 구성한다.
화면 단위 컴포넌트는 이 공통 컴포넌트를 조립하여 구현하며, 직접 스타일을 작성하지 않는다.

| 컴포넌트 | 역할 | 주요 사용처 |
| --- | --- | --- |
| **Button** | 기본·아이콘·위험(destructive) 버튼 변형. variant·size props 제어 | 전체 |
| **Input** | 텍스트 입력 필드. 라벨·에러 메시지·disabled 상태 포함 | 로그인, 메시지 입력, 검색 |
| **Avatar** | 프로필 이미지 표시. 이미지 없을 시 이니셜 자동 생성 폴백 | MessageBubble, DmItem, MemberList |
| **Badge** | 안읽음 수 배지, 역할(Admin/Dev) 배지. 색상 variant 지원 | ChannelItem, MemberList |
| **Tooltip** | 호버 시 설명 텍스트 표시 | FormatToolbar, ReactionBar |
| **Modal** | 채널 생성·삭제 확인 다이얼로그. 오버레이·포커스 트랩 포함 | ChannelList, 설정 |
| **Divider** | 섹션 구분선. 텍스트 포함 variant 지원 | ChannelSidebar, RightPanel |
| **ScrollArea** | 커스텀 스크롤바 스타일 래퍼 | MessageFeed, ChannelList |
| **Spinner** | 로딩 인디케이터. size props 지원 | MessageFeed, 로그인 |

### 6.3 핵심 화면 컴포넌트

- **MessageBubble** — Avatar(공통), 이름, 시간, 텍스트, 코드 블록, 이모지 반응, 스레드 미리보기를 포함
- **CodeBlock** — 언어 감지, Shiki 렌더링, 복사 버튼
- **InputArea** — 서식 툴바 (B / I / `</>` / 링크 / 첨부) + 메시지 입력창
- **ChannelSidebar** — 채널 목록, Badge(공통) 안읽음 표시, DM 목록, 온라인 상태 표시
- **RightPanel** — 핀 메시지, 멤버 목록, 채널 상세 정보 (토글 가능)

---

## 7. 데이터 모델 (주요 테이블)

```sql
-- 사용자
users (id, username, email, display_name, avatar_url, oauth_provider, oauth_id, created_at)

-- 워크스페이스
workspaces (id, name, slug, owner_id, created_at)

-- 채널
channels (id, workspace_id, name, description, is_private, created_by, created_at)

-- 채널 멤버십
channel_members (channel_id, user_id, role, joined_at)

-- 메시지
messages (id, channel_id, user_id, content, parent_id, is_edited, created_at, updated_at)

-- 파일 첨부
attachments (id, message_id, file_name, file_size, mime_type, storage_path, created_at)

-- 이모지 반응
reactions (id, message_id, user_id, emoji, created_at)

-- 핀
pins (id, channel_id, message_id, pinned_by, created_at)
```

---

## 8. 배포 구성 (Docker Compose 예시)

```yaml
version:"3.9"

services:
api:
image: memomemo-api:latest
ports:
-"8080:8080"
environment:
- DB_HOST=postgres
- REDIS_HOST=redis
- JWT_SECRET=${JWT_SECRET}
- LDAP_URL=${LDAP_URL}
volumes:
- ./data/files:/app/files
depends_on:
- postgres
- redis

postgres:
image: postgres:16
environment:
- POSTGRES_DB=memomemo
- POSTGRES_USER=memomemo
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

---

## 9. 개발 팀 구성 (권장)

| 역할 | 담당 영역 | 권장 인원 |
| --- | --- | --- |
| 백엔드 개발 | Spring Boot API, WebSocket, 인증 | 2명 |
| 프론트엔드 개발 | React, Electron, UI 컴포넌트 | 2명 |
| DevOps | Docker 배포, 모니터링, CI/CD | 1명 |
| QA | 테스트 자동화, 보안 검토 | 1명 (겸업 가능) |

---

## 10. 향후 확장 계획

- **웹 클라이언트** — Electron 없이 브라우저에서 접근 가능한 React SPA
- **모바일 앱** — React Native 기반 iOS / Android 클라이언트
- **Webhook 연동** — GitHub, GitLab, Jira 이벤트를 채널에 수신
- **Bot API** — 사내 자동화 봇 개발을 위한 REST + WebSocket Bot API
- **화상회의** — WebRTC 기반 음성 · 영상 통화 (Phase 5 이후)

---

*문서 끝 — memomemo v0.1 기획서*

---

## 11. 용어 정리

| 용어 | 설명 |
| --- | --- |
| **워크스페이스 (Workspace)** | 팀 단위의 최상위 협업 공간. 채널과 멤버를 포함하며, 하나의 조직 또는 프로젝트에 대응한다. |
| **채널 (Channel)** | 워크스페이스 내 주제별 대화 공간. 공개(Public) 채널은 누구나 참여 가능하며, 비공개(Private) 채널은 초대된 멤버만 접근 가능하다. |
| **DM (Direct Message)** | 두 사용자 간의 1:1 비공개 메시지. 채널과 독립적으로 운영된다. |
| **스레드 (Thread)** | 특정 메시지에 대한 답글 모음. 채널의 메인 피드를 방해하지 않고 대화를 이어갈 수 있다. |
| **멘션 (Mention)** | `@사용자` 또는 `#채널` 형식으로 특정 대상을 지목하는 기능. 지목된 사용자에게 알림이 발송된다. |
| **이모지 반응 (Reaction)** | 메시지에 이모지로 반응을 남기는 기능. 별도 메시지 없이 감정이나 의견을 표현할 수 있다. |
| **핀 메시지 (Pin)** | 채널 내 중요 메시지를 고정하는 기능. 우측 패널에서 고정된 메시지 목록을 조회할 수 있다. |
| **코드 블록 (Code Block)** | 메시지 내 소스 코드를 구문 강조(Syntax Highlighting)와 함께 렌더링하는 영역. Shiki 기반으로 언어를 자동 감지한다. |
| **마크다운 (Markdown)** | 텍스트 서식 문법. 굵게, 기울임, 인라인 코드, 링크, 테이블 등을 지원한다. |
| **온프레미스 (On-premises)** | 외부 클라우드가 아닌 사내 서버에 직접 설치·운영하는 배포 방식. |
| **STOMP** | WebSocket 위에서 동작하는 메시지 프로토콜. 발행(publish)/구독(subscribe) 모델로 실시간 메시지를 처리한다. |
| **SSE (Server-Sent Events)** | 서버에서 클라이언트 방향으로 단방향 실시간 이벤트를 전달하는 HTTP 기반 프로토콜. 알림 스트림에 사용된다. |
| **JWT (JSON Web Token)** | 인증 정보를 담은 서명된 토큰. Access Token(15분)과 Refresh Token(7일)으로 구성되어 stateless 인증에 사용된다. |
| **OAuth2** | 소셜 로그인(Google · GitHub)에 사용되는 인증 위임 프로토콜. 사용자가 외부 계정으로 로그인할 수 있게 한다. |
| **Pub/Sub** | 발행자(Publisher)가 메시지를 채널에 발행하면 구독자(Subscriber)가 수신하는 메시지 패턴. Redis를 통해 채널 브로드캐스트에 활용한다. |
| **풀텍스트 검색 (Full-text Search)** | 메시지 내용 전체를 대상으로 키워드를 검색하는 기능. PostgreSQL의 `tsvector`를 기반으로 구현한다. |
| **common_code (공통코드)** | 역할, 상태, 알림 설정 등 시스템 전반의 코드성 데이터를 중앙에서 관리하는 테이블. `group_code_id` + `common_code` 복합키로 식별한다. |