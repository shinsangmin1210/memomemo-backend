package com.memomemo.domain.channel.repository;

import com.memomemo.domain.channel.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findAllByWorkspaceId(Long workspaceId);

    // 특정 워크스페이스에서 사용자가 접근 가능한 채널 (공개 채널 + 멤버로 속한 비공개 채널)
    @Query("""
            SELECT c FROM Channel c
            WHERE c.workspace.id = :workspaceId
              AND (c.isPrivate = false
                   OR EXISTS (
                       SELECT 1 FROM ChannelMember cm
                       WHERE cm.channel = c AND cm.user.id = :userId
                   ))
            """)
    List<Channel> findAccessibleChannels(@Param("workspaceId") Long workspaceId,
                                         @Param("userId") Long userId);

    boolean existsByWorkspaceIdAndName(Long workspaceId, String name);

    // 두 사용자 간 기존 DM 채널 조회
    @Query("""
            SELECT c FROM Channel c
            WHERE c.workspace.id = :workspaceId
              AND c.isDirect = true
              AND EXISTS (SELECT 1 FROM ChannelMember cm1 WHERE cm1.channel = c AND cm1.user.id = :userA)
              AND EXISTS (SELECT 1 FROM ChannelMember cm2 WHERE cm2.channel = c AND cm2.user.id = :userB)
            """)
    java.util.Optional<Channel> findDmChannel(@Param("workspaceId") Long workspaceId,
                                               @Param("userA") Long userA,
                                               @Param("userB") Long userB);
}
