package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BookingDao;
import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.entity.BookingEntity;
import com.example.ebus.booking.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingDao bookingDao;

    @Override
    public BookingResponse getBooking(Long id) {
        return toResponse(bookingDao.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id)));
    }

    @Override
    public Page<BookingResponse> getBookingsByUser(Long userId, Pageable pageable) {
        return bookingDao.findByUserId(userId, pageable).map(this::toResponse);
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
