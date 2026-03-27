package com.memomemo.domain.channel.repository;

import com.memomemo.domain.channel.entity.ChannelMember;
import com.memomemo.domain.channel.entity.ChannelMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

    List<ChannelMember> findAllByChannelId(Long channelId);

    Optional<ChannelMember> findByChannelIdAndUserId(Long channelId, Long userId);

    boolean existsByChannelIdAndUserId(Long channelId, Long userId);

    void deleteByChannelIdAndUserId(Long channelId, Long userId);
}
