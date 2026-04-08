package com.example.ebus.events.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record BookingCancelledSeatEvent(
    @NotBlank String tripId,
    @NotNull Integer seatsToRelease
) {
    public BookingCancelledSeatEvent {
        Objects.requireNonNull(tripId, "tripId must not be null");
        Objects.requireNonNull(seatsToRelease, "seatsToRelease must not be null");
    }
}
