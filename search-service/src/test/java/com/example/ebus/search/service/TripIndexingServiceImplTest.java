package com.example.ebus.search.service;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.repository.TripSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripIndexingServiceImplTest {

    @Mock
    private TripSearchRepository tripSearchRepository;

    @InjectMocks
    private TripIndexingServiceImpl tripIndexingService;

    private TripDocument sampleTrip;

    @BeforeEach
    void setUp() {
        sampleTrip = TripDocument.builder()
                .id("trip-1")
                .origin("NYC")
                .destination("Boston")
                .departureTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 4, 10, 12, 0))
                .price(BigDecimal.valueOf(50))
                .currency("USD")
                .operatorName("Test Bus Co")
                .amenities(List.of("WiFi", "AC"))
                .availableSeats(30)
                .build();
    }

    @Test
    void indexTrip_Success() {
        tripIndexingService.indexTrip(sampleTrip);

        verify(tripSearchRepository).save(sampleTrip);
    }

    @Test
    void updateAvailableSeats_TripExists() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));
        when(tripSearchRepository.save(any(TripDocument.class))).thenReturn(sampleTrip);

        tripIndexingService.updateAvailableSeats("trip-1", 25);

        verify(tripSearchRepository).save(argThat(doc -> doc.getAvailableSeats() == 25));
    }

    @Test
    void updateAvailableSeats_TripNotExists() {
        when(tripSearchRepository.findById("trip-99")).thenReturn(Optional.empty());

        tripIndexingService.updateAvailableSeats("trip-99", 25);

        verify(tripSearchRepository, never()).save(any());
    }

    @Test
    void incrementAvailableSeats_TripExists() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));
        when(tripSearchRepository.save(any(TripDocument.class))).thenReturn(sampleTrip);

        tripIndexingService.incrementAvailableSeats("trip-1", 5);

        verify(tripSearchRepository).save(argThat(doc -> doc.getAvailableSeats() == 35));
    }

    @Test
    void incrementAvailableSeats_TripNotExists() {
        when(tripSearchRepository.findById("trip-99")).thenReturn(Optional.empty());

        tripIndexingService.incrementAvailableSeats("trip-99", 5);

        verify(tripSearchRepository, never()).save(any());
    }
}
