package com.example.ebus.user.service;

import com.example.ebus.user.dto.LoginRequest;
import com.example.ebus.user.dto.UserResponse;

public interface AuthenticationService {

    UserResponse authenticate(LoginRequest request);
}
