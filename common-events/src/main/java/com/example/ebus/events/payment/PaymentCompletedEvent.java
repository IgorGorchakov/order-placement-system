package com.example.ebus.events.payment;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
    Long paymentId,
    Long bookingId,
    BigDecimal amount,
    String currency
) {}
