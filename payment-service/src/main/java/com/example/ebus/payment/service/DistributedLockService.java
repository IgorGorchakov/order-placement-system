package com.example.ebus.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Redis-based distributed lock using SET NX EX pattern.
 * Ensures mutual exclusion across multiple service instances.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration DEFAULT_TTL = Duration.ofSeconds(30);

    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";

    /**
     * Attempts to acquire a distributed lock.
     *
     * @param lockKey the lock key
     * @return the lock value (owner token) if acquired, or null if the lock is already held
     */
    public String tryAcquire(String lockKey) {
        return tryAcquire(lockKey, DEFAULT_TTL);
    }

    /**
     * Attempts to acquire a distributed lock with a specified TTL.
     *
     * @param lockKey the lock key
     * @param ttl     how long the lock is held before auto-expiry
     * @return the lock value (owner token) if acquired, or null if the lock is already held
     */
    public String tryAcquire(String lockKey, Duration ttl) {
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, ttl);
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock: key={}, value={}", lockKey, lockValue);
            return lockValue;
        }
        log.debug("Failed to acquire lock: key={}", lockKey);
        return null;
    }

    /**
     * Releases a distributed lock, but only if the caller is the owner.
     *
     * @param lockKey   the lock key
     * @param lockValue the owner token returned by {@link #tryAcquire}
     */
    public void release(String lockKey, String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, List.of(lockKey), lockValue);
        if (result != null && result == 1) {
            log.debug("Released lock: key={}, value={}", lockKey, lockValue);
        } else {
            log.warn("Lock release failed (not owner or expired): key={}, value={}", lockKey, lockValue);
        }
    }
}
