package com.example.ebus.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTripRequest(
    @NotNull Long routeId,
    @NotNull Long busId,
    @NotNull LocalDateTime departureTime,
    @NotNull LocalDateTime arrivalTime,
    @NotNull @Positive BigDecimal price,
    @NotNull String currency,
    @Positive int totalSeats,
    @NotNull String operatorName
) {}
