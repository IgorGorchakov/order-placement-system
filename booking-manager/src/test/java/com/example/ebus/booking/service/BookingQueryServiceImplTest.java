package com.example.ebus.booking.service;

import com.example.ebus.booking.dao.BookingDao;
import com.example.ebus.booking.dto.BookingResponse;
import com.example.ebus.booking.entity.BookingEntity;
import com.example.ebus.booking.entity.BookingStatus;
import com.example.ebus.booking.exception.BookingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingQueryServiceImplTest {

    @Mock
    private BookingDao bookingDao;

    @InjectMocks
    private BookingQueryServiceImpl bookingQueryService;

    private BookingEntity sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = new BookingEntity();
        sampleBooking.setId(1L);
        sampleBooking.setUserId(100L);
        sampleBooking.setTripId(1L);
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setSeatNumbers("1A,1B");
        sampleBooking.setTotalPrice(BigDecimal.valueOf(200));
        sampleBooking.setCurrency("USD");
    }

    @Test
    void getBooking_Success() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(sampleBooking));

        BookingResponse response = bookingQueryService.getBooking(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(100L);
    }

    @Test
    void getBooking_NotFound() {
        when(bookingDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingQueryService.getBooking(1L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getBookingsByUser_Success() {
        when(bookingDao.findByUserId(100L)).thenReturn(List.of(sampleBooking));

        List<BookingResponse> responses = bookingQueryService.getBookingsByUser(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(100L);
    }

    @Test
    void getBookingsByUser_EmptyList() {
        when(bookingDao.findByUserId(100L)).thenReturn(List.of());

        List<BookingResponse> responses = bookingQueryService.getBookingsByUser(100L);

        assertThat(responses).isEmpty();
    }
}
