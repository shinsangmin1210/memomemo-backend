package com.memomemo.domain.admin.dto;

import com.memomemo.domain.user.entity.User;

import java.time.OffsetDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String role,
        boolean isActive,
        OffsetDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
