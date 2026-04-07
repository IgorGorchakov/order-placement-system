package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.TripResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TripQueryService {

    Page<TripResponse> findTrips(String origin, String destination, LocalDate date, Pageable pageable);

    TripResponse getTrip(Long id);
}
