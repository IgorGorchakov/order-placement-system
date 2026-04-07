package com.example.ebus.user.service;

import com.example.ebus.user.dao.PaymentMethodDao;
import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.*;
import com.example.ebus.user.entity.PaymentMethodEntity;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.AuthenticationException;
import com.example.ebus.user.exception.EmailAlreadyExistsException;
import com.example.ebus.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final PaymentMethodDao paymentMethodDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userDao.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();

        return toResponse(userDao.save(user));
    }

    public UserResponse authenticate(LoginRequest request) {
        UserEntity user = userDao.findByEmail(request.email())
                .orElseThrow(AuthenticationException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException();
        }

        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userDao.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Long id) {
        return toResponse(userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional
    public PaymentMethodResponse addPaymentMethod(Long userId, PaymentMethodRequest request) {
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PaymentMethodEntity pm = PaymentMethodEntity.builder()
                .type(request.type())
                .provider(request.provider())
                .token(request.token())
                .defaultMethod(request.defaultMethod())
                .user(user)
                .build();

        return toPaymentResponse(paymentMethodDao.save(pm));
    }

    public List<PaymentMethodResponse> getPaymentMethods(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return paymentMethodDao.findByUserId(userId).stream()
                .map(this::toPaymentResponse).toList();
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

    private PaymentMethodResponse toPaymentResponse(PaymentMethodEntity pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .type(pm.getType())
                .provider(pm.getProvider())
                .token(pm.getToken())
                .defaultMethod(pm.isDefaultMethod())
                .build();
    }
}
