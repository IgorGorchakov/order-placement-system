package com.example.ebus.booking.service;

import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.dto.CreateBookingRequest;

public interface BookingCommandService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse cancelBooking(Long id);
}
