package com.example.ebus.booking.service;

import com.example.ebus.booking.exception.SeatNotAvailableException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatLockService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final String SEAT_LOCK_PREFIX = "seat-lock:";
    private static final String AVAILABILITY_PREFIX = "trip-availability:";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> lockSeatsScript;
    private final DefaultRedisScript<Long> releaseSeatsScript;

    public SeatLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.lockSeatsScript = new DefaultRedisScript<>();
        this.releaseSeatsScript = new DefaultRedisScript<>();
    }

    @PostConstruct
    public void init() {
        lockSeatsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/lock_seats.lua")));
        lockSeatsScript.setResultType(Long.class);

        releaseSeatsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/release_seats.lua")));
        releaseSeatsScript.setResultType(Long.class);
    }

    public void lockSeats(Long tripId, List<String> seatNumbers) {
        List<String> keys = seatNumbers.stream()
            .map(seat -> SEAT_LOCK_PREFIX + tripId + ":" + seat)
            .collect(Collectors.toList());

        String availabilityKey = AVAILABILITY_PREFIX + tripId;

        Long result = redisTemplate.execute(
            lockSeatsScript,
            keys,
            availabilityKey,
            String.valueOf(seatNumbers.size()),
            String.valueOf(LOCK_TTL.getSeconds())
        );

        if (result != null && result < 0) {
            if (result == -999) {
                throw new SeatNotAvailableException("Not enough seats available");
            }
            int conflictingIndex = Math.abs(result.intValue());
            if (conflictingIndex <= seatNumbers.size()) {
                throw new SeatNotAvailableException(seatNumbers.get(conflictingIndex - 1));
            }
            throw new SeatNotAvailableException("Seat not available");
        }
    }

    public void releaseSeats(Long tripId, List<String> seatNumbers) {
        String availabilityKey = AVAILABILITY_PREFIX + tripId;
        
        List<String> lockKeys = seatNumbers.stream()
            .map(seat -> SEAT_LOCK_PREFIX + tripId + ":" + seat)
            .collect(Collectors.toList());
        
        // First key is availability, rest are lock keys
        List<String> allKeys = new ArrayList<>();
        allKeys.add(availabilityKey);
        allKeys.addAll(lockKeys);

        redisTemplate.execute(
            releaseSeatsScript,
            allKeys,
            String.valueOf(seatNumbers.size())
        );
    }

    public boolean isSeatLocked(Long tripId, String seatNumber) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SEAT_LOCK_PREFIX + tripId + ":" + seatNumber));
    }

    public void initAvailability(Long tripId, int totalSeats) {
        redisTemplate.opsForValue().set(AVAILABILITY_PREFIX + tripId, String.valueOf(totalSeats));
    }
}
