package com.example.ebus.payment.controller;

import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.exception.PaymentNotFoundException;
import com.example.ebus.payment.service.PaymentQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentQueryService paymentQueryService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void getPayment_Success() {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, BigDecimal.valueOf(200), "USD",
                "CREDIT_CARD", "Stripe", "COMPLETED", null, LocalDateTime.now());

        when(paymentQueryService.getPayment(1L)).thenReturn(response);

        PaymentResponse result = paymentController.getPayment(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.status()).isEqualTo("COMPLETED");
    }

    @Test
    void getPayment_NotFound() {
        when(paymentQueryService.getPayment(1L)).thenThrow(new PaymentNotFoundException(1L));

        assertThatThrownBy(() -> paymentController.getPayment(1L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentByBookingId_Success() {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, BigDecimal.valueOf(200), "USD",
                "CREDIT_CARD", "Stripe", "COMPLETED", null, LocalDateTime.now());

        when(paymentQueryService.getPaymentByBookingId(1L)).thenReturn(response);

        PaymentResponse result = paymentController.getPaymentByBookingId(1L);

        assertThat(result).isNotNull();
        assertThat(result.bookingId()).isEqualTo(1);
    }

    @Test
    void getPaymentByBookingId_NotFound() {
        when(paymentQueryService.getPaymentByBookingId(1L)).thenThrow(new PaymentNotFoundException(1L));

        assertThatThrownBy(() -> paymentController.getPaymentByBookingId(1L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void getPaymentsByUserId_Success() {
        PaymentResponse response = new PaymentResponse(
                1L, 1L, 100L, BigDecimal.valueOf(200), "USD",
                "CREDIT_CARD", "Stripe", "COMPLETED", null, LocalDateTime.now());

        when(paymentQueryService.getPaymentsByUserId(100L)).thenReturn(List.of(response));

        List<PaymentResponse> responses = paymentController.getPaymentsByUserId(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(100);
    }

    @Test
    void getPaymentsByUserId_EmptyList() {
        when(paymentQueryService.getPaymentsByUserId(100L)).thenReturn(List.of());

        List<PaymentResponse> responses = paymentController.getPaymentsByUserId(100L);

        assertThat(responses).isEmpty();
    }
}
