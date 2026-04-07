package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.dto.TripResponse;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripQueryServiceImplTest {

    @Mock
    private TripDao tripDao;

    @InjectMocks
    private TripQueryServiceImpl tripQueryService;

    private TripEntity sampleTrip;

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
    }

    @Test
    void findTrips_WithAllParams() {
        LocalDate date = LocalDate.of(2026, 4, 10);
        when(tripDao.findTrips("NYC", "Boston", date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(sampleTrip));

        List<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", date);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1L);
    }

    @Test
    void findTrips_WithNullDate() {
        when(tripDao.findTrips("NYC", "Boston", null, null))
                .thenReturn(List.of(sampleTrip));

        List<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", null);

        assertThat(responses).hasSize(1);
        verify(tripDao).findTrips("NYC", "Boston", null, null);
    }

    @Test
    void findTrips_EmptyResult() {
        when(tripDao.findTrips(any(), any(), any(), any())).thenReturn(List.of());

        List<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", LocalDate.now());

        assertThat(responses).isEmpty();
    }

    @Test
    void getTrip_Success() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));

        TripResponse response = tripQueryService.getTrip(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.operatorName()).isEqualTo("Test Bus Co");
    }

    @Test
    void getTrip_NotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripQueryService.getTrip(1L))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");
    }
}
