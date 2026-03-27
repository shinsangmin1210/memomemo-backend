package com.memomemo.domain.message.service;

import com.memomemo.domain.message.dto.ReactionRequest;
import com.memomemo.domain.message.entity.Message;
import com.memomemo.domain.message.entity.Reaction;
import com.memomemo.domain.message.repository.MessageRepository;
import com.memomemo.domain.message.repository.ReactionRepository;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addReaction(Long messageId, Long userId, ReactionRequest request) {
        if (reactionRepository.existsByMessageIdAndUserIdAndEmoji(
                messageId, userId, request.emoji())) {
            throw new IllegalArgumentException("이미 동일한 반응을 추가했습니다.");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("메시지", messageId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        reactionRepository.save(
                Reaction.builder()
                        .message(message)
                        .user(user)
                        .emoji(request.emoji())
                        .build()
        );
    }

    @Transactional
    public void removeReaction(Long messageId, Long userId, String emoji) {
        if (!reactionRepository.existsByMessageIdAndUserIdAndEmoji(messageId, userId, emoji)) {
            throw new ResourceNotFoundException("해당 반응을 찾을 수 없습니다.");
        }
        reactionRepository.deleteByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);
    }
}
