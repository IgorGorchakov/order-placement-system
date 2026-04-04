package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.BusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusDao extends JpaRepository<BusEntity, Long> {
}
