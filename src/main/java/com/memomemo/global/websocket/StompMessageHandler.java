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
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompMessageHandler {

    private static final int MAX_CONTENT_LENGTH = 10_000;

    private final MessageService messageService;
    private final RedisMessagePublisher redisMessagePublisher;

    // 채널 메시지 전송: /app/channel/{channelId}/send
    @MessageMapping("/channel/{channelId}/send")
    public void sendMessage(@DestinationVariable Long channelId,
                            MessageRequest request,
                            Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        validateContent(request.content());

        MessageResponse saved = messageService.saveMessage(channelId, authUser.id(), request);

        redisMessagePublisher.publish(channelId, toEvent("MESSAGE", saved));
    }

    // 스레드 메시지 전송: /app/channel/{channelId}/thread/{parentId}/send
    @MessageMapping("/channel/{channelId}/thread/{parentId}/send")
    public void sendThreadMessage(@DestinationVariable Long channelId,
                                   @DestinationVariable Long parentId,
                                   ThreadMessageRequest request,
                                   Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        validateContent(request.content());

        MessageResponse saved = messageService.saveThreadMessage(
                channelId, parentId, authUser.id(), request
        );

        redisMessagePublisher.publish(channelId, toEvent("THREAD_REPLY", saved));
    }

    // 메시지 편집: /app/channel/{channelId}/message/{messageId}/edit
    @MessageMapping("/channel/{channelId}/message/{messageId}/edit")
    public void editMessage(@DestinationVariable Long channelId,
                            @DestinationVariable Long messageId,
                            MessageRequest request,
                            Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        validateContent(request.content());

        MessageResponse updated = messageService.editMessage(messageId, authUser.id(), request);

        redisMessagePublisher.publish(channelId, toEvent("MESSAGE_EDIT", updated));
    }

    // 메시지 삭제: /app/channel/{channelId}/message/{messageId}/delete
    @MessageMapping("/channel/{channelId}/message/{messageId}/delete")
    public void deleteMessage(@DestinationVariable Long channelId,
                              @DestinationVariable Long messageId,
                              Authentication authentication) {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        messageService.deleteMessage(messageId, authUser.id());

        NotificationEvent event = new NotificationEvent(
                "MESSAGE_DELETE",
                channelId,
                messageId,
                authUser.id(),
                null,
                null,
                OffsetDateTime.now(),
                null
        );

        redisMessagePublisher.publish(channelId, event);
    }

    // STOMP 에러 핸들러 — 예외 발생 시 클라이언트의 /user/queue/errors 로 에러 메시지 전송
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        log.error("STOMP 메시지 처리 중 오류", ex);
        return ex.getMessage();
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어있을 수 없습니다.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("메시지 내용은 " + MAX_CONTENT_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private NotificationEvent toEvent(String type, MessageResponse saved) {
        return new NotificationEvent(
                type,
                saved.channelId(),
                saved.id(),
                saved.userId(),
                saved.displayName(),
                saved.content(),
                OffsetDateTime.now(),
                saved.attachment()
        );
    }
}
