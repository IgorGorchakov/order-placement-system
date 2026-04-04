package com.example.ebus.fulfillment.dto;

import java.time.LocalDateTime;

public record TicketResponse(
    Long id,
    Long bookingId,
    Long userId,
    Long tripId,
    String ticketCode,
    String status,
    LocalDateTime issuedAt,
    LocalDateTime cancelledAt
) {}
