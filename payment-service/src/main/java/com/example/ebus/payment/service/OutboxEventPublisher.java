package com.example.ebus.payment.service;

public interface OutboxEventPublisher {
    void publishOutboxEvents();
}