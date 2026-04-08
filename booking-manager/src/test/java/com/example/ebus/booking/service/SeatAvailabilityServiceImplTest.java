package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.document.SeatLayoutDocument;
import com.example.ebus.booking.dto.SeatAvailabilityResponse;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.booking.repository.SeatLayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatAvailabilityServiceImplTest {

    @Mock
    private TripDao tripDao;

    @Mock
    private SeatLayoutRepository seatLayoutRepository;

    @Mock
    private SeatLockService seatLockService;

    @InjectMocks
    private SeatAvailabilityServiceImpl seatAvailabilityService;

    private TripEntity sampleTrip;
    private SeatLayoutDocument sampleLayout;

    @BeforeEach
    void setUp() {
        sampleTrip = new TripEntity();
        sampleTrip.setId(1L);
        sampleTrip.setRouteId(10L);
        sampleTrip.setBusId(20L);
        sampleTrip.setDepartureTime(LocalDateTime.of(2026, 4, 10, 8, 0));
        sampleTrip.setArrivalTime(LocalDateTime.of(2026, 4, 10, 12, 0));
        sampleTrip.setPrice(BigDecimal.valueOf(50));
        sampleTrip.setCurrency("USD");
        sampleTrip.setTotalSeats(40);
        sampleTrip.setOperatorName("Test Bus Co");

        sampleLayout = new SeatLayoutDocument();
        sampleLayout.setId("layout-1");
        sampleLayout.setBusId(20L);
        sampleLayout.setSeatMap(Map.of("1A", "window", "1B", "aisle"));
        sampleLayout.setRows(10);
        sampleLayout.setSeatsPerRow(4);
    }

    @Test
    void getSeatAvailability_WithLayout() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(seatLayoutRepository.findByBusId(20L)).thenReturn(Optional.of(sampleLayout));
        when(seatLockService.isSeatLocked(1L, "1A")).thenReturn(false);
        when(seatLockService.isSeatLocked(1L, "1B")).thenReturn(true);

        SeatAvailabilityResponse response = seatAvailabilityService.getSeatAvailability(1L);

        assertThat(response).isNotNull();
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.rows()).isEqualTo(10);
        assertThat(response.seatsPerRow()).isEqualTo(4);
        assertThat(response.availability()).containsEntry("1A", true);
        assertThat(response.availability()).containsEntry("1B", false);
    }

    @Test
    void getSeatAvailability_WithoutLayout() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(seatLayoutRepository.findByBusId(20L)).thenReturn(Optional.empty());

        SeatAvailabilityResponse response = seatAvailabilityService.getSeatAvailability(1L);

        assertThat(response).isNotNull();
        assertThat(response.rows()).isEqualTo(0);
        assertThat(response.seatMap()).isEmpty();
    }

    @Test
    void getSeatAvailability_TripNotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatAvailabilityService.getSeatAvailability(1L))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");
    }
}
