package com.example.ebus.booking.controller;

import com.example.ebus.booking.dto.*;
import com.example.ebus.booking.entity.BusEntity;
import com.example.ebus.booking.entity.RouteEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.booking.service.TripService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @Test
    void findTrips_Success() {
        TripResponse response = new TripResponse(
                1L, 10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        when(tripService.findTrips(eq("NYC"), eq("Boston"), eq(LocalDate.of(2026, 4, 10))))
                .thenReturn(List.of(response));

        List<TripResponse> responses = tripController.findTrips("NYC", "Boston", LocalDate.of(2026, 4, 10));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(1);
    }

    @Test
    void getTrip_Success() {
        TripResponse response = new TripResponse(
                1L, 10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        when(tripService.getTrip(1L)).thenReturn(response);

        TripResponse result = tripController.getTrip(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    void getTrip_NotFound() {
        when(tripService.getTrip(1L)).thenThrow(new TripNotFoundException(1L));

        assertThatThrownBy(() -> tripController.getTrip(1L))
                .isInstanceOf(TripNotFoundException.class);
    }

    @Test
    void getSeatAvailability_Success() {
        SeatAvailabilityResponse response = new SeatAvailabilityResponse(
                1L, 20L, 10, 4, Map.of("1A", "window"), Map.of("1A", true));

        when(tripService.getSeatAvailability(1L)).thenReturn(response);

        SeatAvailabilityResponse result = tripController.getSeatAvailability(1L);

        assertThat(result).isNotNull();
        assertThat(result.tripId()).isEqualTo(1);
        assertThat(result.rows()).isEqualTo(10);
    }

    @Test
    void createRoute_Success() {
        CreateRouteRequest request = new CreateRouteRequest("NYC", "Boston", 350, 240);
        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setOrigin("NYC");

        when(tripService.createRoute(any(CreateRouteRequest.class))).thenReturn(route);

        RouteEntity result = tripController.createRoute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void createTrip_Success() {
        CreateTripRequest request = new CreateTripRequest(
                10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        TripResponse response = new TripResponse(
                1L, 10L, 20L, LocalDateTime.of(2026, 4, 10, 8, 0),
                LocalDateTime.of(2026, 4, 10, 12, 0), BigDecimal.valueOf(50),
                "USD", 40, "Test Bus Co");

        when(tripService.createTrip(any(CreateTripRequest.class))).thenReturn(response);

        TripResponse result = tripController.createTrip(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    void createBus_Success() {
        CreateBusRequest request = new CreateBusRequest("ABC-1234", "Test Bus Co", 40);
        BusEntity bus = new BusEntity();
        bus.setId(1L);
        bus.setPlateNumber("ABC-1234");

        when(tripService.createBus(any(CreateBusRequest.class))).thenReturn(bus);

        BusEntity result = tripController.createBus(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
    }
}
