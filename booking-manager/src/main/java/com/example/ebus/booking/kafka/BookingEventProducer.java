package com.example.ebus.booking.kafka;

import com.example.ebus.booking.dao.OutboxEventDao;
import com.example.ebus.booking.entity.OutboxEventEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventDao outboxEventDao;

    public BookingEventProducer(KafkaTemplate<String, String> kafkaTemplate, OutboxEventDao outboxEventDao) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventDao = outboxEventDao;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEventEntity> events = outboxEventDao.findByProcessedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEventEntity event : events) {
            kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
            event.setProcessedAt(LocalDateTime.now());
            outboxEventDao.save(event);
        }
    }
}
