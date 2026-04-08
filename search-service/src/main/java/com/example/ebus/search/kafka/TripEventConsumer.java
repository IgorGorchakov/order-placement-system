package com.example.ebus.search.kafka;

import com.example.ebus.events.Topics;
import com.example.ebus.events.search.BookingCancelledSeatEvent;
import com.example.ebus.events.search.SeatAvailabilityUpdatedEvent;
import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.service.TripIndexingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripEventConsumer {

    private final TripIndexingService tripIndexingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.TRIP_CREATED, groupId = "search-service")
    public void onTripCreated(TripDocument tripDocument) {
        log.info("Received trip-created event for trip: {}", tripDocument.getId());
        tripIndexingService.indexTrip(tripDocument);
    }

    @KafkaListener(topics = Topics.SEAT_AVAILABILITY_UPDATED, groupId = "search-service")
    public void onSeatAvailabilityUpdated(String message) {
        try {
            SeatAvailabilityUpdatedEvent event = objectMapper.readValue(message, SeatAvailabilityUpdatedEvent.class);
            log.info("Received seat-availability-updated for trip: {}, seats: {}", event.tripId(), event.availableSeats());
            tripIndexingService.updateAvailableSeats(event.tripId(), event.availableSeats());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize seat-availability-updated event: {}", message, e);
            throw new RuntimeException("Failed to process seat-availability-updated event", e);
        }
    }

    @KafkaListener(topics = Topics.BOOKING_CANCELLED, groupId = "search-service")
    public void onBookingCancelled(String message) {
        try {
            BookingCancelledSeatEvent event = objectMapper.readValue(message, BookingCancelledSeatEvent.class);
            log.info("Received booking-cancelled for trip: {}, releasing {} seats", event.tripId(), event.seatsToRelease());
            tripIndexingService.incrementAvailableSeats(event.tripId(), event.seatsToRelease());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize booking-cancelled event: {}", message, e);
            throw new RuntimeException("Failed to process booking-cancelled event", e);
        }
    }
}
