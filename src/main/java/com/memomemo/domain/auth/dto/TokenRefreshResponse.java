package com.memomemo.domain.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {}
