package com.memomemo.global.websocket;

import com.memomemo.domain.message.dto.MessageRequest;
import com.memomemo.domain.message.dto.MessageResponse;
import com.memomemo.domain.message.dto.NotificationEvent;
import com.memomemo.domain.message.dto.ThreadMessageRequest;
import com.memomemo.domain.message.service.MessageService;
import com.memomemo.global.redis.RedisMessagePublisher;
import com.memomemo.global.security.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompMessageHandler {

    private final MessageService messageService;
    private final RedisMessagePublisher redisMessagePublisher;

    // 채널 메시지 전송: /app/channel/{channelId}/send
    @MessageMapping("/channel/{channelId}/send")
    public void sendMessage(@DestinationVariable Long channelId,
                            MessageRequest request,
                            Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        MessageResponse saved = messageService.saveMessage(channelId, authUser.id(), request);

        NotificationEvent event = new NotificationEvent(
                "MESSAGE",
                saved.channelId(),
                saved.id(),
                saved.userId(),
                saved.displayName(),
                saved.content(),
                OffsetDateTime.now(),
                saved.attachment()
        );

        redisMessagePublisher.publish(channelId, event);
    }

    // 스레드 메시지 전송: /app/channel/{channelId}/thread/{parentId}/send
    @MessageMapping("/channel/{channelId}/thread/{parentId}/send")
    public void sendThreadMessage(@DestinationVariable Long channelId,
                                   @DestinationVariable Long parentId,
                                   ThreadMessageRequest request,
                                   Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        MessageResponse saved = messageService.saveThreadMessage(
                channelId, parentId, authUser.id(), request
        );

        NotificationEvent event = new NotificationEvent(
                "THREAD_REPLY",
                saved.channelId(),
                saved.id(),
                saved.userId(),
                saved.displayName(),
                saved.content(),
                OffsetDateTime.now(),
                saved.attachment()
        );

        redisMessagePublisher.publish(channelId, event);
    }
}
