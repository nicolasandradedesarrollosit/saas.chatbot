package com.example.saas.chatbot.domain.auth.exception;

import com.example.saas.chatbot.domain.shared.exception.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
