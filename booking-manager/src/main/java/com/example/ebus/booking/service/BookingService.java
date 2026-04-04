package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BookingDao;
import com.example.ebus.booking.dao.TripDao;
import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.dto.CreateBookingRequest;
import com.example.ebus.booking.entity.BookingEntity;
import com.example.ebus.booking.entity.BookingStatus;
import com.example.ebus.booking.entity.TripEntity;
import com.example.ebus.booking.exception.BookingNotFoundException;
import com.example.ebus.booking.exception.TripNotFoundException;
import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.BookingCancelledEvent;
import com.example.ebus.events.booking.BookingCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final TripDao tripDao;
    private final SeatLockService seatLockService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public BookingService(BookingDao bookingDao, TripDao tripDao,
                          SeatLockService seatLockService, OutboxService outboxService,
                          ObjectMapper objectMapper) {
        this.bookingDao = bookingDao;
        this.tripDao = tripDao;
        this.seatLockService = seatLockService;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        TripEntity trip = tripDao.findById(request.tripId())
                .orElseThrow(() -> new TripNotFoundException(request.tripId()));

        seatLockService.lockSeats(request.tripId(), request.seatNumbers());

        BookingEntity booking = new BookingEntity();
        booking.setUserId(request.userId());
        booking.setTripId(request.tripId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setSeatNumbers(String.join(",", request.seatNumbers()));
        booking.setTotalPrice(trip.getPrice().multiply(java.math.BigDecimal.valueOf(request.seatNumbers().size())));
        booking.setCurrency(trip.getCurrency());
        booking = bookingDao.save(booking);

        try {
            BookingCreatedEvent event = new BookingCreatedEvent(
                    booking.getId(), booking.getUserId(), booking.getTripId(),
                    request.seatNumbers(), booking.getTotalPrice(), booking.getCurrency());
            outboxService.saveEvent("Booking", booking.getId().toString(),
                    Topics.BOOKING_CREATED, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize booking event", e);
        }

        return toResponse(booking);
    }

    public BookingResponse getBooking(Long id) {
        return toResponse(bookingDao.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id)));
    }

    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingDao.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        BookingEntity booking = bookingDao.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingDao.save(booking);

        seatLockService.releaseSeats(booking.getTripId(),
                Arrays.asList(booking.getSeatNumbers().split(",")));

        try {
            BookingCancelledEvent event = new BookingCancelledEvent(
                    booking.getId(), booking.getUserId(), booking.getTripId());
            outboxService.saveEvent("Booking", booking.getId().toString(),
                    Topics.BOOKING_CANCELLED, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize cancel event", e);
        }

        return toResponse(booking);
    }

    private BookingResponse toResponse(BookingEntity entity) {
        return new BookingResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getTripId(),
                entity.getStatus().name(),
                Arrays.asList(entity.getSeatNumbers().split(",")),
                entity.getTotalPrice(),
                entity.getCurrency(),
                entity.getCreatedAt()
        );
    }
}
