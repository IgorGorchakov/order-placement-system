package com.example.ebus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String firstName;
    private String lastName;
    private String phone;
}
