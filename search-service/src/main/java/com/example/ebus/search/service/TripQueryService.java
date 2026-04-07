package com.example.ebus.search.service;

import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;

import java.util.List;

public interface TripQueryService {

    List<TripSearchResponse> searchTrips(TripSearchRequest request);

    TripSearchResponse getTripById(String id);

    List<String> autocomplete(String query);
}
