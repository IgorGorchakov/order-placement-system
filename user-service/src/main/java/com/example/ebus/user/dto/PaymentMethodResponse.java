package com.example.ebus.user.dto;

import com.example.ebus.user.entity.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private Long id;
    private PaymentMethodType type;
    private String provider;
    private String token;
    private boolean defaultMethod;
}
