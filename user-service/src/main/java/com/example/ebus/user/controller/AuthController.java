package com.example.ebus.user.controller;

import com.example.ebus.user.dto.LoginRequest;
import com.example.ebus.user.dto.UserResponse;
import com.example.ebus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }
}
