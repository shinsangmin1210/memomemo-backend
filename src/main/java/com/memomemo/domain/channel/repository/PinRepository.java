package com.memomemo.domain.channel.repository;

import com.memomemo.domain.channel.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findAllByChannelIdOrderByCreatedAtDesc(Long channelId);

    boolean existsByChannelIdAndMessageId(Long channelId, Long messageId);

    void deleteByChannelIdAndMessageId(Long channelId, Long messageId);
}
