package com.example.ebus.fulfillment.controller;

import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.dto.TicketResponse;
import com.example.ebus.fulfillment.service.NotificationService;
import com.example.ebus.fulfillment.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;
    private final NotificationService notificationService;

    public TicketController(TicketService ticketService, NotificationService notificationService) {
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    @GetMapping("/tickets/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return ticketService.getTicket(id);
    }

    @GetMapping("/tickets/booking/{bookingId}")
    public TicketResponse getTicketByBookingId(@PathVariable Long bookingId) {
        return ticketService.getTicketByBookingId(bookingId);
    }

    @GetMapping("/tickets/user/{userId}")
    public Page<TicketResponse> getTicketsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ticketService.getTicketsByUser(
                userId,
                PageRequest.of(page, Math.min(size, 100), Sort.by("issuedAt").descending()));
    }

    @GetMapping("/notifications/user/{userId}")
    public Page<NotificationResponse> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getNotificationsByUser(
                userId,
                PageRequest.of(page, Math.min(size, 100), Sort.by("sentAt").descending()));
    }

    @GetMapping("/notifications/booking/{bookingId}")
    public Page<NotificationResponse> getNotificationsByBooking(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getNotificationsByBooking(
                bookingId,
                PageRequest.of(page, Math.min(size, 100), Sort.by("sentAt").descending()));
    }
}
