package com.example.ebus.search.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class TripSearchRequest {
    private String origin;
    private String destination;
    private LocalDate date;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> amenities;
    private String operator;
    private LocalTime departureAfter;
    private LocalTime departureBefore;
}
