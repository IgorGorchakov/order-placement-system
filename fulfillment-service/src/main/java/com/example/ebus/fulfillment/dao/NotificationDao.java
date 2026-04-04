package com.example.ebus.fulfillment.dao;

import com.example.ebus.fulfillment.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationDao extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserId(Long userId);
    List<NotificationEntity> findByBookingId(Long bookingId);
}
