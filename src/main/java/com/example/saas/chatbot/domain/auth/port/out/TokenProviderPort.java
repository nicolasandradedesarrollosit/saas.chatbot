package com.example.saas.chatbot.domain.auth.port.out;

import com.example.saas.chatbot.domain.auth.model.User;

public interface TokenProviderPort {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String extractEmail(String token);
    boolean isTokenValid(String token);
    boolean isTokenExpired(String token);
}
