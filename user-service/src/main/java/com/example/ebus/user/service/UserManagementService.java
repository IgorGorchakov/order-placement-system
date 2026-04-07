package com.example.ebus.user.service;

import com.example.ebus.user.dto.CreateUserRequest;
import com.example.ebus.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserManagementService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers(int limit);

    UserResponse getUserById(Long id);
}
