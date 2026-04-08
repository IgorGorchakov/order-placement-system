package com.example.ebus.search.service;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.repository.TripSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                .version(1L)
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

        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        verify(tripSearchRepository).save(captor.capture());
        
        TripDocument savedDoc = captor.getValue();
        assertThat(savedDoc.getAvailableSeats()).isEqualTo(25);
        assertThat(savedDoc.getId()).isEqualTo("trip-1");
    }

    @Test
    void updateAvailableSeats_TripNotExists() {
        when(tripSearchRepository.findById("trip-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripIndexingService.updateAvailableSeats("trip-99", 25))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip document not found");
    }

    @Test
    void updateAvailableSeats_ConcurrentModification() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));
        when(tripSearchRepository.save(any(TripDocument.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"));

        assertThatThrownBy(() -> tripIndexingService.updateAvailableSeats("trip-1", 25))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Concurrent modification detected");
    }

    @Test
    void incrementAvailableSeats_TripExists() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));
        when(tripSearchRepository.save(any(TripDocument.class))).thenReturn(sampleTrip);

        tripIndexingService.incrementAvailableSeats("trip-1", 5);

        ArgumentCaptor<TripDocument> captor = ArgumentCaptor.forClass(TripDocument.class);
        verify(tripSearchRepository).save(captor.capture());
        
        TripDocument savedDoc = captor.getValue();
        assertThat(savedDoc.getAvailableSeats()).isEqualTo(35);
        assertThat(savedDoc.getId()).isEqualTo("trip-1");
    }

    @Test
    void incrementAvailableSeats_TripNotExists() {
        when(tripSearchRepository.findById("trip-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripIndexingService.incrementAvailableSeats("trip-99", 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trip document not found");
    }

    @Test
    void incrementAvailableSeats_ConcurrentModification() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));
        when(tripSearchRepository.save(any(TripDocument.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"));

        assertThatThrownBy(() -> tripIndexingService.incrementAvailableSeats("trip-1", 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Concurrent modification detected");
    }
}
