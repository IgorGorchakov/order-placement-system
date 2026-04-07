package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.CreateTripRequest;
import com.example.ebus.booking.dto.TripResponse;

public interface TripManagementService {

    TripResponse createTrip(CreateTripRequest request);
}
