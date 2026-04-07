package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.dto.CreateTripRequest;
import com.example.ebus.booking.dto.TripResponse;
import com.example.ebus.booking.entity.TripEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripManagementServiceImpl implements TripManagementService {

    private final TripDao tripDao;
    private final SeatLockService seatLockService;

    @Override
    @Transactional
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

    private TripResponse toResponse(TripEntity entity) {
        return new TripResponse(
                entity.getId(), entity.getRouteId(), entity.getBusId(),
                entity.getDepartureTime(), entity.getArrivalTime(),
                entity.getPrice(), entity.getCurrency(),
                entity.getTotalSeats(), entity.getOperatorName()
        );
    }
}
