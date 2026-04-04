package com.example.ebus.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
    Long id,
    Long userId,
    Long tripId,
    String status,
    List<String> seatNumbers,
    BigDecimal totalPrice,
    String currency,
    LocalDateTime createdAt
) {}
