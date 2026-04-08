package com.example.ebus.payment.kafka;

import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.BookingCreatedEvent;
import com.example.ebus.payment.service.PaymentProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final PaymentProcessor paymentProcessor;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(PaymentProcessor paymentProcessor, ObjectMapper objectMapper) {
        this.paymentProcessor = paymentProcessor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.BOOKING_CREATED, groupId = "payment-service")
    public void handleBookingCreated(String message) {
        try {
            BookingCreatedEvent event = objectMapper.readValue(message, BookingCreatedEvent.class);
            log.info("Received booking-created event for bookingId={}", event.bookingId());
            paymentProcessor.processBookingCreated(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize booking-created event (poison pill): {}", message, e);
            throw new RuntimeException("Deserialization failed for booking-created event", e);
        } catch (Exception e) {
            log.error("Failed to process booking-created event", e);
            throw e;
        }
    }
}
