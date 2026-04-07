package com.example.ebus.fulfillment.dao;

import com.example.ebus.fulfillment.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDao extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findByUserId(Long userId, Pageable pageable);
    Page<NotificationEntity> findByBookingId(Long bookingId, Pageable pageable);
}
