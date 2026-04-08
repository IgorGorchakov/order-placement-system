package com.example.ebus.booking.service;

import com.example.ebus.booking.exception.SeatNotAvailableException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatLockService implements SeatLockStrategy {

    private final SeatLockStrategy delegate;

    public SeatLockService(SeatLockStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public void lockSeats(Long tripId, List<String> seatNumbers) {
        delegate.lockSeats(tripId, seatNumbers);
    }

    @Override
    public void releaseSeats(Long tripId, List<String> seatNumbers) {
        delegate.releaseSeats(tripId, seatNumbers);
    }

    @Override
    public boolean isSeatLocked(Long tripId, String seatNumber) {
        return delegate.isSeatLocked(tripId, seatNumber);
    }

    @Override
    public void initAvailability(Long tripId, int totalSeats) {
        delegate.initAvailability(tripId, totalSeats);
    }
}
