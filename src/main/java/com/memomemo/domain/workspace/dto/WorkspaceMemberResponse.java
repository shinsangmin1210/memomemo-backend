package com.memomemo.domain.workspace.dto;

import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.workspace.entity.WorkspaceMember;

import java.time.OffsetDateTime;

public record WorkspaceMemberResponse(
        Long userId,
        String username,
        String displayName,
        String avatarUrl,
        String role,
        OffsetDateTime joinedAt
) {
    public static WorkspaceMemberResponse from(WorkspaceMember wm) {
        User user = wm.getUser();
        return new WorkspaceMemberResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                wm.getRole().name(),
                wm.getJoinedAt()
        );
    }
}
