package com.memomemo.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}
