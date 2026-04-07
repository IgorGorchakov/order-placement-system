package com.example.ebus.booking.controller;

import com.example.ebus.booking.dto.*;
import com.example.ebus.booking.entity.BusEntity;
import com.example.ebus.booking.entity.RouteEntity;
import com.example.ebus.booking.service.BusManagementService;
import com.example.ebus.booking.service.RouteManagementService;
import com.example.ebus.booking.service.SeatAvailabilityService;
import com.example.ebus.booking.service.TripManagementService;
import com.example.ebus.booking.service.TripQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class TripController {

    private final TripQueryService tripQueryService;
    private final TripManagementService tripManagementService;
    private final SeatAvailabilityService seatAvailabilityService;
    private final RouteManagementService routeManagementService;
    private final BusManagementService busManagementService;

    public TripController(TripQueryService tripQueryService,
                          TripManagementService tripManagementService,
                          SeatAvailabilityService seatAvailabilityService,
                          RouteManagementService routeManagementService,
                          BusManagementService busManagementService) {
        this.tripQueryService = tripQueryService;
        this.tripManagementService = tripManagementService;
        this.seatAvailabilityService = seatAvailabilityService;
        this.routeManagementService = routeManagementService;
        this.busManagementService = busManagementService;
    }

    @GetMapping("/trips")
    public Page<TripResponse> findTrips(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size) {
        return tripQueryService.findTrips(
                origin, destination, date,
                PageRequest.of(page, Math.min(size, 100), Sort.by("departureTime").ascending()));
    }

    @GetMapping("/trips/{id}")
    public TripResponse getTrip(@PathVariable Long id) {
        return tripQueryService.getTrip(id);
    }

    @GetMapping("/trips/{id}/seats")
    public SeatAvailabilityResponse getSeatAvailability(@PathVariable Long id) {
        return seatAvailabilityService.getSeatAvailability(id);
    }

    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteEntity createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return routeManagementService.createRoute(request);
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@Valid @RequestBody CreateTripRequest request) {
        return tripManagementService.createTrip(request);
    }

    @PostMapping("/buses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusEntity createBus(@Valid @RequestBody CreateBusRequest request) {
        return busManagementService.createBus(request);
    }
}
