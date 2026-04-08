package com.example.ebus.search.exception;

import com.example.ebus.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchExceptionsTest {

    @Test
    void tripNotFoundException_HasCorrectMessage() {
        TripNotFoundException ex = new TripNotFoundException("trip-42");

        assertThat(ex).hasMessage("Trip not found with id: trip-42");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void globalExceptionHandler_HandlesTripNotFoundException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        TripNotFoundException ex = new TripNotFoundException("trip-5");
        HttpServletRequest request = mockRequest();

        ResponseEntity<ErrorResponse> response = handler.handleTripNotFound(ex, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).satisfies(body -> {
            assertThat(body).isNotNull();
            assertThat(body.errorCode()).isEqualTo("TRIP_NOT_FOUND");
            assertThat(body.message()).isEqualTo("Trip not found with id: trip-5");
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
        when(request.getRequestURI()).thenReturn("/api/trips");
        return request;
    }
}
