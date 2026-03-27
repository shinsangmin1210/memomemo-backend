memo_memo_개발물량

memo_memo — 개발 물량 산정

> 작성일: 2026-03-26 | 기준: 설계서 v0.1
> 

---

## 목차

1. [프론트엔드 물량 (43개)](about:blank#1-%ED%94%84%EB%A1%A0%ED%8A%B8%EC%97%94%EB%93%9C-%EB%AC%BC%EB%9F%89-43%EA%B0%9C)
    - 1.1 [파일 목록 및 기능 설명](about:blank#11-%ED%8C%8C%EC%9D%BC-%EB%AA%A9%EB%A1%9D-%EB%B0%8F-%EA%B8%B0%EB%8A%A5-%EC%84%A4%EB%AA%85)
    - 1.2 [구현 순서](about:blank#12-%EA%B5%AC%ED%98%84-%EC%88%9C%EC%84%9C)
2. [백엔드 물량 (72개)](about:blank#2-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%AC%BC%EB%9F%89-72%EA%B0%9C)
    - 2.1 [Entity](about:blank#21-entity-8%EA%B0%9C)
    - 2.2 [Repository](about:blank#22-repository-8%EA%B0%9C)
    - 2.3 [Service](about:blank#23-service-9%EA%B0%9C)
    - 2.4 [Controller](about:blank#24-controller-8%EA%B0%9C)
    - 2.5 [DTO](about:blank#25-dto-17%EA%B0%9C)
    - 2.6 [WebSocket](about:blank#26-websocket-3%EA%B0%9C)
    - 2.7 [인증·보안](about:blank#27-%EC%9D%B8%EC%A6%9D%EB%B3%B4%EC%95%88-4%EA%B0%9C)
    - 2.8 [Config](about:blank#28-config-5%EA%B0%9C)
    - 2.9 [예외 처리](about:blank#29-%EC%98%88%EC%99%B8-%EC%B2%98%EB%A6%AC-4%EA%B0%9C)
    - 2.10 [Redis Pub/Sub](about:blank#210-redis-pubsub-2%EA%B0%9C)
    - 2.11 [Flyway 마이그레이션](about:blank#211-flyway-%EB%A7%88%EC%9D%B4%EA%B7%B8%EB%A0%88%EC%9D%B4%EC%85%98-sql-5%EA%B0%9C)
    - 2.12 [구현 순서](about:blank#212-%EA%B5%AC%ED%98%84-%EC%88%9C%EC%84%9C)
3. [전체 물량 요약](about:blank#3-%EC%A0%84%EC%B2%B4-%EB%AC%BC%EB%9F%89-%EC%9A%94%EC%95%BD)

---

## 1. 프론트엔드 물량 (43개)

### 1.1 파일 목록 및 기능 설명

### 공통 UI 컴포넌트 — ui/ (9개)

> 전체 화면에서 재사용되는 디자인 시스템 단위 컴포넌트. **가장 먼저 구현**해야 이후 화면 컴포넌트에서 조립식으로 사용 가능.
> 

| 파일 | 담당 기능 | 주요 사용처 |
| --- | --- | --- |
| `ui/Button.jsx` | 기본·아이콘·위험(destructive) 버튼 변형. variant·size props로 제어 | 전체 |
| `ui/Input.jsx` | 텍스트 입력 필드. 라벨·에러 메시지·disabled 상태 포함 | LoginPage, MessageInput, 검색 |
| `ui/Avatar.jsx` | 프로필 이미지 표시. 이미지 없을 시 이니셜 자동 생성 폴백 | MessageBubble, DmItem, MemberList, MyProfile |
| `ui/Badge.jsx` | 안읽음 수 배지, 역할(Admin/Dev) 배지. 색상 variant 지원 | ChannelItem, MemberList |
| `ui/Tooltip.jsx` | 호버 시 설명 텍스트 표시 | FormatToolbar, ReactionBar |
| `ui/Modal.jsx` | 채널 생성·삭제 확인 다이얼로그. 오버레이·포커스 트랩 포함 | ChannelList, 설정 |
| `ui/Divider.jsx` | 섹션 구분선. 텍스트 포함 variant 지원 | ChannelSidebar, RightPanel |
| `ui/ScrollArea.jsx` | 커스텀 스크롤바 스타일 래퍼 | MessageFeed, ChannelList |
| `ui/Spinner.jsx` | 로딩 인디케이터. size props 지원 | MessageFeed, LoginPage |

### 스타일 설정 (2개)

| 파일 | 담당 기능 |
| --- | --- |
| `index.css` | Tailwind 디렉티브(`@tailwind base/components/utilities`), 전역 스타일, 커스텀 스크롤바, 폰트 import |
| `tailwind.config.js` | 브랜드 컬러·다크모드·폰트(JetBrains Mono 코드블록용) 디자인 토큰 정의 |

### 앱 진입점 (2개)

| 파일 | 담당 기능 |
| --- | --- |
| `App.jsx` | 앱 루트. AuthProvider·Router 마운트, 전역 레이아웃 진입점 |
| `router.jsx` | React Router 라우팅 설정 (로그인/메인 경로 분기) |

### 인증 (2개)

| 파일 | 담당 기능 |
| --- | --- |
| `LoginPage.jsx` | 로그인 화면. ID/PW 입력 폼, LDAP·로컬 계정 로그인 요청 |
| `AuthProvider.jsx` | 인증 상태 초기화. accessToken 검증, 자동 Refresh 처리 |

### Zustand 전역 상태 Store (4개)

| 파일 | 담당 기능 |
| --- | --- |
| `authStore.js` | 로그인 사용자 정보·토큰 관리, login/logout 액션 |
| `channelStore.js` | 채널 목록·활성 채널 ID 관리, setActiveChannel 액션 |
| `messageStore.js` | 채널별 메시지 목록 관리, append·update·delete 액션 |
| `notificationStore.js` | 채널별 안읽음 카운트 관리, increment·reset 액션 |

### WebSocket 훅 (1개)

| 파일 | 담당 기능 |
| --- | --- |
| `useStompClient.js` | STOMP 연결 수립·해제, 채널 구독/해제, 토큰 만료 시 재연결 처리 |

### 레이아웃 (1개)

| 파일 | 담당 기능 |
| --- | --- |
| `MainLayout.jsx` | 사이드바·채팅영역·우측패널 3단 레이아웃 구성 |

### 사이드바 영역 (7개)

| 파일 | 담당 기능 |
| --- | --- |
| `ChannelSidebar.jsx` | 워크스페이스명·채널목록·DM목록·내 프로필을 포함하는 좌측 사이드바 컨테이너 |
| `WorkspaceName.jsx` | 워크스페이스 이름 표시 |
| `ChannelList.jsx` | 공개·비공개 채널 목록 렌더링, 채널 선택 이벤트 처리 |
| `ChannelItem.jsx` | 개별 채널 항목. 안읽음 메시지 배지 표시 |
| `DirectMessageList.jsx` | DM 대화 목록 렌더링 |
| `DmItem.jsx` | 개별 DM 항목. 상대방 온라인 상태(●) 표시 |
| `MyProfile.jsx` | 로그인 사용자 아바타·이름·상태 표시, 로그아웃 버튼 |

### 채팅 영역 (9개)

| 파일 | 담당 기능 |
| --- | --- |
| `ChatArea.jsx` | 채널 헤더·메시지 피드·입력 영역을 포함하는 메인 채팅 컨테이너 |
| `ChannelHeader.jsx` | 채널명·멤버 수·우측 패널 토글 버튼 표시 |
| `MessageFeed.jsx` | 메시지 목록 스크롤 뷰. 무한 스크롤(페이지네이션) 처리. `ui/ScrollArea` 사용 |
| `MessageBubble.jsx` | 단일 메시지 카드. `ui/Avatar`·작성자·시간·내용·반응·스레드 미리보기 포함 |
| `MessageContent.jsx` | 메시지 본문 렌더링. 마크다운·코드블록·일반 텍스트 분기 |
| `MarkdownRenderer.jsx` | 마크다운 파싱 및 렌더링 (굵게·기울임·링크·테이블·인라인 코드) |
| `CodeBlock.jsx` | Shiki 기반 코드 구문 강조 렌더링. 언어 자동 감지·복사 버튼 포함 |
| `ReactionBar.jsx` | 이모지 반응 목록 표시·추가·제거. 이모지 피커 포함. `ui/Tooltip` 사용 |
| `ThreadPreview.jsx` | 스레드 답글 수·최신 답글 미리보기. 클릭 시 ThreadDrawer 오픈 |

### 입력 영역 (3개)

| 파일 | 담당 기능 |
| --- | --- |
| `InputArea.jsx` | FormatToolbar·MessageInput을 포함하는 메시지 입력 컨테이너 |
| `FormatToolbar.jsx` | Bold·Italic·Code·Link·파일첨부 서식 버튼 툴바 |
| `MessageInput.jsx` | 메시지 텍스트 입력창. @멘션·#채널 자동완성, Enter 전송 처리 |

### 우측 패널 (3개)

| 파일 | 담당 기능 |
| --- | --- |
| `RightPanel.jsx` | 핀 메시지·멤버 목록을 포함하는 우측 패널 컨테이너. 토글 가능 |
| `PinnedMessages.jsx` | 핀된 메시지 목록 표시·핀 해제 기능 |
| `MemberList.jsx` | 채널 멤버 목록·온라인 상태·역할(Admin/Dev 등) 표시 |

### 스레드 (1개)

| 파일 | 담당 기능 |
| --- | --- |
| `ThreadDrawer.jsx` | 스레드 패널. 원본 메시지·답글 목록·답글 입력창 포함 |

---

### 1.2 구현 순서

```
1단계 — 스타일 기반
  tailwind.config.js → index.css

2단계 — 공통 UI 컴포넌트 (ui/)
  Button → Input → Avatar → Badge → Tooltip
  → Modal → Divider → ScrollArea → Spinner

3단계 — 상태 설계
  authStore / channelStore / messageStore / notificationStore

4단계 — 인증
  router.jsx → LoginPage.jsx → AuthProvider.jsx

5단계 — WebSocket
  useStompClient.js

6단계 — 레이아웃
  App.jsx → MainLayout.jsx

7단계 — 사이드바
  ChannelSidebar → WorkspaceName → ChannelList → ChannelItem
                → DirectMessageList → DmItem → MyProfile

8단계 — 채팅 영역
  ChatArea → ChannelHeader → MessageFeed → MessageBubble
          → MessageContent → MarkdownRenderer → CodeBlock
          → ReactionBar → ThreadPreview

9단계 — 입력 영역
  InputArea → FormatToolbar → MessageInput

10단계 — 우측 패널 & 스레드
  RightPanel → PinnedMessages → MemberList → ThreadDrawer
```

---

## 2. 백엔드 물량 (72개)

### 2.1 Entity (8개)

| 파일 | 담당 기능 |
| --- | --- |
| `User.java` | 사용자 엔티티. LDAP DN·로컬 비밀번호 해시 포함 |
| `Workspace.java` | 워크스페이스 엔티티 |
| `Channel.java` | 채널 엔티티. 공개·비공개 여부 포함 |
| `ChannelMember.java` | 채널 멤버십 엔티티. 복합 PK (channel_id, user_id) |
| `Message.java` | 메시지 엔티티. parent_id로 스레드 자기참조, tsvector 포함 |
| `Attachment.java` | 파일 첨부 엔티티. 저장 경로·MIME 타입 포함 |
| `Reaction.java` | 이모지 반응 엔티티. (message_id, user_id, emoji) 유니크 제약 |
| `Pin.java` | 핀 메시지 엔티티 |

### 2.2 Repository (8개)

| 파일 | 담당 기능 |
| --- | --- |
| `UserRepository.java` | username·email 조회 |
| `WorkspaceRepository.java` | slug 조회 |
| `ChannelRepository.java` | workspace 기준 채널 목록 조회 |
| `ChannelMemberRepository.java` | 채널 멤버십 확인·목록 조회 |
| `MessageRepository.java` | 채널별 페이지네이션 조회, tsvector 풀텍스트 검색 쿼리 포함 |
| `AttachmentRepository.java` | 메시지별 첨부파일 조회 |
| `ReactionRepository.java` | 메시지별 반응 목록 조회 |
| `PinRepository.java` | 채널별 핀 목록 조회 |

### 2.3 Service (9개)

| 파일 | 담당 기능 |
| --- | --- |
| `AuthService.java` | 로그인(LDAP·로컬), JWT 발급, Refresh Token 관리, 로그아웃 |
| `UserService.java` | 사용자 프로필 조회·수정 |
| `WorkspaceService.java` | 워크스페이스 조회 |
| `ChannelService.java` | 채널 CRUD, 멤버 추가·제거, 권한 검증 |
| `MessageService.java` | 메시지 저장·수정·삭제, 스레드 조회, 풀텍스트 검색 |
| `FileService.java` | 파일 유효성 검사, 로컬 볼륨 저장·삭제, 다운로드 스트림 |
| `NotificationService.java` | SseEmitter Map 관리, 이벤트 push, 연결 만료 처리 |
| `ReactionService.java` | 이모지 반응 추가·제거 |
| `PinService.java` | 메시지 핀·핀 해제, 채널별 핀 목록 조회 |

### 2.4 Controller (8개)

| 파일 | 담당 기능 |
| --- | --- |
| `AuthController.java` | `POST /auth/login`, `/refresh`, `/logout` |
| `UserController.java` | `GET /users/me`, `PUT /users/me`, `GET /users/{id}` |
| `WorkspaceController.java` | `GET /workspaces/{id}` |
| `ChannelController.java` | 채널 CRUD + 멤버 관리 엔드포인트 |
| `MessageController.java` | 메시지 REST(수정·삭제·핀·반응) + STOMP 메시지 핸들러 |
| `FileController.java` | `POST /files/upload`, `GET /files/{id}` |
| `NotificationController.java` | `GET /notifications/stream` (SSE) |
| `SearchController.java` | `GET /search/messages` |

### 2.5 DTO (17개)

| 파일 | 담당 기능 |
| --- | --- |
| `LoginRequest.java` | 로그인 요청 (username, password) |
| `LoginResponse.java` | 로그인 응답 (accessToken, refreshToken) |
| `TokenRefreshRequest.java` | Refresh Token 갱신 요청 |
| `TokenRefreshResponse.java` | 새 Access Token 응답 |
| `UserResponse.java` | 사용자 프로필 응답 |
| `UserUpdateRequest.java` | 프로필 수정 요청 |
| `ChannelRequest.java` | 채널 생성·수정 요청 |
| `ChannelResponse.java` | 채널 상세 응답 |
| `ChannelMemberRequest.java` | 채널 멤버 추가 요청 |
| `MessageRequest.java` | 메시지 전송 요청 |
| `MessageResponse.java` | 메시지 응답 (첨부파일·반응 포함) |
| `ThreadMessageRequest.java` | 스레드 답글 전송 요청 |
| `ReactionRequest.java` | 이모지 반응 추가 요청 |
| `FileUploadResponse.java` | 파일 업로드 응답 (fileId, fileUrl) |
| `SearchRequest.java` | 메시지 검색 요청 (keyword, channelId) |
| `SearchResponse.java` | 검색 결과 응답 |
| `NotificationEvent.java` | SSE·STOMP 알림 이벤트 페이로드 |

### 2.6 WebSocket (3개)

| 파일 | 담당 기능 |
| --- | --- |
| `StompMessageHandler.java` | `/app/channel/{id}/send` 수신, MessageService 호출 후 Redis publish |
| `StompChannelInterceptor.java` | STOMP CONNECT 시 JWT 검증, 인증 실패 시 연결 차단 |
| `WebSocketMessageBrokerConfig.java` | STOMP 브로커 설정, 엔드포인트·토픽 경로 등록 |

### 2.7 인증·보안 (4개)

| 파일 | 담당 기능 |
| --- | --- |
| `JwtTokenProvider.java` | Access·Refresh Token 생성·파싱·만료 검증 |
| `JwtAuthenticationFilter.java` | OncePerRequestFilter. Authorization 헤더 파싱 후 SecurityContext 설정 |
| `LdapAuthProvider.java` | Spring LDAP을 통해 사내 LDAP 서버에 인증 위임 |
| `SecurityConfig.java` | Spring Security 필터 체인. 경로별 인가 규칙, CORS, CSRF 설정 |

### 2.8 Config (5개)

| 파일 | 담당 기능 |
| --- | --- |
| `RedisConfig.java` | RedisTemplate 설정, Pub/Sub 리스너 등록 |
| `LdapConfig.java` | LDAP 서버 연결 설정 (URL·BaseDN·ManagerDN) |
| `CorsConfig.java` | 허용 Origin·메서드·헤더 설정 |
| `FileStorageConfig.java` | 파일 저장 루트 경로, 최대 크기·허용 MIME 타입 설정 |
| `ActuatorConfig.java` | `/actuator/health`, `/actuator/metrics` 노출 엔드포인트 설정 |

### 2.9 예외 처리 (4개)

| 파일 | 담당 기능 |
| --- | --- |
| `GlobalExceptionHandler.java` | `@ControllerAdvice`. 공통 에러 응답 포맷 반환 |
| `ResourceNotFoundException.java` | 404 — 채널·메시지·파일 등 리소스 없음 |
| `UnauthorizedException.java` | 401·403 — 인증·권한 오류 |
| `FileStorageException.java` | 파일 크기 초과·허용되지 않는 MIME 타입 등 파일 처리 오류 |

### 2.10 Redis Pub/Sub (2개)

| 파일 | 담당 기능 |
| --- | --- |
| `RedisMessagePublisher.java` | 메시지 저장 후 `channel:{channelId}` 키로 Redis publish |
| `RedisMessageSubscriber.java` | Redis 구독 후 수신된 메시지를 STOMP `/topic/channel/{id}`로 브로드캐스트 |

### 2.11 Flyway 마이그레이션 SQL (5개)

| 파일 | 담당 기능 |
| --- | --- |
| `V1__init_users_workspaces.sql` | users, workspaces 테이블 생성 |
| `V2__init_channels.sql` | channels, channel_members 테이블 생성 |
| `V3__init_messages_attachments.sql` | messages, attachments 테이블 생성 |
| `V4__init_reactions_pins.sql` | reactions, pins 테이블 생성 |
| `V5__add_search_vector_trigger.sql` | tsvector 컬럼·GIN 인덱스·자동 갱신 트리거 추가 |

---

### 2.12 구현 순서

```
Phase 1 — 데이터 기반
  Flyway SQL → Entity → Repository

Phase 2 — 인증
  SecurityConfig → JwtTokenProvider → JwtAuthenticationFilter
  → LdapAuthProvider → AuthService → AuthController

Phase 3 — 도메인 API
  UserService/Controller → WorkspaceService/Controller
  → ChannelService/Controller → MessageService/Controller

Phase 4 — 실시간 통신
  WebSocketMessageBrokerConfig → StompChannelInterceptor
  → StompMessageHandler → RedisMessagePublisher → RedisMessageSubscriber

Phase 5 — 부가 기능
  FileService/Controller → NotificationService/Controller(SSE)
  → SearchController → PinService → ReactionService
```

---

## 3. 전체 물량 요약

| 영역 | 분류 | 파일 수 |
| --- | --- | --- |
| **Frontend** | 공통 UI 컴포넌트 (ui/) | 9개 |
|  | 스타일 설정 | 2개 |
|  | 컴포넌트·Store·훅 | 32개 |
|  | **Frontend 소계** | **43개** |
| **Backend** | Entity | 8개 |
|  | Repository | 8개 |
|  | Service | 9개 |
|  | Controller | 8개 |
|  | DTO | 17개 |
|  | WebSocket | 3개 |
|  | 인증·보안 | 4개 |
|  | Config | 5개 |
|  | 예외 처리 | 4개 |
|  | Redis Pub/Sub | 2개 |
|  | Flyway SQL | 5개 |
|  | **Backend 소계** | **72개** |
| **합계** |  | **115개** |

---

*문서 끝 — GroundTalk 개발물량 산정서 v0.1*