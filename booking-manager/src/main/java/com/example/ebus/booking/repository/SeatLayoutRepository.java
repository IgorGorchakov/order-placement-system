package com.example.ebus.booking.repository;

import com.example.ebus.booking.document.SeatLayoutDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SeatLayoutRepository extends MongoRepository<SeatLayoutDocument, String> {
    Optional<SeatLayoutDocument> findByBusId(Long busId);
}
