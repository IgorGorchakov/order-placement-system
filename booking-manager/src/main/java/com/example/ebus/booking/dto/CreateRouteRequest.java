package com.example.ebus.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRouteRequest(
    @NotBlank @Size(max = 100) String origin,
    @NotBlank @Size(max = 100) String destination,
    @NotNull @Min(1) int distanceKm,
    @NotNull @Min(1) int estimatedDurationMinutes
) {}
