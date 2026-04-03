-- V5: channels 테이블에 is_direct 컬럼 추가 (DM 채널 식별)

ALTER TABLE channels ADD COLUMN is_direct BOOLEAN NOT NULL DEFAULT false;

-- DM 채널 조회용 인덱스
CREATE INDEX idx_channels_direct ON channels (workspace_id, is_direct) WHERE is_direct = true;
