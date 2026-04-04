package com.example.ebus.user.dto;

import com.example.ebus.user.entity.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotNull
    private PaymentMethodType type;

    private String provider;
    private String token;
    private boolean defaultMethod;
}
