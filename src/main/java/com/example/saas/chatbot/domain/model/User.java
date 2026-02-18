package com.example.saas.chatbot.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long id;
    private String email;
    private String password;
    private Role role;
}