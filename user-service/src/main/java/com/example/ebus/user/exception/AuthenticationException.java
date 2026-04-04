package com.example.ebus.user.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Invalid email or password");
    }
}
