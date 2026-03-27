package com.memomemo.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

//    @Column(name = "oauth_provider", length = 20)
//    private String oauthProvider;
//
//    @Column(name = "oauth_id", length = 255)
//    private String oauthId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

    // 로컬 회원가입용
    @Builder(builderMethodName = "localBuilder")
    public User(String username, String email, String displayName,
                String avatarUrl, String passwordHash, String role) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : "USER";
        this.isActive = true;
    }

    // OAuth 회원가입용
//    @Builder(builderMethodName = "oauthBuilder")
//    public User(String username, String email, String displayName,
//                String avatarUrl, String oauthProvider, String oauthId) {
//        this.username = username;
//        this.email = email;
//        this.displayName = displayName;
//        this.avatarUrl = avatarUrl;
//        this.oauthProvider = oauthProvider;
//        this.oauthId = oauthId;
//        this.isActive = true;
//    }

    public void updateProfile(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }

//    public boolean isOauthUser() {
//        return this.oauthProvider != null;
//    }
}
