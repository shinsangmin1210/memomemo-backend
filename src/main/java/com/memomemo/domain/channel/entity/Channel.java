package com.memomemo.domain.channel.entity;

import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @Column(name = "is_direct", nullable = false)
    private boolean isDirect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    @Builder
    public Channel(Workspace workspace, String name, String description,
                   boolean isPrivate, boolean isDirect, User createdBy) {
        this.workspace = workspace;
        this.name = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.isDirect = isDirect;
        this.createdBy = createdBy;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
