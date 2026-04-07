package com.example.ebus.user.service;

import com.example.ebus.user.dto.CreateUserRequest;
import com.example.ebus.user.dto.UserResponse;

import java.util.List;

public interface UserManagementService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);
}
