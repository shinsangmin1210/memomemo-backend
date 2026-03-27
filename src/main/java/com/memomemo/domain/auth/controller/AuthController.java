package com.memomemo.domain.auth.controller;

import com.memomemo.domain.auth.dto.LoginRequest;
import com.memomemo.domain.auth.dto.LoginResponse;
import com.memomemo.domain.auth.dto.TokenRefreshRequest;
import com.memomemo.domain.auth.dto.TokenRefreshResponse;
import com.memomemo.domain.auth.service.AuthService;
import com.memomemo.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthUser authUser) {
        authService.logout(authUser.id());
        return ResponseEntity.noContent().build();
    }
}
