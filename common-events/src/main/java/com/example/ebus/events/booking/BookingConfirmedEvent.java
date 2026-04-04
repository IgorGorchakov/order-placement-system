package com.example.ebus.events.booking;

public record BookingConfirmedEvent(
    Long bookingId,
    Long userId,
    Long tripId
) {}
