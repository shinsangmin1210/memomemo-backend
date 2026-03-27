package com.memomemo.domain.message.dto;

import com.memomemo.domain.message.entity.Attachment;
import com.memomemo.domain.message.entity.Message;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long channelId,
        Long userId,
        String username,
        String displayName,
        String content,
        Long parentId,
        boolean isEdited,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        AttachmentInfo attachment
) {
    public record AttachmentInfo(
            Long fileId,
            String fileName,
            String fileUrl,
            String mimeType,
            Long fileSize
    ) {
        public static AttachmentInfo from(Attachment a) {
            return new AttachmentInfo(
                    a.getId(),
                    a.getFileName(),
                    "/api/v1/files/" + a.getId(),
                    a.getMimeType(),
                    a.getFileSize()
            );
        }
    }

    public static MessageResponse from(Message message) {
        return from(message, null);
    }

    public static MessageResponse from(Message message, Attachment attachment) {
        return new MessageResponse(
                message.getId(),
                message.getChannel().getId(),
                message.getUser().getId(),
                message.getUser().getUsername(),
                message.getUser().getDisplayName(),
                message.getContent(),
                message.getParent() != null ? message.getParent().getId() : null,
                message.isEdited(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                attachment != null ? AttachmentInfo.from(attachment) : null
        );
    }
}
