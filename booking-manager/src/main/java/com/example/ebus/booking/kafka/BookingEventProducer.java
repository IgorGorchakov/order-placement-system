package com.example.ebus.booking.kafka;

import com.example.ebus.booking.dao.OutboxEventDao;
import com.example.ebus.booking.entity.OutboxEventEntity;
import com.example.ebus.booking.entity.OutboxEventStatus;
import com.example.ebus.booking.service.OutboxEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BookingEventProducer implements OutboxEventPublisher {

    private static final int MAX_RETRIES = 5;
    private static final long BASE_DELAY_SECONDS = 10;
    private static final long KAFKA_SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventDao outboxEventDao;

    public BookingEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                OutboxEventDao outboxEventDao) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventDao = outboxEventDao;
    }

    @Override
    public void publishOutboxEvents() {
        LocalDateTime cutoff = calculateCutoffTime();
        List<OutboxEventEntity> events = outboxEventDao.findReadyForPublish(OutboxEventStatus.PENDING, MAX_RETRIES, cutoff);

        if (events.isEmpty()) {
            return;
        }

        // Mark events as IN_FLIGHT
        events.forEach(event -> {
            event.setStatus(OutboxEventStatus.IN_FLIGHT);
            event.setLastAttemptAt(LocalDateTime.now());
            event.setRetryCount(event.getRetryCount() + 1);
        });
        outboxEventDao.saveAll(events);

        // Process each event
        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload())
                    .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                event.setStatus(OutboxEventStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                log.debug("Successfully sent outbox event: {}", event.getId());
            } catch (Exception ex) {
                event.setStatus(OutboxEventStatus.PENDING); // Reset for retry
                log.warn("Failed to send outbox event (id={}, retry={}): {}",
                    event.getId(), event.getRetryCount(), ex.getMessage());
            }
        }

        outboxEventDao.saveAll(events);

        // Mark permanently failed events
        markPermanentlyFailedEvents();
    }

    private LocalDateTime calculateCutoffTime() {
        return LocalDateTime.now().minusSeconds(BASE_DELAY_SECONDS);
    }

    private void markPermanentlyFailedEvents() {
        List<OutboxEventEntity> failed = outboxEventDao.findByRetryCountGreaterThanEqualAndStatusNot(MAX_RETRIES, OutboxEventStatus.PROCESSED);
        if (!failed.isEmpty()) {
            failed.forEach(event -> {
                if (event.getStatus() != OutboxEventStatus.PROCESSED) {
                    event.setStatus(OutboxEventStatus.FAILED);
                    log.error("Outbox event permanently failed after {} retries: {}", 
                        event.getRetryCount(), event.getId());
                }
            });
            outboxEventDao.saveAll(failed);
        }
    }
}
