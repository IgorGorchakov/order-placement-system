package com.example.ebus.user.exception;

import com.example.ebus.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserExceptionsTest {

    @Test
    void userNotFoundException_HasCorrectMessage() {
        UserNotFoundException ex = new UserNotFoundException(42L);

        assertThat(ex).hasMessage("User not found with id: 42");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void emailAlreadyExistsException_HasCorrectMessage() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@example.com");

        assertThat(ex).hasMessage("Email already in use: test@example.com");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void authenticationException_HasCorrectMessage() {
        AuthenticationException ex = new AuthenticationException();

        assertThat(ex).hasMessage("Invalid email or password");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void globalExceptionHandler_HandlesUserNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UserNotFoundException ex = new UserNotFoundException(5L);
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("USER_NOT_FOUND");
            assertThat(body.message()).isEqualTo("User not found with id: 5");
            assertThat(body.path()).isEqualTo("/api/users/5");
        });
    }

    @Test
    void globalExceptionHandler_HandlesEmailAlreadyExistsException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@example.com");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleEmailExists(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
            assertThat(body.message()).isEqualTo("Email already in use: test@example.com");
        });
    }

    @Test
    void globalExceptionHandler_HandlesAuthenticationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AuthenticationException ex = new AuthenticationException();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleAuthFailure(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("AUTHENTICATION_FAILED");
            assertThat(body.message()).isEqualTo("Invalid email or password");
        });
    }

    @Test
    void globalExceptionHandler_HandlesGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new RuntimeException("Something went wrong");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("INTERNAL_ERROR");
            assertThat(body.message()).isEqualTo("An unexpected error occurred");
        });
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users/5");
        return request;
    }
}
