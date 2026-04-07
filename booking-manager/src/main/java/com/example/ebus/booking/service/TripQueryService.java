package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.TripResponse;

import java.time.LocalDate;
import java.util.List;

public interface TripQueryService {

    List<TripResponse> findTrips(String origin, String destination, LocalDate date);

    TripResponse getTrip(Long id);
}
