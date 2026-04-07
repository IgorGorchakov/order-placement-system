package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.BookingResponse;

import java.util.List;

public interface BookingQueryService {

    BookingResponse getBooking(Long id);

    List<BookingResponse> getBookingsByUser(Long userId);
}
