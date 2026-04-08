package com.example.ebus.search.controller;

import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.service.TripQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final TripQueryService tripQueryService;

    @GetMapping("/trips")
    public Page<TripSearchResponse> searchTrips(
            @Valid TripSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size) {
        return tripQueryService.searchTrips(
                request,
                PageRequest.of(page, Math.min(size, 100), Sort.by("departureTime").ascending()));
    }

    @GetMapping("/trips/{id}")
    public TripSearchResponse getTripById(@PathVariable String id) {
        return tripQueryService.getTripById(id);
    }

    @GetMapping("/autocomplete")
    public java.util.List<String> autocomplete(@RequestParam String q) {
        return tripQueryService.autocomplete(q);
    }
}
