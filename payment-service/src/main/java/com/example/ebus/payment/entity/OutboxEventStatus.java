package com.example.ebus.payment.entity;

public enum OutboxEventStatus {
    PENDING,
    IN_FLIGHT,
    PROCESSED,
    FAILED
}
