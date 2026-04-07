package com.example.ebus.booking.service;

import com.example.ebus.booking.exception.SeatNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private SeatLockService seatLockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        seatLockService = new SeatLockService(redisTemplate);
    }

    @Test
    void lockSeats_Success() {
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(valueOps.decrement(anyString(), anyLong())).thenReturn(1L);

        seatLockService.lockSeats(1L, Arrays.asList("1A", "1B"));

        verify(valueOps, times(2)).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(valueOps).decrement("trip-availability:1", 2);
    }

    @Test
    void lockSeats_SeatAlreadyLocked() {
        when(valueOps.setIfAbsent(eq("seat-lock:1:1A"), anyString(), any(Duration.class))).thenReturn(true);
        when(valueOps.setIfAbsent(eq("seat-lock:1:1B"), anyString(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> seatLockService.lockSeats(1L, Arrays.asList("1A", "1B")))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("1B");

        verify(redisTemplate).delete("seat-lock:1:1A");
        verify(valueOps, never()).decrement(anyString(), anyLong());
    }

    @Test
    void releaseSeats_Success() {
        when(valueOps.increment(anyString(), anyLong())).thenReturn(1L);

        seatLockService.releaseSeats(1L, Arrays.asList("1A", "1B"));

        verify(redisTemplate).delete("seat-lock:1:1A");
        verify(redisTemplate).delete("seat-lock:1:1B");
        verify(valueOps).increment("trip-availability:1", 2);
    }

    @Test
    void isSeatLocked_SeatIsLocked() {
        when(redisTemplate.hasKey("seat-lock:1:1A")).thenReturn(true);

        boolean result = seatLockService.isSeatLocked(1L, "1A");

        assertThat(result).isTrue();
    }

    @Test
    void isSeatLocked_SeatNotLocked() {
        when(redisTemplate.hasKey("seat-lock:1:1A")).thenReturn(false);

        boolean result = seatLockService.isSeatLocked(1L, "1A");

        assertThat(result).isFalse();
    }

    @Test
    void initAvailability_Success() {
        seatLockService.initAvailability(1L, 40);

        verify(valueOps).set("trip-availability:1", "40");
    }
}
