package com.example.ebus.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long bookingId,
    Long userId,
    BigDecimal amount,
    String currency,
    String paymentMethodType,
    String provider,
    String status,
    String failureReason,
    LocalDateTime createdAt
) {}
