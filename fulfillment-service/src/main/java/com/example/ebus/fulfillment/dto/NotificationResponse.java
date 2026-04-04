package com.example.ebus.fulfillment.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long userId,
    Long bookingId,
    String type,
    String channel,
    String recipient,
    String message,
    LocalDateTime sentAt
) {}
