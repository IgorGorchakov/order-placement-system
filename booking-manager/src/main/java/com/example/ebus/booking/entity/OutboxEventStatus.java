package com.example.ebus.booking.entity;

public enum OutboxEventStatus {
    PENDING,
    IN_FLIGHT,
    PROCESSED,
    FAILED
}
