package com.memomemo.domain.message.dto;

import java.time.OffsetDateTime;

public record NotificationEvent(
        String type,         // MESSAGE | THREAD_REPLY | MENTION
        Long channelId,
        Long messageId,
        Long userId,
        String displayName,
        String content,
        OffsetDateTime createdAt,
        MessageResponse.AttachmentInfo attachment
) {}
