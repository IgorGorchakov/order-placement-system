package com.example.ebus.common.exception;

import com.example.ebus.common.dto.ErrorResponse;

import java.util.Collections;

/**
 * Abstract base class for all GlobalExceptionHandler implementations.
 * Provides shared utilities for building consistent error responses.
 */
public abstract class BaseExceptionHandler {

    /**
     * Creates a standardized error response for domain-specific exceptions.
     */
    protected ErrorResponse createErrorResponse(String errorCode, String message, String path) {
        return new ErrorResponse(errorCode, message, path);
    }

    /**
     * Creates a standardized error response with field-level validation details.
     */
    protected ErrorResponse createErrorResponse(String errorCode, String message, String path,
                                                 java.util.List<ErrorResponse.FieldError> fieldErrors) {
        return new ErrorResponse(errorCode, message, java.time.LocalDateTime.now(), path, fieldErrors);
    }
}
