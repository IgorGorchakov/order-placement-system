package com.example.ebus.fulfillment.kafka;

import com.example.ebus.events.booking.BookingCancelledEvent;
import com.example.ebus.events.booking.BookingConfirmedEvent;
import com.example.ebus.fulfillment.service.TicketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingEventConsumerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private ObjectMapper objectMapper;

    private BookingEventConsumer bookingEventConsumer;

    @BeforeEach
    void setUp() {
        bookingEventConsumer = new BookingEventConsumer(ticketService, objectMapper);
    }

    @Test
    void handleBookingConfirmed_Success() throws JsonProcessingException {
        BookingConfirmedEvent event = new BookingConfirmedEvent(1L, 200L, 300L);
        lenient().when(objectMapper.readValue(anyString(), eq(BookingConfirmedEvent.class))).thenReturn(event);

        bookingEventConsumer.handleBookingConfirmed("{}");

        verify(ticketService).issueTicket(1L, 200L, 300L);
    }

    @Test
    void handleBookingCancelled_Success() throws JsonProcessingException {
        BookingCancelledEvent event = new BookingCancelledEvent(1L, 200L, 300L);
        lenient().when(objectMapper.readValue(anyString(), eq(BookingCancelledEvent.class))).thenReturn(event);

        bookingEventConsumer.handleBookingCancelled("{}");

        verify(ticketService).cancelTicket(1L, 200L);
    }
}
