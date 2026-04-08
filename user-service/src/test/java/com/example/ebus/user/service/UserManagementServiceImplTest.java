package com.example.ebus.user.service;

import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.CreateUserRequest;
import com.example.ebus.user.dto.UserResponse;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.EmailAlreadyExistsException;
import com.example.ebus.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoderStrategy passwordEncoder;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser@example.com",
                "newpass123",
                "Jane",
                "Smith",
                null
        );

        UserEntity newUser = UserEntity.builder()
                .id(2L)
                .email("newuser@example.com")
                .passwordHash("encoded_password")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(passwordEncoder.encode("newpass123")).thenReturn("encoded_password");
        when(userDao.save(any(UserEntity.class))).thenReturn(newUser);

        UserResponse response = userManagementService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getFirstName()).isEqualTo("Jane");
        verify(userDao).save(any(UserEntity.class));
        verify(passwordEncoder).encode("newpass123");
    }

    @Test
    void createUser_EmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "existing@example.com",
                "password123",
                "John",
                "Doe",
                null
        );

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userDao.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation: email"));

        assertThatThrownBy(() -> userManagementService.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(userDao).save(any(UserEntity.class));
    }

    @Test
    void getUserById_Success() {
        when(userDao.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userManagementService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }
}
