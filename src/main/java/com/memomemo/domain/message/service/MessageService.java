package com.memomemo.domain.message.service;

import com.memomemo.domain.channel.entity.Channel;
import com.memomemo.domain.channel.repository.ChannelMemberRepository;
import com.memomemo.domain.channel.repository.ChannelRepository;
import com.memomemo.domain.message.dto.MessageRequest;
import com.memomemo.domain.message.dto.MessageResponse;
import com.memomemo.domain.message.dto.NotificationEvent;
import com.memomemo.domain.message.dto.ThreadMessageRequest;
import com.memomemo.domain.message.entity.Attachment;
import com.memomemo.domain.message.entity.Message;
import com.memomemo.domain.message.repository.AttachmentRepository;
import com.memomemo.domain.message.repository.MessageRepository;
import com.memomemo.domain.notification.service.NotificationService;
import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import com.memomemo.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int PAGE_SIZE = 50;
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Slice<MessageResponse> getMessages(Long channelId, Long cursor, Long userId) {
        checkMembership(channelId, userId);
        return messageRepository.findByChannelIdWithCursor(
                channelId, cursor, PageRequest.of(0, PAGE_SIZE)
        ).map(MessageResponse::from);
    }

    @Transactional
    public MessageResponse saveMessage(Long channelId, Long userId, MessageRequest request) {
        Channel channel = findChannel(channelId);
        User user = findUser(userId);
        checkMembership(channelId, userId);

        Message message = messageRepository.save(
                Message.builder()
                        .channel(channel)
                        .user(user)
                        .content(request.content())
                        .build()
        );

        Attachment attachment = null;
        if (request.fileId() != null) {
            attachment = attachmentRepository.findById(request.fileId())
                    .orElseThrow(() -> new ResourceNotFoundException("파일", request.fileId()));
            attachment.linkMessage(message);
        }

        MessageResponse response = MessageResponse.from(message, attachment);
        sendMentionNotifications(request.content(), message, user);
        return response;
    }

    @Transactional
    public MessageResponse saveThreadMessage(Long channelId, Long parentId,
                                             Long userId, ThreadMessageRequest request) {
        Channel channel = findChannel(channelId);
        User user = findUser(userId);
        checkMembership(channelId, userId);
        Message parent = findMessage(parentId);

        Message message = messageRepository.save(
                Message.builder()
                        .channel(channel)
                        .user(user)
                        .content(request.content())
                        .parent(parent)
                        .build()
        );

        MessageResponse response = MessageResponse.from(message);
        sendMentionNotifications(request.content(), message, user);
        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getThreadMessages(Long parentId) {
        return messageRepository.findAllByParentIdOrderByCreatedAtAsc(parentId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse editMessage(Long messageId, Long userId, MessageRequest request) {
        Message message = findMessage(messageId);
        checkAuthor(message, userId);
        message.edit(request.content());
        return MessageResponse.from(message);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = findMessage(messageId);
        checkAuthor(message, userId);
        messageRepository.delete(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> search(Long channelId, String keyword, Long userId) {
        checkMembership(channelId, userId);
        return messageRepository.searchByKeyword(channelId, keyword, 50).stream()
                .map(MessageResponse::from)
                .toList();
    }

    private Channel findChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("채널", channelId));
    }

    private Message findMessage(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("메시지", messageId));
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

    private void checkAuthor(Message message, Long userId) {
        if (!message.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("본인 메시지만 수정/삭제할 수 있습니다.");
        }
    }

    /**
     * 메시지 content에서 @username 패턴을 파싱하여 해당 사용자에게 MENTION SSE 알림을 발송한다.
     * 발신자 본인은 제외한다.
     */
    private void sendMentionNotifications(String content, Message message, User sender) {
        Set<String> mentionedUsernames = parseMentions(content);
        if (mentionedUsernames.isEmpty()) return;

        String preview = content.length() > 100 ? content.substring(0, 100) + "…" : content;

        for (String username : mentionedUsernames) {
            userRepository.findByUsername(username).ifPresent(mentionedUser -> {
                if (!mentionedUser.getId().equals(sender.getId())) {
                    NotificationEvent event = new NotificationEvent(
                            "MENTION",
                            message.getChannel().getId(),
                            message.getId(),
                            sender.getId(),
                            sender.getDisplayName(),
                            preview,
                            OffsetDateTime.now(),
                            null
                    );
                    notificationService.send(mentionedUser.getId(), event);
                }
            });
        }
    }

    private Set<String> parseMentions(String content) {
        Matcher matcher = MENTION_PATTERN.matcher(content);
        return matcher.results()
                .map(r -> r.group(1))
                .collect(Collectors.toSet());
    }
}
