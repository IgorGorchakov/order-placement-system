package com.example.ebus.payment.dao;

import com.example.ebus.payment.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventDao extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByProcessedAtIsNullOrderByCreatedAtAsc();
}
