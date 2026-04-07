package com.example.ebus.booking.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BookingExceptionsTest {

    @Test
    void bookingNotFoundException_HasCorrectMessage() {
        BookingNotFoundException ex = new BookingNotFoundException(42L);

        assertThat(ex).hasMessage("Booking not found: 42");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void tripNotFoundException_HasCorrectMessage() {
        TripNotFoundException ex = new TripNotFoundException(99L);

        assertThat(ex).hasMessage("Trip not found: 99");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void seatNotAvailableException_HasCorrectMessage() {
        SeatNotAvailableException ex = new SeatNotAvailableException("1A");

        assertThat(ex).hasMessage("Seat not available: 1A");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void globalExceptionHandler_HandlesBookingNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BookingNotFoundException ex = new BookingNotFoundException(5L);

        ResponseEntity<Map<String, String>> response = handler.handleBookingNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Booking not found: 5");
    }

    @Test
    void globalExceptionHandler_HandlesTripNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        TripNotFoundException ex = new TripNotFoundException(10L);

        ResponseEntity<Map<String, String>> response = handler.handleTripNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Trip not found: 10");
    }

    @Test
    void globalExceptionHandler_HandlesSeatNotAvailableException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        SeatNotAvailableException ex = new SeatNotAvailableException("2C");

        ResponseEntity<Map<String, String>> response = handler.handleSeatNotAvailable(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Seat not available: 2C");
    }
}
