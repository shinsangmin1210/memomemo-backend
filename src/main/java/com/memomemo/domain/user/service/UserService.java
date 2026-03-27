package com.memomemo.domain.user.service;

import com.memomemo.domain.user.dto.UserResponse;
import com.memomemo.domain.user.dto.UserUpdateRequest;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import com.memomemo.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return UserResponse.from(findById(userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userRepository.findAllByIsActiveTrue().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = findById(userId);
        user.updateProfile(request.displayName(), request.avatarUrl());
        return UserResponse.from(user);
    }

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
    }
}
