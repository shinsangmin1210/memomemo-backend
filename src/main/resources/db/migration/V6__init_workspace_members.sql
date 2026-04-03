-- V6: workspace_members 테이블 생성

CREATE TABLE workspace_members (
    workspace_id BIGINT      NOT NULL REFERENCES workspaces (id) ON DELETE CASCADE,
    user_id      BIGINT      NOT NULL REFERENCES users (id)      ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- OWNER | ADMIN | MEMBER
    joined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (workspace_id, user_id)
);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);
