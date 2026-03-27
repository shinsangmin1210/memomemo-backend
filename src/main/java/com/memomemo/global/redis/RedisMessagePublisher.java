package com.memomemo.global.redis;

import com.memomemo.domain.message.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(Long channelId, NotificationEvent event) {
        redisTemplate.convertAndSend("channel:" + channelId, event);
    }
}
