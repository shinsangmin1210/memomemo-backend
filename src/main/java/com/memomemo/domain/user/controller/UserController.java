package com.memomemo.domain.user.controller;

import com.memomemo.domain.user.dto.UserResponse;
import com.memomemo.domain.user.dto.UserUpdateRequest;
import com.memomemo.domain.user.service.UserService;
import com.memomemo.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getMe(authUser.id()));
    }

    @PutMapping("/users/me")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal AuthUser authUser,
                                                  @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMe(authUser.id(), request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }
}
