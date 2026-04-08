package com.example.ebus.search.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Monitors dead-letter topics for failed messages that exhausted all retries.
 * Logs the details for manual investigation and replay.
 */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @KafkaListener(
        topics = {"trip-created.DLT", "seat-availability-updated.DLT", "booking-cancelled.DLT"},
        groupId = "search-service-dlt")
    public void handleDeadLetter(ConsumerRecord<String, Object> record) {
        log.error("Message landed in DLT — manual intervention required. " +
            "Topic={}, Partition={}, Offset={}, Key={}, Value={}",
            record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
