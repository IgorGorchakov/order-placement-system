package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingDao extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUserId(Long userId);
}
