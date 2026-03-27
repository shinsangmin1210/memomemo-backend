package com.memomemo.domain.user.dto;

import com.memomemo.domain.user.entity.User;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String avatarUrl
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl()
        );
    }
}
