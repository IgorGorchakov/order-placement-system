package com.example.ebus.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Standardized error response for all API endpoints across services.
 * Prevents leakage of stack traces and internal implementation details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String errorCode,
    String message,
    LocalDateTime timestamp,
    String path,
    List<FieldError> fieldErrors
) {

    public record FieldError(
        String field,
        String rejectedValue,
        String message
    ) {}

    /**
     * Convenience constructor for simple errors without field-level details.
     */
    public ErrorResponse(String errorCode, String message, String path) {
        this(errorCode, message, LocalDateTime.now(), path, Collections.emptyList());
    }
}
