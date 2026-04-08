package com.example.ebus.search.kafka;

import com.example.ebus.events.search.BookingCancelledSeatEvent;
import com.example.ebus.events.search.SeatAvailabilityUpdatedEvent;
import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.service.TripIndexingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripEventConsumerTest {

    @Mock
    private TripIndexingService tripIndexingService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private TripEventConsumer tripEventConsumer;

    private TripDocument sampleTrip;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        tripEventConsumer = new TripEventConsumer(tripIndexingService, objectMapper);

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
    void onSeatAvailabilityUpdated_Success() throws JsonProcessingException {
        SeatAvailabilityUpdatedEvent event = new SeatAvailabilityUpdatedEvent("trip-1", 25);
        String message = objectMapper.writeValueAsString(event);

        tripEventConsumer.onSeatAvailabilityUpdated(message);

        verify(tripIndexingService).updateAvailableSeats("trip-1", 25);
    }

    @Test
    void onSeatAvailabilityUpdated_InvalidMessage_ThrowsException() {
        String invalidMessage = "not-json";

        assertThatThrownBy(() -> tripEventConsumer.onSeatAvailabilityUpdated(invalidMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process seat-availability-updated event");
    }

    @Test
    void onBookingCancelled_Success() throws JsonProcessingException {
        BookingCancelledSeatEvent event = new BookingCancelledSeatEvent("trip-1", 3);
        String message = objectMapper.writeValueAsString(event);

        tripEventConsumer.onBookingCancelled(message);

        verify(tripIndexingService).incrementAvailableSeats("trip-1", 3);
    }

    @Test
    void onBookingCancelled_InvalidMessage_ThrowsException() {
        String invalidMessage = "not-json";

        assertThatThrownBy(() -> tripEventConsumer.onBookingCancelled(invalidMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process booking-cancelled event");
    }
}
