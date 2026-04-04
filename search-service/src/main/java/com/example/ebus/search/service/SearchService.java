package com.example.ebus.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.exception.TripNotFoundException;
import com.example.ebus.search.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TripSearchRepository tripSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<TripSearchResponse> searchTrips(TripSearchRequest request) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (request.getOrigin() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("origin").query(request.getOrigin()))));
        }
        if (request.getDestination() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("destination").query(request.getDestination()))));
        }
        if (request.getOperator() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("operatorName").query(request.getOperator()))));
        }
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            for (String amenity : request.getAmenities()) {
                bool.must(Query.of(q -> q.term(t -> t.field("amenities").value(amenity))));
            }
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            bool.must(Query.of(q -> q.range(r -> {
                var range = r.number(n -> {
                    var nr = n.field("price");
                    if (request.getMinPrice() != null) nr.gte(request.getMinPrice().doubleValue());
                    if (request.getMaxPrice() != null) nr.lte(request.getMaxPrice().doubleValue());
                    return nr;
                });
                return range;
            })));
        }
        if (request.getDate() != null) {
            LocalDateTime dayStart = request.getDate().atStartOfDay();
            LocalDateTime dayEnd = request.getDate().atTime(LocalTime.MAX);
            bool.must(Query.of(q -> q.range(r -> r.date(d -> d
                    .field("departureTime")
                    .gte(dayStart.toString())
                    .lte(dayEnd.toString())))));
        }
        if (request.getDepartureAfter() != null && request.getDate() != null) {
            LocalDateTime after = request.getDate().atTime(request.getDepartureAfter());
            bool.must(Query.of(q -> q.range(r -> r.date(d -> d
                    .field("departureTime")
                    .gte(after.toString())))));
        }
        if (request.getDepartureBefore() != null && request.getDate() != null) {
            LocalDateTime before = request.getDate().atTime(request.getDepartureBefore());
            bool.must(Query.of(q -> q.range(r -> r.date(d -> d
                    .field("departureTime")
                    .lte(before.toString())))));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(bool.build())))
                .build();

        SearchHits<TripDocument> hits = elasticsearchOperations.search(query, TripDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> toResponse(hit.getContent()))
                .toList();
    }

    public TripSearchResponse getTripById(String id) {
        return tripSearchRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TripNotFoundException(id));
    }

    public List<String> autocomplete(String query) {
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(b -> b
                        .should(Query.of(s -> s.prefix(p -> p.field("origin").value(query.toLowerCase()))))
                        .should(Query.of(s -> s.prefix(p -> p.field("destination").value(query.toLowerCase())))))))
                .build();

        SearchHits<TripDocument> hits = elasticsearchOperations.search(nativeQuery, TripDocument.class);
        return hits.getSearchHits().stream()
                .flatMap(hit -> {
                    TripDocument doc = hit.getContent();
                    return java.util.stream.Stream.of(doc.getOrigin(), doc.getDestination());
                })
                .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()))
                .distinct()
                .sorted()
                .toList();
    }

    public void indexTrip(TripDocument document) {
        tripSearchRepository.save(document);
    }

    public void updateAvailableSeats(String tripId, int availableSeats) {
        tripSearchRepository.findById(tripId).ifPresent(doc -> {
            doc.setAvailableSeats(availableSeats);
            tripSearchRepository.save(doc);
        });
    }

    public void incrementAvailableSeats(String tripId, int count) {
        tripSearchRepository.findById(tripId).ifPresent(doc -> {
            doc.setAvailableSeats(doc.getAvailableSeats() + count);
            tripSearchRepository.save(doc);
        });
    }

    private TripSearchResponse toResponse(TripDocument doc) {
        return TripSearchResponse.builder()
                .id(doc.getId())
                .origin(doc.getOrigin())
                .destination(doc.getDestination())
                .departureTime(doc.getDepartureTime())
                .arrivalTime(doc.getArrivalTime())
                .price(doc.getPrice())
                .currency(doc.getCurrency())
                .operatorName(doc.getOperatorName())
                .amenities(doc.getAmenities())
                .availableSeats(doc.getAvailableSeats())
                .build();
    }
}
