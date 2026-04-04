package com.example.ebus.booking.controller;

import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.dto.CreateBookingRequest;
import com.example.ebus.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/user/{userId}")
    public List<BookingResponse> getBookingsByUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }
}
