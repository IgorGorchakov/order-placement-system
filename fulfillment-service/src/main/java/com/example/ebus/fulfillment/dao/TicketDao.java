package com.example.ebus.fulfillment.dao;

import com.example.ebus.fulfillment.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketDao extends JpaRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByBookingId(Long bookingId);
    Page<TicketEntity> findByUserId(Long userId, Pageable pageable);
}
