package com.memomemo.global.websocket;

import com.memomemo.global.security.AuthUser;
import com.memomemo.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = resolveToken(accessor);

        if (!StringUtils.hasText(token) || !jwtTokenProvider.isValid(token)) {
            log.warn("STOMP CONNECT: 유효하지 않은 JWT 토큰");
            throw new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        var claims = jwtTokenProvider.parseClaims(token);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        if (role == null) role = "USER";

        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthUser(userId, username, role),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        accessor.setUser(authentication);

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String bearer = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
