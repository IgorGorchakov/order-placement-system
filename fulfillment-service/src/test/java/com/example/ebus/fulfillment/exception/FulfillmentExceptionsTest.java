package com.example.ebus.fulfillment.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FulfillmentExceptionsTest {

    @Test
    void ticketNotFoundException_HasCorrectMessage() {
        TicketNotFoundException ex = new TicketNotFoundException(42L);

        assertThat(ex).hasMessage("Ticket not found: 42");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void globalExceptionHandler_HandlesTicketNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        TicketNotFoundException ex = new TicketNotFoundException(5L);

        ResponseEntity<Map<String, String>> response = handler.handleTicketNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Ticket not found: 5");
    }
}
