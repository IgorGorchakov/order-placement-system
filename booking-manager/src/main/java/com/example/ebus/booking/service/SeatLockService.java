package com.example.ebus.booking.service;

import com.example.ebus.booking.exception.SeatNotAvailableException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatLockService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final String SEAT_LOCK_PREFIX = "seat-lock:";
    private static final String AVAILABILITY_PREFIX = "trip-availability:";

    private final StringRedisTemplate redisTemplate;

    public SeatLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void lockSeats(Long tripId, List<String> seatNumbers) {
        List<String> lockedKeys = new ArrayList<>();
        try {
            for (String seat : seatNumbers) {
                String key = SEAT_LOCK_PREFIX + tripId + ":" + seat;
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "locked", LOCK_TTL);
                if (Boolean.FALSE.equals(acquired)) {
                    throw new SeatNotAvailableException(seat);
                }
                lockedKeys.add(key);
            }
            redisTemplate.opsForValue().decrement(AVAILABILITY_PREFIX + tripId, seatNumbers.size());
        } catch (SeatNotAvailableException ex) {
            for (String key : lockedKeys) {
                redisTemplate.delete(key);
            }
            throw ex;
        }
    }

    public void releaseSeats(Long tripId, List<String> seatNumbers) {
        for (String seat : seatNumbers) {
            redisTemplate.delete(SEAT_LOCK_PREFIX + tripId + ":" + seat);
        }
        redisTemplate.opsForValue().increment(AVAILABILITY_PREFIX + tripId, seatNumbers.size());
    }

    public boolean isSeatLocked(Long tripId, String seatNumber) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SEAT_LOCK_PREFIX + tripId + ":" + seatNumber));
    }

    public void initAvailability(Long tripId, int totalSeats) {
        redisTemplate.opsForValue().set(AVAILABILITY_PREFIX + tripId, String.valueOf(totalSeats));
    }
}
