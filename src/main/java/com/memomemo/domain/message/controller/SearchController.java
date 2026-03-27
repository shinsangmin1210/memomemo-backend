package com.memomemo.domain.message.controller;

import com.memomemo.domain.message.dto.SearchResponse;
import com.memomemo.domain.message.service.MessageService;
import com.memomemo.global.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final MessageService messageService;

    @GetMapping("/messages")
    public ResponseEntity<SearchResponse> searchMessages(
            @RequestParam Long channelId,
            @RequestParam String q,
            @AuthenticationPrincipal AuthUser authUser) {
        var results = messageService.search(channelId, q, authUser.id());
        return ResponseEntity.ok(new SearchResponse(q, results.size(), results));
    }
}
