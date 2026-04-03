package com.memomemo.domain.workspace.service;

import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.domain.workspace.dto.WorkspaceMemberResponse;
import com.memomemo.domain.workspace.dto.WorkspaceResponse;
import com.memomemo.domain.workspace.entity.Workspace;
import com.memomemo.domain.workspace.entity.WorkspaceMember;
import com.memomemo.domain.workspace.repository.WorkspaceMemberRepository;
import com.memomemo.domain.workspace.repository.WorkspaceRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import com.memomemo.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .map(WorkspaceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("워크스페이스", workspaceId));
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(Long workspaceId) {
        findWorkspace(workspaceId);
        return workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(WorkspaceMemberResponse::from)
                .toList();
    }

    @Transactional
    public void inviteMember(Long workspaceId, Long requesterId, Long targetUserId) {
        Workspace workspace = findWorkspace(workspaceId);
        checkMembership(workspaceId, requesterId);
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", targetUserId));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, targetUserId)) {
            throw new IllegalArgumentException("이미 워크스페이스 멤버입니다.");
        }

        workspaceMemberRepository.save(
                WorkspaceMember.builder()
                        .workspace(workspace)
                        .user(target)
                        .role(WorkspaceMember.WorkspaceRole.MEMBER)
                        .build()
        );
    }

    @Transactional
    public void removeMember(Long workspaceId, Long requesterId, Long targetUserId) {
        findWorkspace(workspaceId);
        checkOwnerOrAdmin(workspaceId, requesterId);

        if (requesterId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신을 제거할 수 없습니다.");
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, targetUserId);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, Long requesterId, String name) {
        Workspace workspace = findWorkspace(workspaceId);
        checkOwnerOrAdmin(workspaceId, requesterId);
        workspace.updateName(name);
        return WorkspaceResponse.from(workspace);
    }

    private Workspace findWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("워크스페이스", workspaceId));
    }

    private void checkMembership(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new UnauthorizedException("워크스페이스 멤버가 아닙니다.");
        }
    }

    private void checkOwnerOrAdmin(Long workspaceId, Long userId) {
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .filter(wm -> wm.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                           || wm.getRole() == WorkspaceMember.WorkspaceRole.ADMIN)
                .orElseThrow(() -> new UnauthorizedException("워크스페이스 관리자만 가능한 작업입니다."));
    }
}
