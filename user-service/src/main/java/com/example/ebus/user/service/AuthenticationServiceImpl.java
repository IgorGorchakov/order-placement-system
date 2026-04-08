package com.example.ebus.user.service;

import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.LoginRequest;
import com.example.ebus.user.dto.UserResponse;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDao userDao;
    private final PasswordEncoderStrategy passwordEncoder;

    @Override
    public UserResponse authenticate(LoginRequest request) {
        UserEntity user = userDao.findByEmail(request.email())
                .orElseThrow(AuthenticationException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException();
        }

        return toResponse(user);
    }

    private UserResponse toResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
