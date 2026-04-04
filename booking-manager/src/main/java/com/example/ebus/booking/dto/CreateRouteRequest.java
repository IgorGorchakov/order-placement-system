package com.example.ebus.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateRouteRequest(
    @NotBlank String origin,
    @NotBlank String destination,
    @Positive int distanceKm,
    @Positive int estimatedDurationMinutes
) {}
