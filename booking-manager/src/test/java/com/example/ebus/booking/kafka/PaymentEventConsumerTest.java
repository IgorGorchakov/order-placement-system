package com.example.ebus.booking.kafka;

import com.example.ebus.booking.dao.BookingDao;
import com.example.ebus.booking.entity.BookingEntity;
import com.example.ebus.booking.entity.BookingStatus;
import com.example.ebus.booking.service.OutboxService;
import com.example.ebus.booking.service.SeatLockService;
import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.BookingCancelledEvent;
import com.example.ebus.events.booking.BookingConfirmedEvent;
import com.example.ebus.events.payment.PaymentCompletedEvent;
import com.example.ebus.events.payment.PaymentFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private BookingDao bookingDao;

    @Mock
    private OutboxService outboxService;

    @Mock
    private SeatLockService seatLockService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private BookingEntity sampleBooking;
    private PaymentCompletedEvent completedEvent;
    private PaymentFailedEvent failedEvent;

    @BeforeEach
    void setUp() {
        sampleBooking = new BookingEntity();
        sampleBooking.setId(1L);
        sampleBooking.setUserId(100L);
        sampleBooking.setTripId(1L);
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setSeatNumbers("1A,1B");
        sampleBooking.setTotalPrice(BigDecimal.valueOf(200));

        completedEvent = new PaymentCompletedEvent(1L, 1L, BigDecimal.valueOf(200), "USD");
        failedEvent = new PaymentFailedEvent(1L, 1L, "Payment declined");
    }

    @Test
    void handlePaymentCompleted_Success() throws Exception {
        String message = "{\"paymentId\":1,\"bookingId\":1}";
        when(objectMapper.readValue(message, PaymentCompletedEvent.class)).thenReturn(completedEvent);
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        when(objectMapper.writeValueAsString(any(BookingConfirmedEvent.class))).thenReturn("{}");

        paymentEventConsumer.handlePaymentCompleted(message);

        verify(bookingDao).save(argThat(booking -> booking.getStatus() == BookingStatus.CONFIRMED));
        verify(outboxService).saveEvent(eq("Booking"), eq("1"), eq(Topics.BOOKING_CONFIRMED), anyString());
    }

    @Test
    void handlePaymentCompleted_BookingNotFound() throws Exception {
        String message = "{\"paymentId\":1,\"bookingId\":1}";
        when(objectMapper.readValue(message, PaymentCompletedEvent.class)).thenReturn(completedEvent);
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        paymentEventConsumer.handlePaymentCompleted(message);

        verify(bookingDao, never()).save(any());
        verify(outboxService, never()).saveEvent(any(), any(), any(), any());
    }

    @Test
    void handlePaymentCompleted_BookingAlreadyConfirmed() throws Exception {
        sampleBooking.setStatus(BookingStatus.CONFIRMED);
        String message = "{\"paymentId\":1,\"bookingId\":1}";
        when(objectMapper.readValue(message, PaymentCompletedEvent.class)).thenReturn(completedEvent);
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));

        paymentEventConsumer.handlePaymentCompleted(message);

        verify(bookingDao, never()).save(any());
    }

    @Test
    void handlePaymentFailed_Success() throws Exception {
        String message = "{\"paymentId\":1,\"bookingId\":1,\"reason\":\"declined\"}";
        when(objectMapper.readValue(message, PaymentFailedEvent.class)).thenReturn(failedEvent);
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        when(objectMapper.writeValueAsString(any(BookingCancelledEvent.class))).thenReturn("{}");

        paymentEventConsumer.handlePaymentFailed(message);

        verify(bookingDao).save(argThat(booking -> booking.getStatus() == BookingStatus.CANCELLED));
        verify(seatLockService).releaseSeats(1L, Arrays.asList("1A", "1B"));
        verify(outboxService).saveEvent(eq("Booking"), eq("1"), eq(Topics.BOOKING_CANCELLED), anyString());
    }

    @Test
    void handlePaymentFailed_BookingNotFound() throws Exception {
        String message = "{\"paymentId\":1,\"bookingId\":1,\"reason\":\"declined\"}";
        when(objectMapper.readValue(message, PaymentFailedEvent.class)).thenReturn(failedEvent);
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        paymentEventConsumer.handlePaymentFailed(message);

        verify(bookingDao, never()).save(any());
        verify(seatLockService, never()).releaseSeats(any(), any());
    }
}
