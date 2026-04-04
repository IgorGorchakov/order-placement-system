package com.example.ebus.user.dao;

import com.example.ebus.user.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodDao extends JpaRepository<PaymentMethodEntity, Long> {

    List<PaymentMethodEntity> findByUserId(Long userId);
}
