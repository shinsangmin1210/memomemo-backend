package com.memomemo.domain.channel.dto;

import jakarta.validation.constraints.NotNull;

public record ChannelMemberRequest(
        @NotNull Long userId
) {}
