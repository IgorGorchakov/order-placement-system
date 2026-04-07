package com.example.ebus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank String password,
    String firstName,
    String lastName,
    String phone
) {}
