package com.example.ebus.payment.service;

import com.example.ebus.events.booking.BookingCreatedEvent;
import com.example.ebus.events.payment.PaymentCompletedEvent;
import com.example.ebus.events.payment.PaymentFailedEvent;
import com.example.ebus.payment.client.UserPaymentMethod;
import com.example.ebus.payment.client.UserServiceClient;
import com.example.ebus.payment.dao.PaymentDao;
import com.example.ebus.payment.entity.PaymentEntity;
import com.example.ebus.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorImplTest {

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentProcessorImpl paymentProcessor;

    private BookingCreatedEvent sampleEvent;
    private UserPaymentMethod samplePaymentMethod;
    private PaymentEntity samplePayment;

    @BeforeEach
    void setUp() {
        sampleEvent = new BookingCreatedEvent(
                1L, 100L, 1L, List.of("1A", "1B"), BigDecimal.valueOf(200), "USD");

        samplePaymentMethod = new UserPaymentMethod(
                1L, "CREDIT_CARD", "Stripe", "tok_123", true);

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
    void processBookingCreated_Success() {
        when(userServiceClient.getPaymentMethods(100L)).thenReturn(List.of(samplePaymentMethod));
        when(paymentDao.save(any(PaymentEntity.class))).thenReturn(samplePayment);
        doNothing().when(eventPublisher).publishPaymentCompletedEvent(anyLong(), any(PaymentCompletedEvent.class));

        paymentProcessor.processBookingCreated(sampleEvent);

        verify(paymentDao).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.COMPLETED &&
                payment.getBookingId().equals(1L)
        ));
        verify(eventPublisher).publishPaymentCompletedEvent(eq(1L), any(PaymentCompletedEvent.class));
    }

    @Test
    void processBookingCreated_NoPaymentMethod() {
        when(userServiceClient.getPaymentMethods(100L)).thenReturn(List.of());
        when(paymentDao.save(any(PaymentEntity.class))).thenReturn(samplePayment);
        doNothing().when(eventPublisher).publishPaymentFailedEvent(anyLong(), any(PaymentFailedEvent.class));

        paymentProcessor.processBookingCreated(sampleEvent);

        verify(paymentDao).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.FAILED &&
                payment.getFailureReason().equals("No payment method found for user")
        ));
        verify(eventPublisher).publishPaymentFailedEvent(anyLong(), any(PaymentFailedEvent.class));
    }

    @Test
    void processBookingCreated_ExceptionHandling() {
        when(userServiceClient.getPaymentMethods(100L)).thenThrow(new RuntimeException("Service unavailable"));
        when(paymentDao.save(any(PaymentEntity.class))).thenReturn(samplePayment);
        doNothing().when(eventPublisher).publishPaymentFailedEvent(anyLong(), any(PaymentFailedEvent.class));

        paymentProcessor.processBookingCreated(sampleEvent);

        verify(paymentDao).save(argThat(payment ->
                payment.getStatus() == PaymentStatus.FAILED &&
                payment.getPaymentMethodType().equals("UNKNOWN")
        ));
    }
}
