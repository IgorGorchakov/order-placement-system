package com.example.ebus.booking.dao;

import com.example.ebus.booking.entity.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TripDao extends JpaRepository<TripEntity, Long> {

    @Query("SELECT t FROM TripEntity t JOIN RouteEntity r ON t.routeId = r.id " +
           "WHERE (:origin IS NULL OR r.origin = :origin) " +
           "AND (:destination IS NULL OR r.destination = :destination) " +
           "AND (:dateFrom IS NULL OR t.departureTime >= :dateFrom) " +
           "AND (:dateTo IS NULL OR t.departureTime <= :dateTo)")
    List<TripEntity> findTrips(@Param("origin") String origin,
                               @Param("destination") String destination,
                               @Param("dateFrom") LocalDateTime dateFrom,
                               @Param("dateTo") LocalDateTime dateTo);
}
