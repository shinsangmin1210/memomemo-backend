package com.memomemo.domain.channel.service;

import com.memomemo.domain.channel.entity.Channel;
import com.memomemo.domain.channel.entity.Pin;
import com.memomemo.domain.channel.repository.ChannelRepository;
import com.memomemo.domain.channel.repository.PinRepository;
import com.memomemo.domain.message.entity.Message;
import com.memomemo.domain.message.repository.MessageRepository;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PinService {

    private final PinRepository pinRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Pin> getPins(Long channelId) {
        return pinRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId);
    }

    @Transactional
    public void pin(Long channelId, Long messageId, Long userId) {
        if (pinRepository.existsByChannelIdAndMessageId(channelId, messageId)) {
            throw new IllegalArgumentException("이미 핀된 메시지입니다.");
        }

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("채널", channelId));
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("메시지", messageId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        pinRepository.save(
                Pin.builder()
                        .channel(channel)
                        .message(message)
                        .pinnedBy(user)
                        .build()
        );
    }

    @Transactional
    public void unpin(Long channelId, Long messageId) {
        if (!pinRepository.existsByChannelIdAndMessageId(channelId, messageId)) {
            throw new ResourceNotFoundException("핀 메시지를 찾을 수 없습니다.");
        }
        pinRepository.deleteByChannelIdAndMessageId(channelId, messageId);
    }
}
