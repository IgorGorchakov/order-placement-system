package com.example.ebus.user.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

        ResponseEntity<Map<String, String>> response = handler.handleUserNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "User not found with id: 5");
    }

    @Test
    void globalExceptionHandler_HandlesEmailAlreadyExistsException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@example.com");

        ResponseEntity<Map<String, String>> response = handler.handleEmailExists(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Email already in use: test@example.com");
    }

    @Test
    void globalExceptionHandler_HandlesAuthenticationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AuthenticationException ex = new AuthenticationException();

        ResponseEntity<Map<String, String>> response = handler.handleAuthFailure(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid email or password");
    }
}
