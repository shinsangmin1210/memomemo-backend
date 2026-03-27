package com.memomemo.domain.message.dto;

import java.util.List;

public record SearchResponse(
        String keyword,
        int totalCount,
        List<MessageResponse> results
) {}
