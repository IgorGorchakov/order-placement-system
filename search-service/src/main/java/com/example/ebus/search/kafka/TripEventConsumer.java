package com.example.ebus.search.kafka;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripEventConsumer {

    private final SearchService searchService;

    @KafkaListener(topics = "trip-created", groupId = "search-service")
    public void onTripCreated(TripDocument tripDocument) {
        log.info("Received trip-created event for trip: {}", tripDocument.getId());
        searchService.indexTrip(tripDocument);
    }

    @KafkaListener(topics = "seat-availability-updated", groupId = "search-service")
    public void onSeatAvailabilityUpdated(Map<String, Object> event) {
        String tripId = (String) event.get("tripId");
        int availableSeats = (int) event.get("availableSeats");
        log.info("Received seat-availability-updated for trip: {}, seats: {}", tripId, availableSeats);
        searchService.updateAvailableSeats(tripId, availableSeats);
    }

    @KafkaListener(topics = "booking-cancelled", groupId = "search-service")
    public void onBookingCancelled(Map<String, Object> event) {
        String tripId = (String) event.get("tripId");
        int seatsToRelease = (int) event.getOrDefault("seatsToRelease", 1);
        log.info("Received booking-cancelled for trip: {}, releasing {} seats", tripId, seatsToRelease);
        searchService.incrementAvailableSeats(tripId, seatsToRelease);
    }
}
