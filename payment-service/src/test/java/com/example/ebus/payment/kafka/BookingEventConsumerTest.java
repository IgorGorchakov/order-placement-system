package com.example.ebus.payment.kafka;

import com.example.ebus.events.booking.BookingCreatedEvent;
import com.example.ebus.payment.service.PaymentProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingEventConsumerTest {

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookingEventConsumer bookingEventConsumer;

    private BookingCreatedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = new BookingCreatedEvent(
                1L, 100L, 1L, List.of("1A", "1B"), BigDecimal.valueOf(200), "USD");
    }

    @Test
    void handleBookingCreated_Success() throws Exception {
        String message = "{\"bookingId\":1,\"userId\":100,\"tripId\":1}";
        when(objectMapper.readValue(message, BookingCreatedEvent.class)).thenReturn(sampleEvent);

        bookingEventConsumer.handleBookingCreated(message);

        verify(paymentProcessor).processBookingCreated(sampleEvent);
    }

    @Test
    void handleBookingCreated_InvalidMessage() throws Exception {
        String message = "invalid json";
        when(objectMapper.readValue(message, BookingCreatedEvent.class))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // Exception should propagate (not swallowed) so Kafka can retry/send to DLT
        assertThrows(RuntimeException.class, () ->
            bookingEventConsumer.handleBookingCreated(message)
        );

        // Verify paymentProcessor was NOT called since parsing failed
        verify(paymentProcessor, never()).processBookingCreated(any(BookingCreatedEvent.class));
    }
}
