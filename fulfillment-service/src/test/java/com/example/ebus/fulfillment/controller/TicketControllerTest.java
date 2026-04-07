package com.example.ebus.fulfillment.controller;

import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.dto.TicketResponse;
import com.example.ebus.fulfillment.exception.TicketNotFoundException;
import com.example.ebus.fulfillment.service.NotificationService;
import com.example.ebus.fulfillment.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketController ticketController;

    @Test
    void getTicket_Success() {
        TicketResponse response = new TicketResponse(
                1L, 100L, 200L, 300L, "TKT-ABC12345", "ISSUED",
                LocalDateTime.now(), null);

        when(ticketService.getTicket(1L)).thenReturn(response);

        TicketResponse result = ticketController.getTicket(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.ticketCode()).isEqualTo("TKT-ABC12345");
    }

    @Test
    void getTicket_NotFound() {
        when(ticketService.getTicket(1L)).thenThrow(new TicketNotFoundException(1L));

        assertThatThrownBy(() -> ticketController.getTicket(1L))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void getTicketByBookingId_Success() {
        TicketResponse response = new TicketResponse(
                1L, 100L, 200L, 300L, "TKT-ABC12345", "ISSUED",
                LocalDateTime.now(), null);

        when(ticketService.getTicketByBookingId(100L)).thenReturn(response);

        TicketResponse result = ticketController.getTicketByBookingId(100L);

        assertThat(result).isNotNull();
        assertThat(result.bookingId()).isEqualTo(100);
    }

    @Test
    void getTicketByBookingId_NotFound() {
        when(ticketService.getTicketByBookingId(100L)).thenThrow(new TicketNotFoundException(100L));

        assertThatThrownBy(() -> ticketController.getTicketByBookingId(100L))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void getTicketsByUser_Success() {
        TicketResponse response = new TicketResponse(
                1L, 100L, 200L, 300L, "TKT-ABC12345", "ISSUED",
                LocalDateTime.now(), null);

        Page<TicketResponse> page = new PageImpl<>(List.of(response));
        when(ticketService.getTicketsByUser(any(Long.class), any(PageRequest.class))).thenReturn(page);

        Page<TicketResponse> responses = ticketController.getTicketsByUser(200L, 0, 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).userId()).isEqualTo(200);
    }

    @Test
    void getTicketsByUser_EmptyList() {
        Page<TicketResponse> page = new PageImpl<>(List.of());
        when(ticketService.getTicketsByUser(any(Long.class), any(PageRequest.class))).thenReturn(page);

        Page<TicketResponse> responses = ticketController.getTicketsByUser(200L, 0, 20);

        assertThat(responses).isEmpty();
    }

    @Test
    void getNotificationsByUser_Success() {
        NotificationResponse response = new NotificationResponse(
                1L, 200L, 100L, "TICKET_ISSUED", "EMAIL",
                "john@example.com", "Your ticket has been issued", LocalDateTime.now());

        Page<NotificationResponse> page = new PageImpl<>(List.of(response));
        when(notificationService.getNotificationsByUser(any(Long.class), any(PageRequest.class))).thenReturn(page);

        Page<NotificationResponse> responses = ticketController.getNotificationsByUser(200L, 0, 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).userId()).isEqualTo(200);
        assertThat(responses.getContent().get(0).type()).isEqualTo("TICKET_ISSUED");
    }

    @Test
    void getNotificationsByBooking_Success() {
        NotificationResponse response = new NotificationResponse(
                1L, 200L, 100L, "TICKET_ISSUED", "EMAIL",
                "john@example.com", "Your ticket has been issued", LocalDateTime.now());

        Page<NotificationResponse> page = new PageImpl<>(List.of(response));
        when(notificationService.getNotificationsByBooking(any(Long.class), any(PageRequest.class))).thenReturn(page);

        Page<NotificationResponse> responses = ticketController.getNotificationsByBooking(100L, 0, 20);

        assertThat(responses).hasSize(1);
        assertThat(responses.getContent().get(0).bookingId()).isEqualTo(100);
    }
}
