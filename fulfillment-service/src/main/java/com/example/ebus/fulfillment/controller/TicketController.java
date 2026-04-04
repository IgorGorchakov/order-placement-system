package com.example.ebus.fulfillment.controller;

import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.dto.TicketResponse;
import com.example.ebus.fulfillment.service.NotificationService;
import com.example.ebus.fulfillment.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<TicketResponse> getTicketsByUser(@PathVariable Long userId) {
        return ticketService.getTicketsByUser(userId);
    }

    @GetMapping("/notifications/user/{userId}")
    public List<NotificationResponse> getNotificationsByUser(@PathVariable Long userId) {
        return notificationService.getNotificationsByUser(userId);
    }

    @GetMapping("/notifications/booking/{bookingId}")
    public List<NotificationResponse> getNotificationsByBooking(@PathVariable Long bookingId) {
        return notificationService.getNotificationsByBooking(bookingId);
    }
}
