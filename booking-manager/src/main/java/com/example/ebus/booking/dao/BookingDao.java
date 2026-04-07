package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingDao extends JpaRepository<BookingEntity, Long> {
    Page<BookingEntity> findByUserId(Long userId, Pageable pageable);
}
