package com.example.ebus.search.kafka;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.service.TripIndexingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripEventConsumerTest {

    @Mock
    private TripIndexingService tripIndexingService;

    @InjectMocks
    private TripEventConsumer tripEventConsumer;

    private TripDocument sampleTrip;

    @BeforeEach
    void setUp() {
        sampleTrip = TripDocument.builder()
                .id("trip-1")
                .origin("NYC")
                .destination("Boston")
                .departureTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .price(BigDecimal.valueOf(50))
                .currency("USD")
                .operatorName("Test Bus Co")
                .amenities(List.of("WiFi"))
                .availableSeats(30)
                .build();
    }

    @Test
    void onTripCreated_Success() {
        tripEventConsumer.onTripCreated(sampleTrip);

        verify(tripIndexingService).indexTrip(sampleTrip);
    }

    @Test
    void onSeatAvailabilityUpdated_Success() {
        Map<String, Object> event = Map.of(
                "tripId", "trip-1",
                "availableSeats", 25
        );

        tripEventConsumer.onSeatAvailabilityUpdated(event);

        verify(tripIndexingService).updateAvailableSeats("trip-1", 25);
    }

    @Test
    void onBookingCancelled_Success() {
        Map<String, Object> event = Map.of(
                "tripId", "trip-1",
                "seatsToRelease", 3
        );

        tripEventConsumer.onBookingCancelled(event);

        verify(tripIndexingService).incrementAvailableSeats("trip-1", 3);
    }

    @Test
    void onBookingCancelled_DefaultSeatsToRelease() {
        Map<String, Object> event = Map.of("tripId", "trip-1");

        tripEventConsumer.onBookingCancelled(event);

        verify(tripIndexingService).incrementAvailableSeats("trip-1", 1);
    }
}
