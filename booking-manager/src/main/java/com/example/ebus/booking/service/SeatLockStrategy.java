package com.example.ebus.booking.service;

import java.util.List;

public interface SeatLockStrategy {
    void lockSeats(Long tripId, List<String> seatNumbers);
    void releaseSeats(Long tripId, List<String> seatNumbers);
    boolean isSeatLocked(Long tripId, String seatNumber);
    void initAvailability(Long tripId, int totalSeats);
}