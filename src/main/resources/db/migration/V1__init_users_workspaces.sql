-- V1: users, workspaces 테이블 생성

CREATE TABLE users (
    id            BIGSERIAL       PRIMARY KEY,
    username      VARCHAR(50)     NOT NULL UNIQUE,
    email         VARCHAR(255)    NOT NULL UNIQUE,
    display_name  VARCHAR(100)    NOT NULL,
    avatar_url     VARCHAR(500),
    oauth_provider VARCHAR(20),
    oauth_id       VARCHAR(255),
    password_hash  VARCHAR(255),
    is_active     BOOLEAN         NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email    ON users (email);


CREATE TABLE workspaces (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    owner_id   BIGINT       NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
