package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.dto.TripResponse;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.TripNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TripQueryServiceImpl implements TripQueryService {

    private final TripDao tripDao;

    @Override
    public Page<TripResponse> findTrips(String origin, String destination, LocalDate date, Pageable pageable) {
        LocalDateTime dateFrom = date != null ? date.atStartOfDay() : null;
        LocalDateTime dateTo = date != null ? date.plusDays(1).atStartOfDay() : null;
        return tripDao.findTrips(origin, destination, dateFrom, dateTo, pageable)
                .map(this::toResponse);
    }

    @Override
    public TripResponse getTrip(Long id) {
        return toResponse(tripDao.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id)));
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
