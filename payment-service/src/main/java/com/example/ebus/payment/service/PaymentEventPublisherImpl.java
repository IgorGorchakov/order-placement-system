package com.example.ebus.payment.service;

import com.example.ebus.events.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Override
    public void publishPaymentCompletedEvent(Long paymentId, Object event) {
        saveEvent("Payment", paymentId.toString(), Topics.PAYMENT_COMPLETED, event);
    }

    @Override
    public void publishPaymentFailedEvent(Long paymentId, Object event) {
        saveEvent("Payment", paymentId.toString(), Topics.PAYMENT_FAILED, event);
    }

    private void saveEvent(String aggregateType, String aggregateId, String eventType, Object event) {
        try {
            outboxService.saveEvent(aggregateType, aggregateId, eventType, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for {} {}", aggregateType, aggregateId, e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
