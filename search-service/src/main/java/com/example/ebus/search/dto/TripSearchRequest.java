package com.example.ebus.search.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TripSearchRequest(
    String origin,
    String destination,
    LocalDate date,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    List<String> amenities,
    String operator,
    LocalTime departureAfter,
    LocalTime departureBefore
) {}
