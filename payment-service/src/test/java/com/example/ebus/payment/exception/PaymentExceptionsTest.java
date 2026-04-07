package com.example.ebus.payment.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentExceptionsTest {

    @Test
    void paymentNotFoundException_HasCorrectMessage() {
        PaymentNotFoundException ex = new PaymentNotFoundException(42L);

        assertThat(ex).hasMessage("Payment not found: 42");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void paymentNotFoundException_WithBookingId() {
        PaymentNotFoundException ex = new PaymentNotFoundException(99L);

        assertThat(ex).hasMessage("Payment not found: 99");
    }

    @Test
    void globalExceptionHandler_HandlesPaymentNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        PaymentNotFoundException ex = new PaymentNotFoundException(5L);

        ResponseEntity<Map<String, String>> response = handler.handlePaymentNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Payment not found: 5");
    }
}
