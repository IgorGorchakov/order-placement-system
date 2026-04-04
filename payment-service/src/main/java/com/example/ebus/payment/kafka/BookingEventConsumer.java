package com.example.ebus.payment.kafka;

import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.*;
import com.example.ebus.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.BOOKING_CREATED, groupId = "payment-service")
    public void handleBookingCreated(String message) {
        try {
            BookingCreatedEvent event = objectMapper.readValue(message, BookingCreatedEvent.class);
            log.info("Received booking-created event for bookingId={}", event.bookingId());
            paymentService.processBookingCreated(event);
        } catch (Exception e) {
            log.error("Failed to process booking-created event", e);
        }
    }
}
