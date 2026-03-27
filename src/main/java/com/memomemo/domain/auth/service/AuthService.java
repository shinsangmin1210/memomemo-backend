package com.memomemo.domain.auth.service;

import com.memomemo.domain.auth.dto.LoginRequest;
import com.memomemo.domain.auth.dto.LoginResponse;
import com.memomemo.domain.auth.dto.TokenRefreshRequest;
import com.memomemo.domain.auth.dto.TokenRefreshResponse;
import com.memomemo.domain.auth.entity.RefreshToken;
import com.memomemo.domain.auth.repository.RefreshTokenRepository;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        authenticate(user, request.password());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        if (!jwtTokenProvider.isValid(request.refreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 존재하지 않습니다."));

        if (stored.isExpired() || !stored.getToken().equals(request.refreshToken())) {
            throw new IllegalArgumentException("Refresh Token이 만료되었거나 일치하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        stored.rotate(newRefreshToken, OffsetDateTime.now().plusSeconds(refreshTokenExpiry));

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private void authenticate(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void saveRefreshToken(Long userId, String token) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(refreshTokenExpiry);
        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        rt -> rt.rotate(token, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(token)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }
}
