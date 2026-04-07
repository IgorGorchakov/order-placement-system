package com.example.ebus.search.controller;

import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.service.TripQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final TripQueryService tripQueryService;

    @GetMapping("/trips")
    public List<TripSearchResponse> searchTrips(TripSearchRequest request) {
        return tripQueryService.searchTrips(request);
    }

    @GetMapping("/trips/{id}")
    public TripSearchResponse getTripById(@PathVariable String id) {
        return tripQueryService.getTripById(id);
    }

    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String q) {
        return tripQueryService.autocomplete(q);
    }
}
