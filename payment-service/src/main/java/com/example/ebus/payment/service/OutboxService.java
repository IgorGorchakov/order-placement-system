package com.example.ebus.payment.service;

import com.example.ebus.payment.dao.OutboxEventDao;
import com.example.ebus.payment.entity.OutboxEventEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private final OutboxEventDao outboxEventDao;

    public OutboxService(OutboxEventDao outboxEventDao) {
        this.outboxEventDao = outboxEventDao;
    }

    @Transactional
    public void saveEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        outboxEventDao.save(event);
    }
}
