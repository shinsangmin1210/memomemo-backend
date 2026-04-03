package com.memomemo.domain.admin.service;

import com.memomemo.domain.admin.dto.AdminChannelResponse;
import com.memomemo.domain.admin.dto.AdminUserCreateRequest;
import com.memomemo.domain.admin.dto.AdminUserResponse;
import com.memomemo.domain.channel.repository.ChannelRepository;
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
    private final ChannelRepository channelRepository;
    private final PasswordEncoder passwordEncoder;

    // ── 사용자 관리 ──

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
        User user = findUser(userId);
        user.deactivate();
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = findUser(userId);
        user.activate();
    }

    @Transactional
    public AdminUserResponse changeRole(Long userId, String role) {
        User user = findUser(userId);
        user.changeRole(role);
        return AdminUserResponse.from(user);
    }

    // ── 채널 관리 ──

    @Transactional(readOnly = true)
    public List<AdminChannelResponse> getChannels(Long workspaceId) {
        return channelRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(AdminChannelResponse::from)
                .toList();
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new ResourceNotFoundException("채널", channelId);
        }
        channelRepository.deleteById(channelId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
    }
}
