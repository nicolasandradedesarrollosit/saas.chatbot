package com.example.saas.chatbot.domain.auth.port.in;

import com.example.saas.chatbot.domain.auth.model.User;

public interface AuthUseCase {
    String login(String email, String password);
    User register(String email, String password);
}
