package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.RouteDao;
import com.example.ebus.booking.dto.CreateRouteRequest;
import com.example.ebus.booking.entity.RouteEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteManagementServiceImplTest {

    @Mock
    private RouteDao routeDao;

    @InjectMocks
    private RouteManagementServiceImpl routeManagementService;

    @Test
    void createRoute_Success() {
        CreateRouteRequest request = new CreateRouteRequest("NYC", "Boston", 350, 240);
        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setOrigin("NYC");
        route.setDestination("Boston");

        when(routeDao.save(any(RouteEntity.class))).thenReturn(route);

        RouteEntity result = routeManagementService.createRoute(request);

        assertThat(result).isNotNull();
        assertThat(result.getOrigin()).isEqualTo("NYC");
        verify(routeDao).save(any(RouteEntity.class));
    }
}
