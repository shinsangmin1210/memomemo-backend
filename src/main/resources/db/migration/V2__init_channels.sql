-- V2: channels, channel_members 테이블 생성

CREATE TABLE channels (
    id           BIGSERIAL    PRIMARY KEY,
    workspace_id BIGINT       NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    is_private   BOOLEAN      NOT NULL DEFAULT false,
    created_by   BIGINT       NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_channels_workspace_id ON channels (workspace_id);
CREATE INDEX idx_channels_name         ON channels (name);


CREATE TABLE channel_members (
    channel_id BIGINT      NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users (id)    ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- OWNER | MEMBER
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (channel_id, user_id)
);
