package com.memomemo.domain.channel.dto;

import jakarta.validation.constraints.NotNull;

public record DmRequest(
        @NotNull Long targetUserId
) {}
