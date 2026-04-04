package com.example.ebus.events.booking;

public record BookingCancelledEvent(
    Long bookingId,
    Long userId,
    Long tripId
) {}
