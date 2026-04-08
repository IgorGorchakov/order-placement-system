package com.example.ebus.user.controller;

import com.example.ebus.user.dto.CreateUserRequest;
import com.example.ebus.user.dto.PaymentMethodRequest;
import com.example.ebus.user.dto.PaymentMethodResponse;
import com.example.ebus.user.dto.UserResponse;
import com.example.ebus.user.service.PaymentMethodService;
import com.example.ebus.user.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
