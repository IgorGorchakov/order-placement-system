package com.example.ebus.payment.service;

import com.example.ebus.payment.dao.PaymentDao;
import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.entity.PaymentEntity;
import com.example.ebus.payment.entity.PaymentStatus;
import com.example.ebus.payment.exception.PaymentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQueryServiceImplTest {

    @Mock
    private PaymentDao paymentDao;

    @InjectMocks
    private PaymentQueryServiceImpl paymentQueryService;

    private PaymentEntity samplePayment;

    @BeforeEach
    void setUp() {
        samplePayment = new PaymentEntity();
        samplePayment.setId(1L);
        samplePayment.setBookingId(1L);
        samplePayment.setUserId(100L);
        samplePayment.setAmount(BigDecimal.valueOf(200));
        samplePayment.setCurrency("USD");
        samplePayment.setPaymentMethodType("CREDIT_CARD");
        samplePayment.setProvider("Stripe");
        samplePayment.setStatus(PaymentStatus.COMPLETED);
    }

    @Test
    void getPayment_Success() {
        when(paymentDao.findById(1L)).thenReturn(Optional.of(samplePayment));

        PaymentResponse response = paymentQueryService.getPayment(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void getPayment_NotFound() {
        when(paymentDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentQueryService.getPayment(1L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getPaymentByBookingId_Success() {
        when(paymentDao.findByBookingId(1L)).thenReturn(Optional.of(samplePayment));

        PaymentResponse response = paymentQueryService.getPaymentByBookingId(1L);

        assertThat(response).isNotNull();
        assertThat(response.bookingId()).isEqualTo(1L);
    }

    @Test
    void getPaymentByBookingId_NotFound() {
        when(paymentDao.findByBookingId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentQueryService.getPaymentByBookingId(1L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getPaymentsByUserId_Success() {
        when(paymentDao.findByUserId(100L)).thenReturn(List.of(samplePayment));

        List<PaymentResponse> responses = paymentQueryService.getPaymentsByUserId(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(100L);
    }

    @Test
    void getPaymentsByUserId_EmptyList() {
        when(paymentDao.findByUserId(100L)).thenReturn(List.of());

        List<PaymentResponse> responses = paymentQueryService.getPaymentsByUserId(100L);

        assertThat(responses).isEmpty();
    }
}
