package com.example.ebus.search.controller;

import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.exception.TripNotFoundException;
import com.example.ebus.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @Test
    void searchTrips_Success() {
        TripSearchRequest request = new TripSearchRequest();
        request.setOrigin("NYC");
        request.setDestination("Boston");

        TripSearchResponse response = TripSearchResponse.builder()
                .id("trip-1")
                .origin("NYC")
                .destination("Boston")
                .departureTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .price(BigDecimal.valueOf(50))
                .currency("USD")
                .operatorName("Test Bus Co")
                .availableSeats(30)
                .build();

        when(searchService.searchTrips(request)).thenReturn(List.of(response));

        List<TripSearchResponse> responses = searchController.searchTrips(request);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo("trip-1");
    }

    @Test
    void getTripById_Success() {
        TripSearchResponse response = TripSearchResponse.builder()
                .id("trip-1")
                .origin("NYC")
                .destination("Boston")
                .departureTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .price(BigDecimal.valueOf(50))
                .currency("USD")
                .operatorName("Test Bus Co")
                .availableSeats(30)
                .build();

        when(searchService.getTripById("trip-1")).thenReturn(response);

        TripSearchResponse result = searchController.getTripById("trip-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("trip-1");
    }

    @Test
    void getTripById_NotFound() {
        when(searchService.getTripById("trip-99")).thenThrow(new TripNotFoundException("trip-99"));

        assertThatThrownBy(() -> searchController.getTripById("trip-99"))
                .isInstanceOf(TripNotFoundException.class);
    }

    @Test
    void autocomplete_Success() {
        when(searchService.autocomplete("New")).thenReturn(List.of("Newark", "NYC"));

        List<String> suggestions = searchController.autocomplete("New");

        assertThat(suggestions).hasSize(2);
        assertThat(suggestions.get(0)).isEqualTo("Newark");
    }
}
