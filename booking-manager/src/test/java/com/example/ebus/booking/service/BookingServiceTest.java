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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingDao bookingDao;

    @Mock
    private TripDao tripDao;

    @Mock
    private SeatLockService seatLockService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookingService bookingService;

    private TripEntity sampleTrip;
    private BookingEntity sampleBooking;
    private CreateBookingRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleTrip = new TripEntity();
        sampleTrip.setId(1L);
        sampleTrip.setPrice(BigDecimal.valueOf(100));
        sampleTrip.setCurrency("USD");

        sampleBooking = new BookingEntity();
        sampleBooking.setId(1L);
        sampleBooking.setUserId(100L);
        sampleBooking.setTripId(1L);
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setSeatNumbers("1A,1B");
        sampleBooking.setTotalPrice(BigDecimal.valueOf(200));
        sampleBooking.setCurrency("USD");

        createRequest = new CreateBookingRequest(100L, 1L, Arrays.asList("1A", "1B"));
    }

    @Test
    void createBooking_Success() throws Exception {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        when(objectMapper.writeValueAsString(any(BookingCreatedEvent.class))).thenReturn("{}");

        BookingResponse response = bookingService.createBooking(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(100L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.seatNumbers()).containsExactly("1A", "1B");

        verify(seatLockService).lockSeats(1L, Arrays.asList("1A", "1B"));
        verify(outboxService).saveEvent(eq("Booking"), eq("1"), eq(Topics.BOOKING_CREATED), anyString());
    }

    @Test
    void createBooking_TripNotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(createRequest))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");

        verify(seatLockService, never()).lockSeats(any(), any());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void getBooking_Success() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));

        BookingResponse response = bookingService.getBooking(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(100L);
    }

    @Test
    void getBooking_NotFound() {
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getBookingsByUser_Success() {
        when(bookingDao.findByUserId(100L)).thenReturn(List.of(sampleBooking));

        List<BookingResponse> responses = bookingService.getBookingsByUser(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(100L);
    }

    @Test
    void getBookingsByUser_EmptyList() {
        when(bookingDao.findByUserId(100L)).thenReturn(List.of());

        List<BookingResponse> responses = bookingService.getBookingsByUser(100L);

        assertThat(responses).isEmpty();
    }

    @Test
    void cancelBooking_Success() throws Exception {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        when(objectMapper.writeValueAsString(any(BookingCancelledEvent.class))).thenReturn("{}");

        BookingResponse response = bookingService.cancelBooking(1L);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("CANCELLED");

        verify(seatLockService).releaseSeats(1L, Arrays.asList("1A", "1B"));
        verify(outboxService).saveEvent(eq("Booking"), eq("1"), eq(Topics.BOOKING_CANCELLED), anyString());
    }

    @Test
    void cancelBooking_NotFound() {
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("1");

        verify(seatLockService, never()).releaseSeats(any(), any());
    }
}
