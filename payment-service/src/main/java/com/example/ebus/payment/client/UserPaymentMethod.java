package com.example.ebus.payment.client;

public record UserPaymentMethod(
    Long id,
    String type,
    String provider,
    String token,
    boolean defaultMethod
) {}
