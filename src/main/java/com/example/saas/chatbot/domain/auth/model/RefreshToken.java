package com.example.saas.chatbot.domain.auth.model;

import java.time.Instant;

public class RefreshToken {
    private Long id;
    private final String token;
    private final String userEmail;
    private final Instant expiresAt;
    private boolean revoked;

    public RefreshToken(String token, String userEmail, Instant expiresAt) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public RefreshToken(Long id, String token, String userEmail, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }
}
