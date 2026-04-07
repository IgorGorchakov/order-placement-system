package com.example.ebus.user.service;

import com.example.ebus.user.dto.CreateUserRequest;
import com.example.ebus.user.dto.UserResponse;

public interface UserManagementService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);
}
