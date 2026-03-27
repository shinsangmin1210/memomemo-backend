# memo_memo_setup

memo_memo Backend — 프로젝트 생성 및 초기 셋업 프로세스

> 기준 스택: Java 21 · Spring Boot 3.3 · Gradle 8 · PostgreSQL 16 · Redis 7
> 

---

## 사전 요구사항 확인

아래 항목이 로컬 또는 서버에 설치되어 있어야 한다.

| 도구 | 최소 버전 | 확인 명령 |
| --- | --- | --- |
| JDK | 21 | `java -version` |
| Gradle | 8.x | `gradle -version` (또는 Wrapper 사용 시 불필요) |
| Docker & Docker Compose | 최신 | `docker -v && docker compose version` |
| Git | 최신 | `git -version` |

---

## 1단계 — 디렉터리 구조 설계

```
groundtalk/
├── backend/                    ← Spring Boot 프로젝트 루트
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/groundtalk/
│   │   │   │   ├── GroundTalkApplication.java
│   │   │   │   ├── config/         ← Spring 설정 클래스
│   │   │   │   ├── domain/         ← JPA 엔티티
│   │   │   │   ├── repository/     ← Spring Data JPA Repository
│   │   │   │   ├── service/        ← 비즈니스 로직
│   │   │   │   ├── controller/     ← REST API 컨트롤러
│   │   │   │   ├── websocket/      ← STOMP WebSocket 핸들러
│   │   │   │   ├── security/       ← JWT · Spring Security
│   │   │   │   ├── dto/            ← Request / Response DTO
│   │   │   │   └── exception/      ← 공통 예외 처리
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-local.yml
│   │   │       └── db/migration/   ← Flyway SQL 파일
│   │   └── test/
│   │       └── java/com/groundtalk/
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradlew  (+ gradlew.bat)
├── frontend/                   ← React + Electron (추후)
├── docker/
│   ├── docker-compose.yml
│   ├── docker-compose.local.yml
│   └── nginx.conf
└── .gitignore
```

---

## 2단계 — Spring Boot 프로젝트 생성

### 방법 A: Spring Initializr (권장)

1. https://start.spring.io 접속
2. 아래 설정 입력:

| 항목 | 값 |
| --- | --- |
| Project | Gradle - Groovy |
| Language | Java |
| Spring Boot | 3.3.x |
| Group | com.groundtalk |
| Artifact | groundtalk-backend |
| Packaging | Jar |
| Java | 21 |
1. Dependencies 추가:

```
- Spring Web
- Spring WebSocket
- Spring Security
- Spring Data JPA
- Spring Data Redis (Reactive 아닌 일반)
- Spring LDAP
- Flyway Migration
- PostgreSQL Driver
- Lombok
- Spring Boot Actuator
- Spring Boot DevTools (개발용)
- Validation
```

1. GENERATE 클릭 → ZIP 다운로드 → `backend/` 디렉터리에 압축 해제

### 방법 B: Gradle CLI

```bash
mkdir -p groundtalk/backend && cd groundtalk/backend
gradle init \
  --type java-application \
  --dsl groovy \
  --java-version 21 \
  --project-name groundtalk-backend \
  --package com.groundtalk
```

초기화 후 `build.gradle`에 의존성을 수동으로 추가한다 (3단계 참고).

---

## 3단계 — build.gradle 의존성 설정

`backend/build.gradle` 전체 내용:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.0'
    id 'io.spring.dependency-management' version '1.1.5'
    id 'checkstyle'
}

group = 'com.groundtalk'
version = '0.1.0-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Web & WebSocket
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'

    // Spring Security + JWT
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'

    // Spring Data JPA + PostgreSQL
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'

    // Flyway
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'

    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // LDAP
    implementation 'org.springframework.boot:spring-boot-starter-data-ldap'

    // Actuator
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // API 문서 (SpringDoc / Swagger UI)
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // DevTools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'

    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'com.redis:testcontainers-redis:2.2.2'
}

