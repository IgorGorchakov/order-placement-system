package com.example.ebus.fulfillment.exception;

import com.example.ebus.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleTicketNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("TICKET_NOT_FOUND");
            assertThat(body.message()).isEqualTo("Ticket not found: 5");
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
        when(request.getRequestURI()).thenReturn("/api/tickets");
        return request;
    }
}
