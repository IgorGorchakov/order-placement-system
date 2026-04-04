package com.example.ebus.user.dto;

import com.example.ebus.user.entity.PaymentMethodType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private Long id;
    private PaymentMethodType type;
    private String provider;
    private String token;
    private boolean defaultMethod;
}
