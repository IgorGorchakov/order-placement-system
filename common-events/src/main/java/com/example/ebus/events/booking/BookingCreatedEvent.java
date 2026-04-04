package com.example.ebus.events.booking;

import java.math.BigDecimal;
import java.util.List;

public record BookingCreatedEvent(
    Long bookingId,
    Long userId,
    Long tripId,
    List<String> seatNumbers,
    BigDecimal totalPrice,
    String currency
) {}
