package com.example.ebus.payment.exception;

import com.example.ebus.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handlePaymentNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("PAYMENT_NOT_FOUND");
            assertThat(body.message()).isEqualTo("Payment not found: 5");
        });
    }

    @Test
    void globalExceptionHandler_HandlesGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new RuntimeException("Something went wrong");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("INTERNAL_ERROR");
        });
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/payments");
        return request;
    }
}
