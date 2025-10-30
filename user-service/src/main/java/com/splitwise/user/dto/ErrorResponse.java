package com.splitwise.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response for all API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private int status;
    private String error;
    private String message;
    private String path;

    // For validation errors
    private Map<String, String> fieldErrors;

    // For debugging (only in non-production)
    private String debugMessage;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse validationError(String message, Map<String, String> fieldErrors, String path) {
        return ErrorResponse.builder()
                .success(false)
                .status(400)
                .error("Validation Error")
                .message(message)
                .fieldErrors(fieldErrors)
                .path(path)
                .build();
    }
}
