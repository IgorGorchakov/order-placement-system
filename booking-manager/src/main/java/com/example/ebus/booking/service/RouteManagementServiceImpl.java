package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.RouteDao;
import com.example.ebus.booking.dto.CreateRouteRequest;
import com.example.ebus.booking.entity.RouteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RouteManagementServiceImpl implements RouteManagementService {

    private final RouteDao routeDao;

    @Override
    @Transactional
    public RouteEntity createRoute(CreateRouteRequest request) {
        RouteEntity route = new RouteEntity();
        route.setOrigin(request.origin());
        route.setDestination(request.destination());
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        return routeDao.save(route);
    }
}
