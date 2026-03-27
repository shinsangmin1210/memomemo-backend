package com.memomemo.domain.message.repository;

import com.memomemo.domain.message.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    List<Reaction> findAllByMessageId(Long messageId);

    Optional<Reaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    boolean existsByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    void deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}
