package com.memomemo.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SearchRequest(
        @NotNull Long channelId,
        @NotBlank String keyword
) {}
