package com.example.ebus.user.service;

public interface PasswordEncoderStrategy {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}