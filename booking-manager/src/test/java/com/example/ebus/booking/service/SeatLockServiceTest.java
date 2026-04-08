package com.example.ebus.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockServiceTest {

    @Mock
    private SeatLockStrategy delegate;

    @InjectMocks
    private SeatLockService seatLockService;

    @Test
    void lockSeats_shouldDelegateToStrategy() {
        // Given
        Long tripId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        // When
        seatLockService.lockSeats(tripId, seatNumbers);

        // Then
        verify(delegate).lockSeats(tripId, seatNumbers);
    }

    @Test
    void releaseSeats_shouldDelegateToStrategy() {
        // Given
        Long tripId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        // When
        seatLockService.releaseSeats(tripId, seatNumbers);

        // Then
        verify(delegate).releaseSeats(tripId, seatNumbers);
    }

    @Test
    void isSeatLocked_shouldDelegateToStrategy() {
        // Given
        Long tripId = 1L;
        String seatNumber = "A1";

        // When
        seatLockService.isSeatLocked(tripId, seatNumber);

        // Then
        verify(delegate).isSeatLocked(tripId, seatNumber);
    }

    @Test
    void initAvailability_shouldDelegateToStrategy() {
        // Given
        Long tripId = 1L;
        int totalSeats = 50;

        // When
        seatLockService.initAvailability(tripId, totalSeats);

        // Then
        verify(delegate).initAvailability(tripId, totalSeats);
    }
}
