package com.example.ebus.booking.exception;

public class TripNotFoundException extends RuntimeException {
    public TripNotFoundException(Long id) {
        super("Trip not found: " + id);
    }
}
