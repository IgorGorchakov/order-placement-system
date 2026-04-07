package com.example.ebus.search.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

        ResponseEntity<Map<String, String>> response = handler.handleTripNotFound(ex);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Trip not found with id: trip-5");
    }
}
