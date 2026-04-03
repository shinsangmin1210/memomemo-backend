package com.memomemo.domain.admin.controller;

import com.memomemo.domain.admin.dto.AdminChannelResponse;
import com.memomemo.domain.admin.dto.AdminRoleChangeRequest;
import com.memomemo.domain.admin.dto.AdminUserCreateRequest;
import com.memomemo.domain.admin.dto.AdminUserResponse;
import com.memomemo.domain.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ── 사용자 관리 ──

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(201).body(adminService.createUser(request));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<AdminUserResponse> changeRole(@PathVariable Long userId,
                                                         @Valid @RequestBody AdminRoleChangeRequest request) {
        return ResponseEntity.ok(adminService.changeRole(userId, request.role()));
    }

    // ── 채널 관리 ──

    @GetMapping("/workspaces/{workspaceId}/channels")
    public ResponseEntity<List<AdminChannelResponse>> getChannels(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(adminService.getChannels(workspaceId));
    }

    @DeleteMapping("/channels/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        adminService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }
}
