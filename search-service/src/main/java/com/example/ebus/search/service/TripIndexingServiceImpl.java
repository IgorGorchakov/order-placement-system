package com.example.ebus.search.service;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripIndexingServiceImpl implements TripIndexingService {

    private final TripSearchRepository tripSearchRepository;

    @Override
    public void indexTrip(TripDocument document) {
        tripSearchRepository.save(document);
    }

    @Override
    public void updateAvailableSeats(String tripId, int availableSeats) {
        tripSearchRepository.findById(tripId).ifPresent(doc -> {
            doc.setAvailableSeats(availableSeats);
            tripSearchRepository.save(doc);
        });
    }

    @Override
    public void incrementAvailableSeats(String tripId, int count) {
        tripSearchRepository.findById(tripId).ifPresent(doc -> {
            doc.setAvailableSeats(doc.getAvailableSeats() + count);
            tripSearchRepository.save(doc);
        });
    }
}
