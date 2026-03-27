memo_memo — Backend TODO List
> 기준: 개발물량 산정서 v0.1 | 전체: 72개

---

## Phase 1 — 데이터 기반

#### Flyway SQL (5개)
- [x] `V1__init_users_workspaces.sql` — users, workspaces 테이블 생성
- [x] `V2__init_channels.sql` — channels, channel_members 테이블 생성
- [x] `V3__init_messages_attachments.sql` — messages, attachments 테이블 생성
- [x] `V4__init_reactions_pins.sql` — reactions, pins 테이블 생성
- [x] `V5__add_search_vector_trigger.sql` — tsvector 컬럼·GIN 인덱스·자동 갱신 트리거

#### Entity (8개)
- [x] `User.java`
- [x] `Workspace.java`
- [x] `Channel.java`
- [x] `ChannelMember.java`
- [x] `Message.java`
- [x] `Attachment.java`
- [x] `Reaction.java`
- [x] `Pin.java`

#### Repository (8개)
- [x] `UserRepository.java`
- [x] `WorkspaceRepository.java`
- [x] `ChannelRepository.java`
- [x] `ChannelMemberRepository.java`
- [x] `MessageRepository.java`
- [x] `AttachmentRepository.java`
- [x] `ReactionRepository.java`
- [x] `PinRepository.java`

---

## Phase 2 — 인증·보안

#### Config / Security (4개)
- [x] `SecurityConfig.java`
- [x] `JwtTokenProvider.java`
- [x] `JwtAuthenticationFilter.java`
- [x] `OAuth2SuccessHandler.java` — OAuth 로그인 성공 후 JWT 발급 처리

#### Auth (2개)
- [x] `AuthService.java`
- [x] `AuthController.java`

#### DTO — 인증 (4개)
- [x] `LoginRequest.java`
- [x] `LoginResponse.java`
- [x] `TokenRefreshRequest.java`
- [x] `TokenRefreshResponse.java`

---

## Phase 3 — 도메인 API

#### User (4개)
- [x] `UserService.java`
- [x] `UserController.java`
- [x] `UserResponse.java`
- [x] `UserUpdateRequest.java`

#### Workspace (2개)
- [x] `WorkspaceService.java`
- [x] `WorkspaceController.java`

#### Channel (5개)
- [x] `ChannelService.java`
- [x] `ChannelController.java`
- [x] `ChannelRequest.java`
- [x] `ChannelResponse.java`
- [x] `ChannelMemberRequest.java`

#### Message (5개)
- [x] `MessageService.java`
- [x] `MessageController.java`
- [x] `MessageRequest.java`
- [x] `MessageResponse.java`
- [x] `ThreadMessageRequest.java`

#### 예외 처리 (4개)
- [x] `GlobalExceptionHandler.java`
- [x] `ResourceNotFoundException.java`
- [x] `UnauthorizedException.java`
- [x] `FileStorageException.java`

---

## Phase 4 — 실시간 통신

#### Config (1개)
- [x] `RedisConfig.java`

#### WebSocket (3개)
- [x] `WebSocketMessageBrokerConfig.java`
- [x] `StompChannelInterceptor.java`
- [x] `StompMessageHandler.java`

#### Redis Pub/Sub (2개)
- [x] `RedisMessagePublisher.java`
- [x] `RedisMessageSubscriber.java`

#### DTO (1개)
- [x] `NotificationEvent.java`

---

## Phase 5 — 부가 기능

#### File (4개)
- [x] `FileStorageConfig.java`
- [x] `FileService.java`
- [x] `FileController.java`
- [x] `FileUploadResponse.java`

#### Notification SSE (2개)
- [x] `NotificationService.java`
- [x] `NotificationController.java`

#### Search (3개)
- [x] `SearchController.java`
- [x] `SearchRequest.java`
- [x] `SearchResponse.java`

#### Reaction (2개)
- [x] `ReactionService.java`
- [x] `ReactionRequest.java`

#### Pin (1개)
- [x] `PinService.java`

#### 기타 Config (2개)
- [x] `CorsConfig.java`
- [x] `ActuatorConfig.java`
- ~~`LdapConfig.java`~~ — LDAP 제거로 불필요

---

## 진행 현황

| Phase | 전체 | 완료 | 진행률 |
|-------|------|------|--------|
| Phase 1 — 데이터 기반 | 21 | 21 | 100% |
| Phase 2 — 인증·보안 | 10 | 10 | 100% |
| Phase 3 — 도메인 API | 20 | 20 | 100% |
| Phase 4 — 실시간 통신 | 7 | 7 | 100% |
| Phase 5 — 부가 기능 | 14 | 14 | 100% |
| **합계** | **72** | **72** | **100%** |
