package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventDao extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByProcessedAtIsNullOrderByCreatedAtAsc();
}
