package com.example.ebus.search.service;

import com.example.ebus.search.document.TripDocument;
import com.example.ebus.search.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripIndexingServiceImpl implements TripIndexingService {

    private final TripSearchRepository tripSearchRepository;

    @Override
    public void indexTrip(TripDocument document) {
        tripSearchRepository.save(document);
    }

    @Override
    public void updateAvailableSeats(String tripId, int availableSeats) {
        try {
            TripDocument doc = tripSearchRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip document not found: " + tripId));
            
            doc.setAvailableSeats(availableSeats);
            tripSearchRepository.save(doc);
            
            log.info("Updated available seats to {} for tripId={}", availableSeats, tripId);

        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            log.warn("Concurrent update detected for tripId={}: {}", tripId, e.getMessage());
            throw new RuntimeException("Concurrent modification detected, please retry", e);
        } catch (Exception e) {
            log.error("Failed to update available seats for tripId={}", tripId, e);
            throw e;
        }
    }

    @Override
    public void incrementAvailableSeats(String tripId, int count) {
        try {
            TripDocument doc = tripSearchRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip document not found: " + tripId));
            
            int newCount = doc.getAvailableSeats() + count;
            doc.setAvailableSeats(newCount);
            tripSearchRepository.save(doc);
            
            log.info("Incremented available seats by {} for tripId={} (new value: {})", count, tripId, newCount);

        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            log.warn("Concurrent update detected for tripId={}: {}", tripId, e.getMessage());
            throw new RuntimeException("Concurrent modification detected, please retry", e);
        } catch (Exception e) {
            log.error("Failed to increment available seats for tripId={}", tripId, e);
            throw e;
        }
    }
}
