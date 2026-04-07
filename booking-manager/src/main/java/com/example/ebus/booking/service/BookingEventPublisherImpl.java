package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.OutboxEventDao;
import com.example.ebus.booking.entity.OutboxEventEntity;
import com.example.ebus.events.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingEventPublisherImpl implements BookingEventPublisher {

    private final OutboxEventDao outboxEventDao;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void publishBookingCreatedEvent(Long bookingId, Object event) {
        saveEvent("Booking", bookingId.toString(), Topics.BOOKING_CREATED, event);
    }

    @Override
    @Transactional
    public void publishBookingCancelledEvent(Long bookingId, Object event) {
        saveEvent("Booking", bookingId.toString(), Topics.BOOKING_CANCELLED, event);
    }

    private void saveEvent(String aggregateType, String aggregateId, String eventType, Object event) {
        try {
            OutboxEventEntity eventEntity = new OutboxEventEntity();
            eventEntity.setAggregateType(aggregateType);
            eventEntity.setAggregateId(aggregateId);
            eventEntity.setEventType(eventType);
            eventEntity.setPayload(objectMapper.writeValueAsString(event));
            outboxEventDao.save(eventEntity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
