package com.example.ebus.user.controller;

import com.example.ebus.user.dto.*;
import com.example.ebus.user.service.PaymentMethodService;
import com.example.ebus.user.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;
    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userManagementService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userManagementService.getUserById(id);
    }

    @PostMapping("/{id}/payment-methods")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodResponse addPaymentMethod(@PathVariable Long id,
                                                   @Valid @RequestBody PaymentMethodRequest request) {
        return paymentMethodService.addPaymentMethod(id, request);
    }

    @GetMapping("/{id}/payment-methods")
    public List<PaymentMethodResponse> getPaymentMethods(@PathVariable Long id) {
        return paymentMethodService.getPaymentMethods(id);
    }
}
