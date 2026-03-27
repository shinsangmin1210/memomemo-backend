package com.memomemo.domain.channel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        boolean isPrivate
) {}
