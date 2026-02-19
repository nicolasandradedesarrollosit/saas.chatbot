package com.example.saas.chatbot.domain.auth.exception;

import com.example.saas.chatbot.domain.shared.exception.DomainException;

public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
