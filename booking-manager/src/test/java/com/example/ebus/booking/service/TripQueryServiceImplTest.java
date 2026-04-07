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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        Page<TripEntity> page = new PageImpl<>(List.of(sampleTrip));
        when(tripDao.findTrips(eq("NYC"), eq("Boston"), eq(date.atStartOfDay()), eq(date.plusDays(1).atStartOfDay()), any(PageRequest.class)))
                .thenReturn(page);

        Page<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", date, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
    }

    @Test
    void findTrips_WithNullDate() {
        Page<TripEntity> page = new PageImpl<>(List.of(sampleTrip));
        when(tripDao.findTrips(eq("NYC"), eq("Boston"), isNull(), isNull(), any(PageRequest.class)))
                .thenReturn(page);

        Page<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", null, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        verify(tripDao).findTrips(eq("NYC"), eq("Boston"), isNull(), isNull(), any(PageRequest.class));
    }

    @Test
    void findTrips_EmptyResult() {
        Page<TripEntity> page = new PageImpl<>(List.of());
        when(tripDao.findTrips(any(), any(), any(), any(), any(PageRequest.class))).thenReturn(page);

        Page<TripResponse> responses = tripQueryService.findTrips("NYC", "Boston", LocalDate.now(), PageRequest.of(0, 20));

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
