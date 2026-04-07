package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.SeatAvailabilityResponse;

public interface SeatAvailabilityService {

    SeatAvailabilityResponse getSeatAvailability(Long tripId);
}
