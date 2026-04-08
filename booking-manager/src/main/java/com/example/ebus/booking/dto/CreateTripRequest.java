package com.example.ebus.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTripRequest(
    @NotNull Long routeId,
    @NotNull Long busId,
    @NotNull LocalDateTime departureTime,
    @NotNull LocalDateTime arrivalTime,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotBlank String currency,
    @NotNull @Min(1) Integer totalSeats,
    @NotBlank String operatorName
) {}
