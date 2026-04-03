package com.memomemo.domain.workspace.entity;

import com.memomemo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workspace_members")
@IdClass(WorkspaceMemberId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private WorkspaceRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @PrePersist
    private void prePersist() {
        this.joinedAt = OffsetDateTime.now();
    }

    @Builder
    public WorkspaceMember(Workspace workspace, User user, WorkspaceRole role) {
        this.workspace = workspace;
        this.user = user;
        this.role = role;
    }

    public enum WorkspaceRole {
        OWNER, ADMIN, MEMBER
    }
}
