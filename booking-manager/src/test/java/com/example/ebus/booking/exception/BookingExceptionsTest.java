package com.example.ebus.booking.exception;

import com.example.ebus.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleBookingNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("BOOKING_NOT_FOUND");
            assertThat(body.message()).isEqualTo("Booking not found: 5");
        });
    }

    @Test
    void globalExceptionHandler_HandlesTripNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        TripNotFoundException ex = new TripNotFoundException(10L);
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleTripNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("TRIP_NOT_FOUND");
            assertThat(body.message()).isEqualTo("Trip not found: 10");
        });
    }

    @Test
    void globalExceptionHandler_HandlesSeatNotAvailableException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        SeatNotAvailableException ex = new SeatNotAvailableException("2C");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleSeatNotAvailable(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("SEAT_NOT_AVAILABLE");
            assertThat(body.message()).isEqualTo("Seat not available: 2C");
        });
    }

    @Test
    void globalExceptionHandler_HandlesGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new RuntimeException("Something went wrong");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("INTERNAL_ERROR");
        });
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/bookings");
        return request;
    }
}
