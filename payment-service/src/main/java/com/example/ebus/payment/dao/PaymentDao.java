package com.example.ebus.payment.dao;

import com.example.ebus.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentDao extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByBookingId(Long bookingId);
    Page<PaymentEntity> findByUserId(Long userId, Pageable pageable);
}
