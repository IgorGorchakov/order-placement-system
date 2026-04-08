package com.example.ebus.search.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ValidTripSearchRequest
public record TripSearchRequest(
    @Size(max = 100) String origin,
    @Size(max = 100) String destination,
    @FutureOrPresent(message = "Date must be today or in the future") LocalDate date,
    @DecimalMin(value = "0.00", message = "Min price cannot be negative") BigDecimal minPrice,
    @DecimalMin(value = "0.00") BigDecimal maxPrice,
    @Size(max = 10) List<@Size(max = 50) String> amenities,
    @Size(max = 100) String operator,
    LocalTime departureAfter,
    LocalTime departureBefore,
    AmenityMatchStrategy amenityMatch
) {
    public enum AmenityMatchStrategy {
        ANY,  // OR — match trips with any selected amenity
        ALL   // AND — match trips with all selected amenities
    }
}
