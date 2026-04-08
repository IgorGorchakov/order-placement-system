package com.example.ebus.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateBookingRequest(
    @NotNull Long userId,
    @NotNull Long tripId,
    @NotEmpty @Size(min = 1, max = 10) List<@NotBlank String> seatNumbers
) {}
