package com.memomemo.domain.admin.service;

import com.memomemo.domain.admin.dto.AdminUserCreateRequest;
import com.memomemo.domain.admin.dto.AdminUserResponse;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse createUser(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 username입니다: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 email입니다: " + request.email());
        }

        User user = userRepository.save(
                User.localBuilder()
                        .username(request.username())
                        .email(request.email())
                        .displayName(request.displayName())
                        .passwordHash(passwordEncoder.encode(request.password()))
                        .role("USER")
                        .build()
        );

        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
        user.deactivate();
    }
}
