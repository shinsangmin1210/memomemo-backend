// OAuth2 미사용 — 전체 주석 처리
//package com.memomemo.global.security;
//
//import com.memomemo.domain.auth.entity.RefreshToken;
//import com.memomemo.domain.auth.repository.RefreshTokenRepository;
//import com.memomemo.domain.user.entity.User;
//import com.memomemo.domain.user.repository.UserRepository;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.time.OffsetDateTime;
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final UserRepository userRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//
//    @Value("${jwt.refresh-token-expiry}")
//    private long refreshTokenExpiry;
//
//    @Value("${oauth2.redirect-uri:http://localhost:5173/oauth/callback}")
//    private String redirectUri;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//
//        String email = resolveEmail(oAuth2User);
//        String provider = resolveProvider(request);
//        String oauthId = resolveOauthId(oAuth2User);
//        String displayName = resolveDisplayName(oAuth2User);
//        String avatarUrl = resolveAvatarUrl(oAuth2User);
//
//        User user = userRepository.findByEmail(email)
//                .orElseGet(() -> registerOauthUser(email, provider, oauthId, displayName, avatarUrl));
//
//        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
//        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
//
//        saveRefreshToken(user.getId(), refreshToken);
//
//        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
//                .queryParam("accessToken", accessToken)
//                .queryParam("refreshToken", refreshToken)
//                .build().toUriString();
//
//        getRedirectStrategy().sendRedirect(request, response, targetUrl);
//    }
//
//    private User registerOauthUser(String email, String provider, String oauthId,
//                                   String displayName, String avatarUrl) {
//        String username = provider + "_" + oauthId;
//        return userRepository.save(
//                User.oauthBuilder()
//                        .username(username)
//                        .email(email)
//                        .displayName(displayName)
//                        .avatarUrl(avatarUrl)
//                        .oauthProvider(provider)
//                        .oauthId(oauthId)
//                        .build()
//        );
//    }
//
//    private void saveRefreshToken(Long userId, String token) {
//        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(refreshTokenExpiry);
//        refreshTokenRepository.findByUserId(userId)
//                .ifPresentOrElse(
//                        rt -> rt.rotate(token, expiresAt),
//                        () -> refreshTokenRepository.save(
//                                RefreshToken.builder()
//                                        .userId(userId)
//                                        .token(token)
//                                        .expiresAt(expiresAt)
//                                        .build()
//                        )
//                );
//    }
//
//    private String resolveEmail(OAuth2User oAuth2User) {
//        String email = oAuth2User.getAttribute("email");
//        if (email == null) {
//            throw new IllegalStateException("OAuth2 제공자로부터 이메일을 가져올 수 없습니다.");
//        }
//        return email;
//    }
//
//    private String resolveProvider(HttpServletRequest request) {
//        String uri = request.getRequestURI();
//        String[] parts = uri.split("/");
//        return parts[parts.length - 1];
//    }
//
//    private String resolveOauthId(OAuth2User oAuth2User) {
//        Object id = oAuth2User.getAttribute("sub");
//        if (id == null) {
//            id = oAuth2User.getAttribute("id");
//        }
//        return String.valueOf(id);
//    }
//
//    private String resolveDisplayName(OAuth2User oAuth2User) {
//        String name = oAuth2User.getAttribute("name");
//        return name != null ? name : "Unknown";
//    }
//
//    private String resolveAvatarUrl(OAuth2User oAuth2User) {
//        String avatar = oAuth2User.getAttribute("picture");
//        if (avatar == null) {
//            avatar = oAuth2User.getAttribute("avatar_url");
//        }
//        return avatar;
//    }
//}
