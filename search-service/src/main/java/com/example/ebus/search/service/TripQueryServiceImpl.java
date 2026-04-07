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
public class TripQueryServiceImpl implements TripQueryService {

    private final TripSearchRepository tripSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<TripSearchResponse> searchTrips(TripSearchRequest request) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (request.origin() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("origin").query(request.origin()))));
        }
        if (request.destination() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("destination").query(request.destination()))));
        }
        if (request.operator() != null) {
            bool.must(Query.of(q -> q.match(m -> m.field("operatorName").query(request.operator()))));
        }
        if (request.amenities() != null && !request.amenities().isEmpty()) {
            for (String amenity : request.amenities()) {
                bool.must(Query.of(q -> q.term(t -> t.field("amenities").value(amenity))));
            }
        }
        if (request.minPrice() != null || request.maxPrice() != null) {
            bool.must(Query.of(q -> q.range(r -> {
                var range = r.number(n -> {
                    var nr = n.field("price");
                    if (request.minPrice() != null) nr.gte(request.minPrice().doubleValue());
                    if (request.maxPrice() != null) nr.lte(request.maxPrice().doubleValue());
                    return nr;
                });
                return range;
            })));
        }
        if (request.date() != null) {
            LocalDateTime dayStart = request.date().atStartOfDay();
            LocalDateTime dayEnd = request.date().atTime(LocalTime.MAX);
            bool.must(Query.of(q -> q.range(r -> r.date(d -> d
                    .field("departureTime")
                    .gte(dayStart.toString())
                    .lte(dayEnd.toString())))));
        }
        if (request.departureAfter() != null && request.date() != null) {
            LocalDateTime after = request.date().atTime(request.departureAfter());
            bool.must(Query.of(q -> q.range(r -> r.date(d -> d
                    .field("departureTime")
                    .gte(after.toString())))));
        }
        if (request.departureBefore() != null && request.date() != null) {
            LocalDateTime before = request.date().atTime(request.departureBefore());
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

    @Override
    public TripSearchResponse getTripById(String id) {
        return tripSearchRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new TripNotFoundException(id));
    }

    @Override
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
