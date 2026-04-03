-- V4: reactions, pins, refresh_tokens 테이블 생성

CREATE TABLE reactions (
    id         BIGSERIAL    PRIMARY KEY,
    message_id BIGINT       NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    user_id    BIGINT       NOT NULL REFERENCES users (id)    ON DELETE CASCADE,
    emoji      VARCHAR(50)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (message_id, user_id, emoji)
);

CREATE INDEX idx_reactions_message_id ON reactions (message_id);


CREATE TABLE pins (
    id         BIGSERIAL   PRIMARY KEY,
    channel_id BIGINT      NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    message_id BIGINT      NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    pinned_by  BIGINT      NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_pins_channel_id ON pins (channel_id);


CREATE TABLE refresh_tokens (
    user_id    BIGINT       PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL
);
