package com.example.ebus.fulfillment.kafka;

import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.BookingCancelledEvent;
import com.example.ebus.events.booking.BookingConfirmedEvent;
import com.example.ebus.fulfillment.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;

    public BookingEventConsumer(TicketService ticketService, ObjectMapper objectMapper) {
        this.ticketService = ticketService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = Topics.BOOKING_CONFIRMED, groupId = "fulfillment-service")
    public void handleBookingConfirmed(String message) {
        try {
            BookingConfirmedEvent event = objectMapper.readValue(message, BookingConfirmedEvent.class);
            log.info("Received booking-confirmed for bookingId={}", event.bookingId());
            ticketService.issueTicket(event.bookingId(), event.userId(), event.tripId());
        } catch (Exception e) {
            log.error("Failed to process booking-confirmed event", e);
        }
    }

    @KafkaListener(topics = Topics.BOOKING_CANCELLED, groupId = "fulfillment-service")
    public void handleBookingCancelled(String message) {
        try {
            BookingCancelledEvent event = objectMapper.readValue(message, BookingCancelledEvent.class);
            log.info("Received booking-cancelled for bookingId={}", event.bookingId());
            ticketService.cancelTicket(event.bookingId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process booking-cancelled event", e);
        }
    }
}
