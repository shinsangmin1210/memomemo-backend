package com.memomemo.domain.message.dto;

import jakarta.validation.constraints.NotBlank;

public record ThreadMessageRequest(
        @NotBlank String content
) {}
