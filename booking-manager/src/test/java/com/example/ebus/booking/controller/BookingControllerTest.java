package com.example.ebus.booking.controller;

import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.dto.CreateBookingRequest;
import com.example.ebus.booking.exception.BookingNotFoundException;
import com.example.ebus.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void createBooking_Success() {
        CreateBookingRequest request = new CreateBookingRequest(100L, 1L, Arrays.asList("1A", "1B"));
        BookingResponse response = new BookingResponse(
                1L, 100L, 1L, "PENDING", Arrays.asList("1A", "1B"),
                BigDecimal.valueOf(200), "USD", LocalDateTime.now());

        when(bookingService.createBooking(any(CreateBookingRequest.class))).thenReturn(response);

        BookingResponse result = bookingController.createBooking(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.userId()).isEqualTo(100);
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    void getBooking_Success() {
        BookingResponse response = new BookingResponse(
                1L, 100L, 1L, "PENDING", Arrays.asList("1A", "1B"),
                BigDecimal.valueOf(200), "USD", LocalDateTime.now());

        when(bookingService.getBooking(1L)).thenReturn(response);

        BookingResponse result = bookingController.getBooking(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    void getBooking_NotFound() {
        when(bookingService.getBooking(1L)).thenThrow(new BookingNotFoundException(1L));

        assertThatThrownBy(() -> bookingController.getBooking(1L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getBookingsByUser_Success() {
        BookingResponse response = new BookingResponse(
                1L, 100L, 1L, "PENDING", Arrays.asList("1A"),
                BigDecimal.valueOf(100), "USD", LocalDateTime.now());

        when(bookingService.getBookingsByUser(100L)).thenReturn(List.of(response));

        List<BookingResponse> responses = bookingController.getBookingsByUser(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(100);
    }

    @Test
    void cancelBooking_Success() {
        BookingResponse response = new BookingResponse(
                1L, 100L, 1L, "CANCELLED", Arrays.asList("1A"),
                BigDecimal.valueOf(100), "USD", LocalDateTime.now());

        when(bookingService.cancelBooking(1L)).thenReturn(response);

        BookingResponse result = bookingController.cancelBooking(1L);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelBooking_NotFound() {
        when(bookingService.cancelBooking(1L)).thenThrow(new BookingNotFoundException(1L));

        assertThatThrownBy(() -> bookingController.cancelBooking(1L))
                .isInstanceOf(BookingNotFoundException.class);
    }
}
