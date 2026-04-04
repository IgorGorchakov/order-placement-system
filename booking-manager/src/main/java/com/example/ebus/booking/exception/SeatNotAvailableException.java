package com.example.ebus.booking.exception;

public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException(String seat) {
        super("Seat not available: " + seat);
    }
}
