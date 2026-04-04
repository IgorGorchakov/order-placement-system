package com.example.ebus.search.repository;

import com.example.ebus.search.document.TripDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TripSearchRepository extends ElasticsearchRepository<TripDocument, String> {

    List<TripDocument> findByOriginAndDestination(String origin, String destination);
}
