package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.CreateRouteRequest;
import com.example.ebus.booking.entity.RouteEntity;

public interface RouteManagementService {

    RouteEntity createRoute(CreateRouteRequest request);
}
