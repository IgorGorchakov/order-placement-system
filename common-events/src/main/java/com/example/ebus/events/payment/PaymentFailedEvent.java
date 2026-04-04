package com.example.ebus.events.payment;

public record PaymentFailedEvent(
    Long paymentId,
    Long bookingId,
    String reason
) {}
