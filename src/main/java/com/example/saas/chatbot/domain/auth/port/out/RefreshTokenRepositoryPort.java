package com.example.saas.chatbot.domain.auth.port.out;

import com.example.saas.chatbot.domain.auth.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserEmail(String userEmail);
    void deleteExpiredTokens();
}
