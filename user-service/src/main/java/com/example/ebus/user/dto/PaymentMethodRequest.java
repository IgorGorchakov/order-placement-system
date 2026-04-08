package com.example.ebus.user.dto;

import com.example.ebus.user.entity.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(
    @NotNull PaymentMethodType type,
    @NotBlank @Size(max = 100) String provider,
    @NotBlank @Size(max = 255) String token,
    boolean defaultMethod
) {}
