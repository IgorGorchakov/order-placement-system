package com.example.ebus.fulfillment.service;

import com.example.ebus.fulfillment.dao.TicketDao;
import com.example.ebus.fulfillment.dto.TicketResponse;
import com.example.ebus.fulfillment.entity.TicketEntity;
import com.example.ebus.fulfillment.entity.TicketStatus;
import com.example.ebus.fulfillment.exception.TicketNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketDao ticketDao;
    private final NotificationService notificationService;

    public TicketService(TicketDao ticketDao, NotificationService notificationService) {
        this.ticketDao = ticketDao;
        this.notificationService = notificationService;
    }

    @Transactional
    public void issueTicket(Long bookingId, Long userId, Long tripId) {
        if (ticketDao.findByBookingId(bookingId).isPresent()) {
            log.warn("Ticket already issued for bookingId={}", bookingId);
            return;
        }

        String ticketCode = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        TicketEntity ticket = new TicketEntity();
        ticket.setBookingId(bookingId);
        ticket.setUserId(userId);
        ticket.setTripId(tripId);
        ticket.setTicketCode(ticketCode);
        ticket.setStatus(TicketStatus.ISSUED);
        ticketDao.save(ticket);

        log.info("Issued ticket {} for bookingId={}", ticketCode, bookingId);

        notificationService.sendTicketIssuedNotification(userId, bookingId, ticketCode);
    }

    @Transactional
    public void cancelTicket(Long bookingId, Long userId) {
        TicketEntity ticket = ticketDao.findByBookingId(bookingId).orElse(null);
        if (ticket != null && ticket.getStatus() == TicketStatus.ISSUED) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.setCancelledAt(LocalDateTime.now());
            ticketDao.save(ticket);
            log.info("Cancelled ticket {} for bookingId={}", ticket.getTicketCode(), bookingId);
        }

        notificationService.sendBookingCancelledNotification(userId, bookingId);
    }

    public TicketResponse getTicket(Long id) {
        return toResponse(ticketDao.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id)));
    }

    public TicketResponse getTicketByBookingId(Long bookingId) {
        return toResponse(ticketDao.findByBookingId(bookingId)
                .orElseThrow(() -> new TicketNotFoundException(bookingId)));
    }

    public List<TicketResponse> getTicketsByUser(Long userId) {
        return ticketDao.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private TicketResponse toResponse(TicketEntity entity) {
        return new TicketResponse(
                entity.getId(), entity.getBookingId(), entity.getUserId(), entity.getTripId(),
                entity.getTicketCode(), entity.getStatus().name(),
                entity.getIssuedAt(), entity.getCancelledAt());
    }
}
