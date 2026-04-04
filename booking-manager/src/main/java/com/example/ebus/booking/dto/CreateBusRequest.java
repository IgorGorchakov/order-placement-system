package com.example.ebus.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateBusRequest(
    @NotBlank String plateNumber,
    @NotBlank String operatorName,
    @Positive int totalSeats
) {}
