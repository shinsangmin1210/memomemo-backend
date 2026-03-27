-- V6: refresh_tokens 테이블 생성 (Redis 대체, 추후 Redis 전환 가능)

CREATE TABLE refresh_tokens (
    user_id    BIGINT       PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL
);
