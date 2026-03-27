package com.memomemo.domain.channel.entity;

import com.memomemo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "channel_members")
@IdClass(ChannelMemberId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChannelRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @PrePersist
    private void prePersist() {
        this.joinedAt = OffsetDateTime.now();
    }

    @Builder
    public ChannelMember(Channel channel, User user, ChannelRole role) {
        this.channel = channel;
        this.user = user;
        this.role = role;
    }

    public enum ChannelRole {
        OWNER, MEMBER
    }
}
