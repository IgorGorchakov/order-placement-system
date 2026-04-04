package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteDao extends JpaRepository<RouteEntity, Long> {
}
