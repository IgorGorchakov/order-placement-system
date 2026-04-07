package com.example.ebus.fulfillment.service;

import com.example.ebus.fulfillment.dao.NotificationDao;
import com.example.ebus.fulfillment.dao.TicketDao;
import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.dto.TicketResponse;
import com.example.ebus.fulfillment.entity.NotificationEntity;
import com.example.ebus.fulfillment.entity.TicketEntity;
import com.example.ebus.fulfillment.entity.TicketStatus;
import com.example.ebus.fulfillment.exception.TicketNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketDao ticketDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketService ticketService;

    private TicketEntity sampleTicket;

    @BeforeEach
    void setUp() {
        sampleTicket = new TicketEntity();
        sampleTicket.setId(1L);
        sampleTicket.setBookingId(100L);
        sampleTicket.setUserId(200L);
        sampleTicket.setTripId(300L);
        sampleTicket.setTicketCode("TKT-ABC12345");
        sampleTicket.setStatus(TicketStatus.ISSUED);
    }

    @Test
    void issueTicket_Success() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.empty());
        when(ticketDao.save(any(TicketEntity.class))).thenReturn(sampleTicket);
        doNothing().when(notificationService).sendTicketIssuedNotification(anyLong(), anyLong(), anyString());

        ticketService.issueTicket(100L, 200L, 300L);

        verify(ticketDao).save(argThat(ticket ->
                ticket.getBookingId().equals(100L) &&
                ticket.getUserId().equals(200L) &&
                ticket.getTicketCode().startsWith("TKT-")
        ));
        verify(notificationService).sendTicketIssuedNotification(eq(200L), eq(100L), anyString());
    }

    @Test
    void issueTicket_AlreadyExists() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.of(sampleTicket));

        ticketService.issueTicket(100L, 200L, 300L);

        verify(ticketDao, never()).save(any());
        verify(notificationService, never()).sendTicketIssuedNotification(anyLong(), anyLong(), anyString());
    }

    @Test
    void cancelTicket_Success() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.of(sampleTicket));
        doNothing().when(notificationService).sendBookingCancelledNotification(anyLong(), anyLong());

        ticketService.cancelTicket(100L, 200L);

        verify(ticketDao).save(argThat(ticket ->
                ticket.getStatus() == TicketStatus.CANCELLED &&
                ticket.getCancelledAt() != null
        ));
        verify(notificationService).sendBookingCancelledNotification(200L, 100L);
    }

    @Test
    void cancelTicket_TicketNotFound() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.empty());
        doNothing().when(notificationService).sendBookingCancelledNotification(anyLong(), anyLong());

        ticketService.cancelTicket(100L, 200L);

        verify(ticketDao, never()).save(any());
        verify(notificationService).sendBookingCancelledNotification(200L, 100L);
    }

    @Test
    void cancelTicket_AlreadyCancelled() {
        sampleTicket.setStatus(TicketStatus.CANCELLED);
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.of(sampleTicket));
        doNothing().when(notificationService).sendBookingCancelledNotification(anyLong(), anyLong());

        ticketService.cancelTicket(100L, 200L);

        verify(ticketDao, never()).save(any());
    }

    @Test
    void getTicket_Success() {
        when(ticketDao.findById(1L)).thenReturn(Optional.of(sampleTicket));

        TicketResponse response = ticketService.getTicket(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.ticketCode()).isEqualTo("TKT-ABC12345");
    }

    @Test
    void getTicket_NotFound() {
        when(ticketDao.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicket(1L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void getTicketByBookingId_Success() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.of(sampleTicket));

        TicketResponse response = ticketService.getTicketByBookingId(100L);

        assertThat(response).isNotNull();
        assertThat(response.bookingId()).isEqualTo(100L);
    }

    @Test
    void getTicketByBookingId_NotFound() {
        when(ticketDao.findByBookingId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketByBookingId(100L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("100");
    }

    @Test
    void getTicketsByUser_Success() {
        when(ticketDao.findByUserId(200L)).thenReturn(List.of(sampleTicket));

        List<TicketResponse> responses = ticketService.getTicketsByUser(200L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(200L);
    }

    @Test
    void getTicketsByUser_EmptyList() {
        when(ticketDao.findByUserId(200L)).thenReturn(List.of());

        List<TicketResponse> responses = ticketService.getTicketsByUser(200L);

        assertThat(responses).isEmpty();
    }
}
