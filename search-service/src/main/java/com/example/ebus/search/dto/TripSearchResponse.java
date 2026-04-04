package com.example.ebus.search.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TripSearchResponse {
    private String id;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private String currency;
    private String operatorName;
    private List<String> amenities;
    private int availableSeats;
}
