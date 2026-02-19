package com.example.saas.chatbot.domain.auth.exception;

import com.example.saas.chatbot.domain.shared.exception.DomainException;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
