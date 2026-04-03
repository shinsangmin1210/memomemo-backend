package com.memomemo.domain.workspace.controller;

import com.memomemo.domain.workspace.dto.WorkspaceInviteRequest;
import com.memomemo.domain.workspace.dto.WorkspaceMemberResponse;
import com.memomemo.domain.workspace.dto.WorkspaceResponse;
import com.memomemo.domain.workspace.dto.WorkspaceUpdateRequest;
import com.memomemo.domain.workspace.service.WorkspaceService;
import com.memomemo.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(workspaceService.getWorkspace(workspaceId));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long workspaceId,
                                                              @AuthenticationPrincipal AuthUser authUser,
                                                              @Valid @RequestBody WorkspaceUpdateRequest request) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, authUser.id(), request.name()));
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceMemberResponse>> getMembers(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(workspaceService.getMembers(workspaceId));
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<Void> inviteMember(@PathVariable Long workspaceId,
                                              @AuthenticationPrincipal AuthUser authUser,
                                              @Valid @RequestBody WorkspaceInviteRequest request) {
        workspaceService.inviteMember(workspaceId, authUser.id(), request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long workspaceId,
                                              @PathVariable Long userId,
                                              @AuthenticationPrincipal AuthUser authUser) {
        workspaceService.removeMember(workspaceId, authUser.id(), userId);
        return ResponseEntity.noContent().build();
    }
}