dependencyManagement {
    imports {
        mavenBom "org.testcontainers:testcontainers-bom:1.19.8"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}

// Virtual Threads 활성화를 위해 JVM 옵션 설정
bootRun {
    jvmArgs = ['-Djdk.virtualThreadScheduler.parallelism=4']
}

checkstyle {
    toolVersion = '10.17.0'
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
}
```

---

## 4단계 — application.yml 설정

### `src/main/resources/application.yml` (공통 기본값)

```yaml
spring:
application:
name: groundtalk-backend

datasource:
url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:groundtalk}
username: ${DB_USER:groundtalk}
password: ${DB_PASSWORD:groundtalk}
driver-class-name: org.postgresql.Driver

jpa:
hibernate:
ddl-auto: validate          # Flyway가 스키마를 관리하므로 validate
show-sql:false
properties:
hibernate:
format_sql:true
dialect: org.hibernate.dialect.PostgreSQLDialect

flyway:
enabled:true
locations: classpath:db/migration
baseline-on-migrate:true

data:
redis:
host: ${REDIS_HOST:localhost}
port:6379
password: ${REDIS_PASSWORD:}

ldap:
urls: ${LDAP_URL:ldap://localhost:389}
base: ${LDAP_BASE:dc=groundtalk,dc=com}
username: ${LDAP_USER:cn=admin,dc=groundtalk,dc=com}
password: ${LDAP_PASSWORD:}

server:
port:8080
servlet:
context-path: /

management:
endpoints:
web:
exposure:
include: health,info,metrics,prometheus
endpoint:
health:
show-details: when_authorized

jwt:
secret: ${JWT_SECRET:change-this-in-production-min-32-chars}
access-token-expiry:900       # 15분 (초)
refresh-token-expiry:604800   # 7일 (초)

logging:
level:
com.groundtalk: DEBUG
org.springframework.security: INFO
org.hibernate.SQL: INFO
```

### `src/main/resources/application-local.yml` (로컬 개발 오버라이드)

```yaml
spring:
jpa:
show-sql:true

datasource:
url: jdbc:postgresql://localhost:5432/groundtalk

logging:
level:
com.groundtalk: DEBUG
org.hibernate.SQL: DEBUG
org.hibernate.type.descriptor.sql: TRACE
```

---

## 5단계 — 로컬 개발용 Docker Compose 구성

### `docker/docker-compose.local.yml`

```yaml
version:"3.9"

services:
postgres:
image: postgres:16
container_name: groundtalk-postgres
environment:
POSTGRES_DB: groundtalk
POSTGRES_USER: groundtalk
POSTGRES_PASSWORD: groundtalk
ports:
-"5432:5432"
volumes:
- postgres_data:/var/lib/postgresql/data
healthcheck:
test:["CMD-SHELL","pg_isready -U groundtalk"]
interval: 5s
timeout: 5s
retries:5

redis:
image: redis:7-alpine
container_name: groundtalk-redis
ports:
-"6379:6379"
volumes:
- redis_data:/data
healthcheck:
test:["CMD","redis-cli","ping"]
interval: 5s
timeout: 3s
retries:5

volumes:
postgres_data:
redis_data:
```

---

## 6단계 — Flyway 초기 스키마 작성

파일 경로: `src/main/resources/db/migration/V1__init_schema.sql`

파일명 규칙: `V{버전}__{설명}.sql` (언더스코어 2개)

```sql
-- =============================================
-- V1: 기초 스키마 생성
-- =============================================

-- 사용자
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),               -- LDAP 사용자는 NULL 허용
    avatar_url   VARCHAR(500),
    ldap_dn      VARCHAR(500),
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 워크스페이스
CREATE TABLE workspaces (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    owner_id   BIGINT       NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 채널
CREATE TABLE channels (
    id           BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT       NOT NULL REFERENCES workspaces(id),
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by   BIGINT       NOT NULL REFERENCES users(id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (workspace_id, name)
);

-- 채널 멤버십
CREATE TABLE channel_members (
    channel_id BIGINT      NOT NULL REFERENCES channels(id),
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    role       VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- MEMBER | ADMIN
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (channel_id, user_id)
);

-- 메시지
CREATE TABLE messages (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT      NOT NULL REFERENCES channels(id),
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    content    TEXT        NOT NULL,
    parent_id  BIGINT      REFERENCES messages(id),   -- 스레드 부모
    is_edited  BOOLEAN     NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 풀텍스트 검색용 인덱스
ALTER TABLE messages ADD COLUMN tsv TSVECTOR
    GENERATED ALWAYS AS (to_tsvector('simple', content)) STORED;
CREATE INDEX idx_messages_tsv ON messages USING GIN (tsv);
CREATE INDEX idx_messages_channel_created ON messages (channel_id, created_at DESC);

-- 파일 첨부
CREATE TABLE attachments (
    id           BIGSERIAL PRIMARY KEY,
    message_id   BIGINT       NOT NULL REFERENCES messages(id),
    file_name    VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    mime_type    VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 이모지 반응
CREATE TABLE reactions (
    id         BIGSERIAL PRIMARY KEY,
    message_id BIGINT      NOT NULL REFERENCES messages(id),
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    emoji      VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (message_id, user_id, emoji)
);

-- 핀 메시지
CREATE TABLE pins (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT      NOT NULL REFERENCES channels(id),
    message_id BIGINT      NOT NULL REFERENCES messages(id),
    pinned_by  BIGINT      NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (channel_id, message_id)
);
```

---

## 7단계 — 메인 애플리케이션 클래스 작성

`src/main/java/com/groundtalk/GroundTalkApplication.java`

```java
package com.groundtalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GroundTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroundTalkApplication.class, args);
    }
}
```

Virtual Threads 활성화는 `application.yml` 또는 별도 Config 클래스로 설정한다.

`src/main/java/com/groundtalk/config/VirtualThreadConfig.java`

```java
package com.groundtalk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    @Bean
    public java.util.concurrent.ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

---

## 8단계 — .gitignore 설정

프로젝트 루트 `.gitignore`:

```
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# IDE
.idea/
*.iml
.vscode/

# 환경변수
.env
.env.*
!.env.example

# 로그
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Docker 볼륨 데이터
docker/data/
```

---

## 9단계 — 초기 실행 순서

아래 순서대로 명령을 실행한다. (직접 실행하지 않고 순서를 확인용으로 사용)

```bash
# 1. 레포지터리 루트로 이동
cd groundtalk

# 2. 로컬 인프라 기동 (PostgreSQL + Redis)
docker compose -f docker/docker-compose.local.yml up -d

# 3. 컨테이너 헬스체크 확인
docker compose -f docker/docker-compose.local.yml ps

# 4. 백엔드 프로젝트 빌드 (테스트 제외)
cd backend
./gradlew build -x test

# 5. 로컬 프로파일로 서버 기동
./gradlew bootRun --args='--spring.profiles.active=local'

# 6. 기동 확인
curl http://localhost:8080/actuator/health
# 기대값: {"status":"UP"}

# 7. Swagger UI 확인
# 브라우저에서 http://localhost:8080/swagger-ui/index.html 접속
```

---

## 10단계 — 다음 구현 순서 (Phase 1 기준)

초기 셋업 완료 후 아래 순서로 구현을 진행한다.

1. **JWT 인증** — `JwtProvider`, `JwtFilter`, `SecurityConfig` 작성
2. **User 도메인** — `User` 엔티티, `UserRepository`, `AuthController` (로그인 / 토큰 재발급)
3. **Channel 도메인** — `Channel`, `ChannelMember` 엔티티, 채널 CRUD API
4. **Message 도메인** — `Message` 엔티티, 메시지 CRUD API
5. **WebSocket 설정** — `WebSocketConfig` (STOMP 브로커 설정), `MessageController`
6. **Redis Pub/Sub** — 채널별 메시지 브로드캐스트 구현
7. **통합 테스트** — Testcontainers 기반 슬라이스 테스트 작성

---

## 체크리스트

- [x]  JDK 21 설치 확인
- [ ]  Docker Desktop 실행 중 확인
- [ ]  `backend/` 디렉터리 생성 및 Spring Initializr ZIP 압축 해제
- [ ]  `build.gradle` 의존성 붙여넣기
- [ ]  `application.yml` / `application-local.yml` 작성
- [ ]  `docker/docker-compose.local.yml` 작성
- [ ]  `db/migration/V1__init_schema.sql` 작성
- [ ]  `.gitignore` 작성
- [ ]  `docker compose up -d` 로 로컬 DB 기동
- [ ]  `./gradlew build -x test` 빌드 성공 확인
- [ ]  `./gradlew bootRun` 서버 기동 및 `/actuator/health` 응답 확인