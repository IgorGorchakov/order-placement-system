package com.example.ebus.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.exception.TripNotFoundException;
import com.example.ebus.search.repository.TripSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripQueryServiceImplTest {

    @Mock
    private TripSearchRepository tripSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private TripQueryServiceImpl tripQueryService;

    private TripDocument sampleTrip;
    private TripSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        sampleTrip = TripDocument.builder()
                .id("trip-1")
                .origin("NYC")
                .destination("Boston")
                .departureTime(LocalDateTime.of(2026, 4, 10, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 4, 10, 12, 0))
                .price(BigDecimal.valueOf(50))
                .currency("USD")
                .operatorName("Test Bus Co")
                .amenities(List.of("WiFi", "AC"))
                .availableSeats(30)
                .build();

        searchRequest = new TripSearchRequest(
                "NYC",
                "Boston",
                LocalDate.of(2026, 4, 10),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void searchTrips_WithFilters() {
        SearchHit<TripDocument> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(sampleTrip);

        SearchHits<TripDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(TripDocument.class)))
                .thenReturn(searchHits);

        Page<TripSearchResponse> responses = tripQueryService.searchTrips(searchRequest, PageRequest.of(0, 20));

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo("trip-1");
        assertThat(responses.getContent().get(0).getOrigin()).isEqualTo("NYC");
    }

    @Test
    void searchTrips_EmptyResult() {
        SearchHits<TripDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(TripDocument.class)))
                .thenReturn(searchHits);

        Page<TripSearchResponse> responses = tripQueryService.searchTrips(searchRequest, PageRequest.of(0, 20));

        assertThat(responses).isEmpty();
    }

    @Test
    void getTripById_Success() {
        when(tripSearchRepository.findById("trip-1")).thenReturn(Optional.of(sampleTrip));

        TripSearchResponse response = tripQueryService.getTripById("trip-1");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("trip-1");
        assertThat(response.getOperatorName()).isEqualTo("Test Bus Co");
    }

    @Test
    void getTripById_NotFound() {
        when(tripSearchRepository.findById("trip-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripQueryService.getTripById("trip-99"))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("trip-99");
    }

    @Test
    void autocomplete_Success() {
        SearchHit<TripDocument> hit1 = mock(SearchHit.class);
        when(hit1.getContent()).thenReturn(sampleTrip);

        TripDocument trip2 = TripDocument.builder()
                .id("trip-2")
                .origin("Newark")
                .destination("Philadelphia")
                .departureTime(LocalDateTime.now())
                .arrivalTime(LocalDateTime.now())
                .price(BigDecimal.valueOf(40))
                .currency("USD")
                .operatorName("Another Bus Co")
                .availableSeats(20)
                .build();

        SearchHit<TripDocument> hit2 = mock(SearchHit.class);
        when(hit2.getContent()).thenReturn(trip2);

        SearchHits<TripDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit1, hit2));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(TripDocument.class)))
                .thenReturn(searchHits);

        List<String> suggestions = tripQueryService.autocomplete("New");

        assertThat(suggestions).contains("Newark");
    }

    @Test
    void autocomplete_EmptyResult() {
        SearchHits<TripDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of());

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(TripDocument.class)))
                .thenReturn(searchHits);

        List<String> suggestions = tripQueryService.autocomplete("XYZ");

        assertThat(suggestions).isEmpty();
    }
}
