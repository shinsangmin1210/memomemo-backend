package com.memomemo.domain.notification.service;

import com.memomemo.domain.channel.entity.ChannelMember;
import com.memomemo.domain.channel.repository.ChannelMemberRepository;
import com.memomemo.domain.message.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    // userId → SseEmitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ChannelMemberRepository channelMemberRepository;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        // 연결 확인용 초기 이벤트
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    public void send(Long userId, NotificationEvent event) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(event.type())
                    .data(event));
        } catch (IOException e) {
            emitters.remove(userId);
            log.debug("SSE 전송 실패 - userId: {}", userId);
        }
    }

    /**
     * 채널 멤버 중 발신자를 제외한 모든 사용자에게 SSE 알림을 발송한다.
     */
    public void sendToChannelMembers(Long channelId, Long senderId, NotificationEvent event) {
        List<ChannelMember> members = channelMemberRepository.findAllByChannelId(channelId);
        for (ChannelMember member : members) {
            Long memberId = member.getUser().getId();
            if (!memberId.equals(senderId)) {
                send(memberId, event);
            }
        }
    }
}
