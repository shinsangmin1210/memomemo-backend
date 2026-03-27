-- V3: messages, attachments 테이블 생성

CREATE TABLE messages (
    id            BIGSERIAL    PRIMARY KEY,
    channel_id    BIGINT       NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    user_id       BIGINT       NOT NULL REFERENCES users (id),
    content       TEXT         NOT NULL,
    parent_id     BIGINT       REFERENCES messages (id) ON DELETE CASCADE,
    is_edited     BOOLEAN      NOT NULL DEFAULT false,
    search_vector TSVECTOR,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_channel_id    ON messages (channel_id);
CREATE INDEX idx_messages_parent_id     ON messages (parent_id);
CREATE INDEX idx_messages_search_vector ON messages USING GIN (search_vector);


CREATE TABLE attachments (
    id           BIGSERIAL    PRIMARY KEY,
    message_id   BIGINT       NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
    file_name    VARCHAR(255) NOT NULL,
    file_size    BIGINT       NOT NULL,
    mime_type    VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
