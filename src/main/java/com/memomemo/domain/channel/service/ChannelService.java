package com.memomemo.domain.channel.service;

import com.memomemo.domain.channel.dto.ChannelMemberRequest;
import com.memomemo.domain.channel.dto.ChannelRequest;
import com.memomemo.domain.channel.dto.ChannelResponse;
import com.memomemo.domain.channel.entity.Channel;
import com.memomemo.domain.channel.entity.ChannelMember;
import com.memomemo.domain.channel.repository.ChannelMemberRepository;
import com.memomemo.domain.channel.repository.ChannelRepository;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.domain.workspace.entity.Workspace;
import com.memomemo.domain.workspace.repository.WorkspaceRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import com.memomemo.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChannelResponse> getChannels(Long workspaceId, Long userId) {
        return channelRepository.findAccessibleChannels(workspaceId, userId).stream()
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChannelResponse getChannel(Long channelId, Long userId) {
        Channel channel = findById(channelId);
        checkMembership(channelId, userId);
        return ChannelResponse.from(channel);
    }

    @Transactional
    public ChannelResponse createChannel(Long workspaceId, Long userId, ChannelRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("워크스페이스", workspaceId));
        User user = findUser(userId);

        Channel channel = channelRepository.save(
                Channel.builder()
                        .workspace(workspace)
                        .name(request.name())
                        .description(request.description())
                        .isPrivate(request.isPrivate())
                        .createdBy(user)
                        .build()
        );

        channelMemberRepository.save(
                ChannelMember.builder()
                        .channel(channel)
                        .user(user)
                        .role(ChannelMember.ChannelRole.OWNER)
                        .build()
        );

        return ChannelResponse.from(channel);
    }

    @Transactional
    public ChannelResponse updateChannel(Long channelId, Long userId, ChannelRequest request) {
        Channel channel = findById(channelId);
        checkOwner(channelId, userId);
        channel.update(request.name(), request.description());
        return ChannelResponse.from(channel);
    }

    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        findById(channelId);
        checkOwner(channelId, userId);
        channelRepository.deleteById(channelId);
    }

    @Transactional
    public void addMember(Long channelId, Long requesterId, ChannelMemberRequest request) {
        Channel channel = findById(channelId);
        checkMembership(channelId, requesterId);
        User user = findUser(request.userId());

        if (channelMemberRepository.existsByChannelIdAndUserId(channelId, user.getId())) {
            throw new IllegalArgumentException("이미 채널 멤버입니다.");
        }

        channelMemberRepository.save(
                ChannelMember.builder()
                        .channel(channel)
                        .user(user)
                        .role(ChannelMember.ChannelRole.MEMBER)
                        .build()
        );
    }

    @Transactional
    public void removeMember(Long channelId, Long requesterId, Long targetUserId) {
        findById(channelId);
        checkOwner(channelId, requesterId);
        channelMemberRepository.deleteByChannelIdAndUserId(channelId, targetUserId);
    }

    private Channel findById(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("채널", channelId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));
    }

    private void checkMembership(Long channelId, Long userId) {
        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, userId)) {
            throw new UnauthorizedException("채널 멤버가 아닙니다.");
        }
    }

    private void checkOwner(Long channelId, Long userId) {
        channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
                .filter(cm -> cm.getRole() == ChannelMember.ChannelRole.OWNER)
                .orElseThrow(() -> new UnauthorizedException("채널 소유자만 가능한 작업입니다."));
    }
}
