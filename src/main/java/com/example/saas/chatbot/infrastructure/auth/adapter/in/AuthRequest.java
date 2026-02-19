package com.example.saas.chatbot.infrastructure.auth.adapter.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {
    public record RequestRegisterAndLogin(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Must be at least 8 characters")
            String password
    ) {}
}
