package com.memomemo.domain.notification.controller;

import com.memomemo.domain.notification.service.NotificationService;
import com.memomemo.global.security.AuthUser;
import com.memomemo.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * SSE 알림 스트림.
     * EventSource는 Authorization 헤더를 지원하지 않으므로
     * 쿼리 파라미터 ?token=... 으로도 인증을 허용한다.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) String token) {

        Long userId;
        if (authUser != null) {
            userId = authUser.id();
        } else if (token != null && jwtTokenProvider.isValid(token)) {
            userId = jwtTokenProvider.getUserId(token);
        } else {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(notificationService.subscribe(userId));
    }
}
