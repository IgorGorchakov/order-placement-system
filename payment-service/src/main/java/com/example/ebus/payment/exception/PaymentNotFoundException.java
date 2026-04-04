package com.example.ebus.payment.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long id) {
        super("Payment not found: " + id);
    }
}
