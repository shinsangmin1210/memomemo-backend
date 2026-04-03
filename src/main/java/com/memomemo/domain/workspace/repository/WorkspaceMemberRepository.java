package com.memomemo.domain.workspace.repository;

import com.memomemo.domain.workspace.entity.WorkspaceMember;
import com.memomemo.domain.workspace.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {

    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    void deleteByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}
