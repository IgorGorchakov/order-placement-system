package com.example.ebus.payment.dao;

import com.example.ebus.payment.entity.OutboxEventEntity;
import com.example.ebus.payment.entity.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventDao extends JpaRepository<OutboxEventEntity, Long> {

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status = :status " +
           "AND e.retryCount < :maxRetries " +
           "AND (e.lastAttemptAt IS NULL OR e.lastAttemptAt < :cutoff) " +
           "ORDER BY e.createdAt ASC")
    List<OutboxEventEntity> findReadyForPublish(
        @Param("status") OutboxEventStatus status,
        @Param("maxRetries") int maxRetries,
        @Param("cutoff") LocalDateTime cutoff);

    List<OutboxEventEntity> findByRetryCountGreaterThanEqualAndStatusNot(int retryCount, OutboxEventStatus status);
}
