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
import com.example.ebus.events.booking.BookingCancelledEvent;
import com.example.ebus.events.booking.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingDao bookingDao;
    private final TripDao tripDao;
    private final SeatLockService seatLockService;
    private final BookingEventPublisher eventPublisher;

    @Override
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

        BookingCreatedEvent event = new BookingCreatedEvent(
                booking.getId(), booking.getUserId(), booking.getTripId(),
                request.seatNumbers(), booking.getTotalPrice(), booking.getCurrency());
        eventPublisher.publishBookingCreatedEvent(booking.getId(), event);

        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        BookingEntity booking = bookingDao.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingDao.save(booking);

        seatLockService.releaseSeats(booking.getTripId(),
                Arrays.asList(booking.getSeatNumbers().split(",")));

        BookingCancelledEvent event = new BookingCancelledEvent(
                booking.getId(), booking.getUserId(), booking.getTripId());
        eventPublisher.publishBookingCancelledEvent(booking.getId(), event);

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
