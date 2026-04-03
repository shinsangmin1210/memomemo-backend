package com.memomemo.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memomemo.domain.message.dto.NotificationEvent;
import com.memomemo.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper redisObjectMapper;
    private final NotificationService notificationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationEvent event = redisObjectMapper.readValue(
                    message.getBody(), NotificationEvent.class
            );
            // 해당 채널 구독자 전체에게 STOMP 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/channel/" + event.channelId(), event
            );

            // 발신자를 제외한 채널 멤버에게 SSE 알림 발송
            notificationService.sendToChannelMembers(
                    event.channelId(), event.userId(), event
            );
        } catch (Exception e) {
            log.error("Redis 메시지 처리 중 오류 발생", e);
        }
    }
}
