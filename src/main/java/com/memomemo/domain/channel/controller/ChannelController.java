package com.memomemo.domain.channel.controller;

import com.memomemo.domain.channel.dto.ChannelMemberRequest;
import com.memomemo.domain.channel.dto.ChannelRequest;
import com.memomemo.domain.channel.dto.ChannelResponse;
import com.memomemo.domain.channel.dto.DmRequest;
import com.memomemo.domain.channel.service.ChannelService;
import com.memomemo.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping("/api/v1/workspaces/{workspaceId}/channels")
    public ResponseEntity<List<ChannelResponse>> getChannels(@PathVariable Long workspaceId,
                                                              @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(channelService.getChannels(workspaceId, authUser.id()));
    }

    @PostMapping("/api/v1/workspaces/{workspaceId}/channels")
    public ResponseEntity<ChannelResponse> createChannel(@PathVariable Long workspaceId,
                                                          @AuthenticationPrincipal AuthUser authUser,
                                                          @Valid @RequestBody ChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(channelService.createChannel(workspaceId, authUser.id(), request));
    }

    @PostMapping("/api/v1/workspaces/{workspaceId}/dm")
    public ResponseEntity<ChannelResponse> getOrCreateDm(@PathVariable Long workspaceId,
                                                          @AuthenticationPrincipal AuthUser authUser,
                                                          @Valid @RequestBody DmRequest request) {
        return ResponseEntity.ok(
                channelService.getOrCreateDm(workspaceId, authUser.id(), request.targetUserId()));
    }

    @GetMapping("/api/v1/channels/{channelId}")
    public ResponseEntity<ChannelResponse> getChannel(@PathVariable Long channelId,
                                                       @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(channelService.getChannel(channelId, authUser.id()));
    }

    @PutMapping("/api/v1/channels/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable Long channelId,
                                                          @AuthenticationPrincipal AuthUser authUser,
                                                          @Valid @RequestBody ChannelRequest request) {
        return ResponseEntity.ok(channelService.updateChannel(channelId, authUser.id(), request));
    }

    @DeleteMapping("/api/v1/channels/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId,
                                               @AuthenticationPrincipal AuthUser authUser) {
        channelService.deleteChannel(channelId, authUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/channels/{channelId}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long channelId,
                                           @AuthenticationPrincipal AuthUser authUser,
                                           @Valid @RequestBody ChannelMemberRequest request) {
        channelService.addMember(channelId, authUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/api/v1/channels/{channelId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long channelId,
                                              @PathVariable Long userId,
                                              @AuthenticationPrincipal AuthUser authUser) {
        channelService.removeMember(channelId, authUser.id(), userId);
        return ResponseEntity.noContent().build();
    }
}
