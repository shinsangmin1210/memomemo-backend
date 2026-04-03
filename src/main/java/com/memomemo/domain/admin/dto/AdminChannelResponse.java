package com.memomemo.domain.admin.dto;

import com.memomemo.domain.channel.entity.Channel;

import java.time.OffsetDateTime;

public record AdminChannelResponse(
        Long id,
        Long workspaceId,
        String name,
        String description,
        boolean isPrivate,
        boolean isDirect,
        String createdByUsername,
        OffsetDateTime createdAt
) {
    public static AdminChannelResponse from(Channel channel) {
        return new AdminChannelResponse(
                channel.getId(),
                channel.getWorkspace().getId(),
                channel.getName(),
                channel.getDescription(),
                channel.isPrivate(),
                channel.isDirect(),
                channel.getCreatedBy().getUsername(),
                channel.getCreatedAt()
        );
    }
}
