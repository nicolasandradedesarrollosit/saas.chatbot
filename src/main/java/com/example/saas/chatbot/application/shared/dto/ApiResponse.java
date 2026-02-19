package com.example.saas.chatbot.application.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
        String type,
        String title,
        int status,
        String detail,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {}

    public static ApiResponse success(int status, String title, String detail) {
        return new ApiResponse(null, title, status, detail, null);
    }

    public static ApiResponse error(String type, String title, int status, String detail) {
        return new ApiResponse(type, title, status, detail, null);
    }

    public static ApiResponse validation(String detail, List<FieldError> fieldErrors) {
        return new ApiResponse("validation", "Validation Failed", 400, detail, fieldErrors);
    }
}
