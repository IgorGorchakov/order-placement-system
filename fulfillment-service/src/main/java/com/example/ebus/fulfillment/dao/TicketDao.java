package com.example.ebus.fulfillment.dao;

import com.example.ebus.fulfillment.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketDao extends JpaRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByBookingId(Long bookingId);
    List<TicketEntity> findByUserId(Long userId);
}
