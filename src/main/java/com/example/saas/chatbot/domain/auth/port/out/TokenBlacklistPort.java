package com.example.saas.chatbot.domain.auth.port.out;

public interface TokenBlacklistPort {
    void blacklist(String token);
    boolean isBlacklisted(String token);
}
