package com.example.ebus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Email(message = "Email must be valid") @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters") String password,
    @Size(max = 100) String firstName,
    @Size(max = 100) String lastName,
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Phone must be a valid number") String phone
) {}
