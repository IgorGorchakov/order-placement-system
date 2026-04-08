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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCommandServiceImplTest {

    @Mock
    private BookingDao bookingDao;

    @Mock
    private TripDao tripDao;

    @Mock
    private SeatLockService seatLockService;

    @Mock
    private BookingEventPublisher eventPublisher;

    @InjectMocks
    private BookingCommandServiceImpl bookingCommandService;

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
    void createBooking_Success() {
        when(tripDao.findById(1L)).thenReturn(Optional.of(sampleTrip));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        doNothing().when(eventPublisher).publishBookingCreatedEvent(anyLong(), any(BookingCreatedEvent.class));

        BookingResponse response = bookingCommandService.createBooking(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(100L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.seatNumbers()).containsExactly("1A", "1B");

        verify(seatLockService).lockSeats(1L, Arrays.asList("1A", "1B"));
        verify(eventPublisher).publishBookingCreatedEvent(eq(1L), any(BookingCreatedEvent.class));
    }

    @Test
    void createBooking_TripNotFound() {
        when(tripDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingCommandService.createBooking(createRequest))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("1");

        verify(seatLockService, never()).lockSeats(any(), any());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void cancelBooking_Success() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingDao.save(any(BookingEntity.class))).thenReturn(sampleBooking);
        doNothing().when(eventPublisher).publishBookingCancelledEvent(anyLong(), any(BookingCancelledEvent.class));

        BookingResponse response = bookingCommandService.cancelBooking(1L);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("CANCELLED");

        verify(seatLockService).releaseSeats(1L, Arrays.asList("1A", "1B"));
        verify(eventPublisher).publishBookingCancelledEvent(eq(1L), any(BookingCancelledEvent.class));
    }

    @Test
    void cancelBooking_NotFound() {
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingCommandService.cancelBooking(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("1");

        verify(seatLockService, never()).releaseSeats(any(), any());
    }
}
