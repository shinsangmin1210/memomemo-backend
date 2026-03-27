package com.memomemo.domain.message.controller;

import com.memomemo.domain.channel.service.PinService;
import com.memomemo.domain.message.dto.MessageRequest;
import com.memomemo.domain.message.dto.MessageResponse;
import com.memomemo.domain.message.dto.ReactionRequest;
import com.memomemo.domain.message.dto.ThreadMessageRequest;
import com.memomemo.domain.message.service.MessageService;
import com.memomemo.domain.message.service.ReactionService;
import com.memomemo.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ReactionService reactionService;
    private final PinService pinService;

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<Slice<MessageResponse>> getMessages(
            @PathVariable Long channelId,
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(messageService.getMessages(channelId, cursor, authUser.id()));
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.editMessage(messageId, authUser.id(), request));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal AuthUser authUser) {
        messageService.deleteMessage(messageId, authUser.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/messages/{messageId}/threads")
    public ResponseEntity<List<MessageResponse>> getThreadMessages(
            @PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getThreadMessages(messageId));
    }

    // 이모지 반응
    @PostMapping("/messages/{messageId}/reactions")
    public ResponseEntity<Void> addReaction(
            @PathVariable Long messageId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ReactionRequest request) {
        reactionService.addReaction(messageId, authUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/messages/{messageId}/reactions/{emoji}")
    public ResponseEntity<Void> removeReaction(
            @PathVariable Long messageId,
            @PathVariable String emoji,
            @AuthenticationPrincipal AuthUser authUser) {
        reactionService.removeReaction(messageId, authUser.id(), emoji);
        return ResponseEntity.noContent().build();
    }

    // 핀
    @PostMapping("/messages/{messageId}/pin")
    public ResponseEntity<Void> pinMessage(
            @PathVariable Long messageId,
            @RequestParam Long channelId,
            @AuthenticationPrincipal AuthUser authUser) {
        pinService.pin(channelId, messageId, authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/messages/{messageId}/pin")
    public ResponseEntity<Void> unpinMessage(
            @PathVariable Long messageId,
            @RequestParam Long channelId) {
        pinService.unpin(channelId, messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/channels/{channelId}/pins")
    public ResponseEntity<?> getPins(
            @PathVariable Long channelId) {
        return ResponseEntity.ok(pinService.getPins(channelId));
    }
}
