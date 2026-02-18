package com.example.saas.chatbot.infrastructure.auth.adapter.in;

public class AuthRequest {
    public record RequestRegisterAndLogin(String email, String password) {}
}
