package com.example.ebus.booking.dto;

import java.util.Map;

public record SeatAvailabilityResponse(
    Long tripId,
    Long busId,
    int rows,
    int seatsPerRow,
    Map<String, String> seatMap,
    Map<String, Boolean> availability
) {}
