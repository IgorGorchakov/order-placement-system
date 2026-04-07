package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingQueryService {

    BookingResponse getBooking(Long id);

    Page<BookingResponse> getBookingsByUser(Long userId, Pageable pageable);
}
