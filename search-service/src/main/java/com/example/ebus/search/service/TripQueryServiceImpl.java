package com.example.ebus.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.dto.TripSearchRequest;
import com.example.ebus.search.dto.TripSearchResponse;
import com.example.ebus.search.exception.TripNotFoundException;
import com.example.ebus.search.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<TripSearchResponse> searchTrips(TripSearchRequest request, Pageable pageable) {
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

        // Merge all date/time bounds into a single range query
        LocalDateTime departureTimeFrom = null;
        LocalDateTime departureTimeTo = null;

        if (request.date() != null) {
            departureTimeFrom = request.date().atStartOfDay();
            departureTimeTo = request.date().atTime(LocalTime.MAX);
        }

        if (request.departureAfter() != null && request.date() != null) {
            LocalDateTime after = request.date().atTime(request.departureAfter());
            if (departureTimeFrom == null || after.isAfter(departureTimeFrom)) {
                departureTimeFrom = after;
            }
        }

        if (request.departureBefore() != null && request.date() != null) {
            LocalDateTime before = request.date().atTime(request.departureBefore());
            if (departureTimeTo == null || before.isBefore(departureTimeTo)) {
                departureTimeTo = before;
            }
        }

        if (departureTimeFrom != null || departureTimeTo != null) {
            final LocalDateTime from = departureTimeFrom;
            final LocalDateTime to = departureTimeTo;

            bool.must(Query.of(q -> q.range(r -> r.date(d -> {
                d.field("departureTime");
                if (from != null) d.gte(from.toString());
                if (to != null) d.lte(to.toString());
                return d;
            }))));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(bool.build())))
                .withPageable(pageable)
                .build();

        SearchHits<TripDocument> hits = elasticsearchOperations.search(query, TripDocument.class);
        List<TripSearchResponse> content = hits.getSearchHits().stream()
                .map(hit -> toResponse(hit.getContent()))
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
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
                .withMaxResults(10)
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
