package com.example.ebus.events.search;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record SeatAvailabilityUpdatedEvent(
    @NotBlank String tripId,
    @NotNull @Min(0) Integer availableSeats
) {
    public SeatAvailabilityUpdatedEvent {
        Objects.requireNonNull(tripId, "tripId must not be null");
        Objects.requireNonNull(availableSeats, "availableSeats must not be null");
    }
}
