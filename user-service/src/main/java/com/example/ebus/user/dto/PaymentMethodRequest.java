package com.example.ebus.user.dto;

import com.example.ebus.user.entity.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

public record PaymentMethodRequest(
    @NotNull PaymentMethodType type,
    String provider,
    String token,
    boolean defaultMethod
) {}
