package com.example.ebus.user.service;

import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.LoginRequest;
import com.example.ebus.user.dto.UserResponse;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserEntity sampleUser;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        sampleUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void authenticate_Success() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password123"
        );

        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
    }

    @Test
    void authenticate_UserNotFound() {
        LoginRequest request = new LoginRequest(
                "nonexistent@example.com",
                "password123"
        );

        when(userDao.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_InvalidPassword() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrongpassword"
        );

        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }
}
