package com.example.ebus.booking.controller;

import com.example.ebus.booking.dto.*;
import com.example.ebus.booking.entity.BusEntity;
import com.example.ebus.booking.entity.RouteEntity;
import com.example.ebus.booking.service.TripService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trips")
    public List<TripResponse> findTrips(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripService.findTrips(origin, destination, date);
    }

    @GetMapping("/trips/{id}")
    public TripResponse getTrip(@PathVariable Long id) {
        return tripService.getTrip(id);
    }

    @GetMapping("/trips/{id}/seats")
    public SeatAvailabilityResponse getSeatAvailability(@PathVariable Long id) {
        return tripService.getSeatAvailability(id);
    }

    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.CREATED)
    public RouteEntity createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return tripService.createRoute(request);
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@Valid @RequestBody CreateTripRequest request) {
        return tripService.createTrip(request);
    }

    @PostMapping("/buses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusEntity createBus(@Valid @RequestBody CreateBusRequest request) {
        return tripService.createBus(request);
    }
}
