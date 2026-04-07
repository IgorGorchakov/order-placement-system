package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.document.SeatLayoutDocument;
import com.example.ebus.booking.dto.SeatAvailabilityResponse;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.booking.repository.SeatLayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {

    private final TripDao tripDao;
    private final SeatLayoutRepository seatLayoutRepository;
    private final SeatLockService seatLockService;

    @Override
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
}
