package com.example.ebus.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripResponse(
    Long id,
    Long routeId,
    Long busId,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price,
    String currency,
    int totalSeats,
    String operatorName
) {}
