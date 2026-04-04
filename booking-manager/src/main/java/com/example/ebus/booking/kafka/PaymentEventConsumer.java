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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class PaymentEventConsumer {

    private final BookingDao bookingDao;
    private final OutboxService outboxService;
    private final SeatLockService seatLockService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(BookingDao bookingDao, OutboxService outboxService,
                                SeatLockService seatLockService, ObjectMapper objectMapper) {
        this.bookingDao = bookingDao;
        this.outboxService = outboxService;
        this.seatLockService = seatLockService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "booking-manager")
    @Transactional
    public void handlePaymentCompleted(String message) {
        try {
            PaymentCompletedEvent paymentEvent = objectMapper.readValue(message, PaymentCompletedEvent.class);
            BookingEntity booking = bookingDao.findById(paymentEvent.bookingId()).orElse(null);
            if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingDao.save(booking);

                BookingConfirmedEvent event = new BookingConfirmedEvent(
                        booking.getId(), booking.getUserId(), booking.getTripId());
                outboxService.saveEvent("Booking", booking.getId().toString(),
                        Topics.BOOKING_CONFIRMED, objectMapper.writeValueAsString(event));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment-completed event", e);
        }
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "booking-manager")
    @Transactional
    public void handlePaymentFailed(String message) {
        try {
            PaymentFailedEvent paymentEvent = objectMapper.readValue(message, PaymentFailedEvent.class);
            BookingEntity booking = bookingDao.findById(paymentEvent.bookingId()).orElse(null);
            if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingDao.save(booking);
                seatLockService.releaseSeats(booking.getTripId(),
                        Arrays.asList(booking.getSeatNumbers().split(",")));

                BookingCancelledEvent event = new BookingCancelledEvent(
                        booking.getId(), booking.getUserId(), booking.getTripId());
                outboxService.saveEvent("Booking", booking.getId().toString(),
                        Topics.BOOKING_CANCELLED, objectMapper.writeValueAsString(event));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process payment-failed event", e);
        }
    }
}
