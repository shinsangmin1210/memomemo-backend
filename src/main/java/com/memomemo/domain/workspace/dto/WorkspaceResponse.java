package com.memomemo.domain.workspace.dto;

import com.memomemo.domain.workspace.entity.Workspace;

import java.time.OffsetDateTime;

public record WorkspaceResponse(
        Long id,
        String name,
        String slug,
        Long ownerId,
        OffsetDateTime createdAt
) {
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getOwner().getId(),
                workspace.getCreatedAt()
        );
    }
}
