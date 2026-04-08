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
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        seatLockService.init();
    }

    @Test
    void lockSeats_Success() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(2L);

        seatLockService.lockSeats(1L, Arrays.asList("1A", "1B"));

        verify(redisTemplate).execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void lockSeats_SeatAlreadyLocked() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(-2L);

        assertThatThrownBy(() -> seatLockService.lockSeats(1L, Arrays.asList("1A", "1B")))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("1B");

        verify(redisTemplate).execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void lockSeats_NotEnoughSeats() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(-999L);

        assertThatThrownBy(() -> seatLockService.lockSeats(1L, Arrays.asList("1A", "1B", "1C")))
                .isInstanceOf(SeatNotAvailableException.class)
                .hasMessageContaining("Not enough seats available");

        verify(redisTemplate).execute(any(DefaultRedisScript.class), any(List.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void releaseSeats_Success() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), any(List.class), any(String.class)))
                .thenReturn(5L);

        seatLockService.releaseSeats(1L, Arrays.asList("1A", "1B"));

        verify(redisTemplate).execute(any(DefaultRedisScript.class), any(List.class), any(String.class));
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

        verify(redisTemplate.opsForValue()).set("trip-availability:1", "40");
    }
}
