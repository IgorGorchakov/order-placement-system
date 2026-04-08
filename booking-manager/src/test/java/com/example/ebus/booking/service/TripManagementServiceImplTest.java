package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.dto.CreateTripRequest;
import com.example.ebus.booking.dto.TripResponse;
import com.example.ebus.booking.entity.TripEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripManagementServiceImplTest {

    @Mock
    private TripDao tripDao;

    @Mock
    private SeatLockService seatLockService;

    @InjectMocks
    private TripManagementServiceImpl tripManagementService;

    @Test
    void createTrip_Success() {
        CreateTripRequest request = new CreateTripRequest(
                10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        when(tripDao.save(any(TripEntity.class))).thenAnswer(invocation -> {
            TripEntity trip = invocation.getArgument(0);
            trip.setId(1L);
            return trip;
        });

        TripResponse response = tripManagementService.createTrip(request);

        assertThat(response).isNotNull();
        assertThat(response.operatorName()).isEqualTo("Test Bus Co");
        verify(seatLockService).initAvailability(1L, 40);
    }
}
