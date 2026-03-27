package com.memomemo.domain.message.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank String content,
        Long fileId
) {}
