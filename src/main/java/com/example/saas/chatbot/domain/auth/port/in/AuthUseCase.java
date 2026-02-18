package com.example.saas.chatbot.domain.auth.port.in;

import com.example.saas.chatbot.domain.auth.model.AuthToken;
import com.example.saas.chatbot.domain.auth.model.User;

public interface AuthUseCase {
    AuthToken login(String email, String password);
    User register(String email, String password);
    void logout(String accessToken, String refreshToken);
    AuthToken refresh(String refreshToken);
}
