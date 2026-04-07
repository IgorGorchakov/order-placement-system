package com.example.ebus.search.service;

import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TripQueryService {

    Page<TripSearchResponse> searchTrips(TripSearchRequest request, Pageable pageable);

    TripSearchResponse getTripById(String id);

    List<String> autocomplete(String query);
}
