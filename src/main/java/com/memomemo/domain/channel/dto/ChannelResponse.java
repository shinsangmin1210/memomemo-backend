package com.memomemo.domain.channel.dto;

import com.memomemo.domain.channel.entity.Channel;

public record ChannelResponse(
        Long id,
        Long workspaceId,
        String name,
        String description,
        boolean isPrivate,
        Long createdById
) {
    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getWorkspace().getId(),
                channel.getName(),
                channel.getDescription(),
                channel.isPrivate(),
                channel.getCreatedBy().getId()
        );
    }
}
