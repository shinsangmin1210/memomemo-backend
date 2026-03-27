-- V7: attachments.message_id NOT NULL 제약 제거
-- 파일을 메시지 전송 전에 먼저 업로드할 수 있도록 변경

ALTER TABLE attachments
    ALTER COLUMN message_id DROP NOT NULL;
