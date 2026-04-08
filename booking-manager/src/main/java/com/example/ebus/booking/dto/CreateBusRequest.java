package com.example.ebus.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBusRequest(
    @NotBlank @Size(max = 50) String plateNumber,
    @NotBlank @Size(max = 100) String operatorName,
    @NotNull @Min(1) Integer totalSeats
) {}
