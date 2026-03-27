-- V4: reactions, pins 테이블 생성

CREATE TABLE reactions (
    id         BIGSERIAL   PRIMARY KEY,
    message_id BIGINT      NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users (id)    ON DELETE CASCADE,
    emoji      VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (message_id, user_id, emoji)
);


CREATE TABLE pins (
    id         BIGSERIAL   PRIMARY KEY,
    channel_id BIGINT      NOT NULL REFERENCES channels (id)  ON DELETE CASCADE,
    message_id BIGINT      NOT NULL REFERENCES messages (id)  ON DELETE CASCADE,
    pinned_by  BIGINT      NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
