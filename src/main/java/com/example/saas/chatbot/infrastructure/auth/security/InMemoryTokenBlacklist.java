package com.example.saas.chatbot.infrastructure.auth.security;

import com.example.saas.chatbot.domain.auth.port.out.TokenBlacklistPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklist implements TokenBlacklistPort {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token) {
        blacklist.put(token, Instant.now());
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanup() {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(oneHourAgo));
    }
}
