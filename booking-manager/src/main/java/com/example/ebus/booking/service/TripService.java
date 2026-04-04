package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BusDao;
import com.example.ebus.booking.dao.RouteDao;
import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.document.SeatLayoutDocument;
import com.example.ebus.booking.dto.*;
import com.example.ebus.booking.entity.BusEntity;
import com.example.ebus.booking.entity.RouteEntity;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.booking.repository.SeatLayoutRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TripService {

    private final TripDao tripDao;
    private final RouteDao routeDao;
    private final BusDao busDao;
    private final SeatLayoutRepository seatLayoutRepository;
    private final SeatLockService seatLockService;

    public TripService(TripDao tripDao, RouteDao routeDao, BusDao busDao,
                       SeatLayoutRepository seatLayoutRepository, SeatLockService seatLockService) {
        this.tripDao = tripDao;
        this.routeDao = routeDao;
        this.busDao = busDao;
        this.seatLayoutRepository = seatLayoutRepository;
        this.seatLockService = seatLockService;
    }

    public List<TripResponse> findTrips(String origin, String destination, LocalDate date) {
        LocalDateTime dateFrom = date != null ? date.atStartOfDay() : null;
        LocalDateTime dateTo = date != null ? date.plusDays(1).atStartOfDay() : null;
        return tripDao.findTrips(origin, destination, dateFrom, dateTo)
                .stream().map(this::toResponse).toList();
    }

    public TripResponse getTrip(Long id) {
        return toResponse(tripDao.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id)));
    }

    public SeatAvailabilityResponse getSeatAvailability(Long tripId) {
        TripEntity trip = tripDao.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        SeatLayoutDocument layout = seatLayoutRepository.findByBusId(trip.getBusId())
                .orElse(null);

        Map<String, String> seatMap = layout != null ? layout.getSeatMap() : Map.of();
        Map<String, Boolean> availability = new HashMap<>();
        for (String seat : seatMap.keySet()) {
            availability.put(seat, !seatLockService.isSeatLocked(tripId, seat));
        }

        return new SeatAvailabilityResponse(
                tripId, trip.getBusId(),
                layout != null ? layout.getRows() : 0,
                layout != null ? layout.getSeatsPerRow() : 0,
                seatMap, availability
        );
    }

    public RouteEntity createRoute(CreateRouteRequest request) {
        RouteEntity route = new RouteEntity();
        route.setOrigin(request.origin());
        route.setDestination(request.destination());
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        return routeDao.save(route);
    }

    public TripResponse createTrip(CreateTripRequest request) {
        TripEntity trip = new TripEntity();
        trip.setRouteId(request.routeId());
        trip.setBusId(request.busId());
        trip.setDepartureTime(request.departureTime());
        trip.setArrivalTime(request.arrivalTime());
        trip.setPrice(request.price());
        trip.setCurrency(request.currency());
        trip.setTotalSeats(request.totalSeats());
        trip.setOperatorName(request.operatorName());
        trip = tripDao.save(trip);
        seatLockService.initAvailability(trip.getId(), trip.getTotalSeats());
        return toResponse(trip);
    }

    public BusEntity createBus(CreateBusRequest request) {
        BusEntity bus = new BusEntity();
        bus.setPlateNumber(request.plateNumber());
        bus.setOperatorName(request.operatorName());
        bus.setTotalSeats(request.totalSeats());
        return busDao.save(bus);
    }

    private TripResponse toResponse(TripEntity entity) {
        return new TripResponse(
                entity.getId(), entity.getRouteId(), entity.getBusId(),
                entity.getDepartureTime(), entity.getArrivalTime(),
                entity.getPrice(), entity.getCurrency(),
                entity.getTotalSeats(), entity.getOperatorName()
        );
    }
}
