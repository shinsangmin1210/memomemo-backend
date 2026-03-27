package com.memomemo.domain.message.entity;

import com.memomemo.domain.channel.entity.Channel;
import com.memomemo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Message parent;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    // search_vector는 DB 트리거가 자동 관리하므로 insertable/updatable = false
    @Column(name = "search_vector", insertable = false, updatable = false,
            columnDefinition = "tsvector")
    private String searchVector;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @Builder
    public Message(Channel channel, User user, String content, Message parent) {
        this.channel = channel;
        this.user = user;
        this.content = content;
        this.parent = parent;
    }

    public void edit(String content) {
        this.content = content;
        this.isEdited = true;
    }
}
