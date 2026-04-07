package com.example.ebus.search.service;

import com.example.ebus.search.document.TripDocument;

public interface TripIndexingService {

    void indexTrip(TripDocument document);

    void updateAvailableSeats(String tripId, int availableSeats);

    void incrementAvailableSeats(String tripId, int count);
}
