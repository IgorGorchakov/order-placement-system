package com.example.ebus.fulfillment.service;

import com.example.ebus.fulfillment.client.UserInfo;
import com.example.ebus.fulfillment.client.UserServiceClient;
import com.example.ebus.fulfillment.dao.NotificationDao;
import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.entity.NotificationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDao notificationDao;
    private final UserServiceClient userServiceClient;

    public NotificationService(NotificationDao notificationDao, UserServiceClient userServiceClient) {
        this.notificationDao = notificationDao;
        this.userServiceClient = userServiceClient;
    }

    public void sendTicketIssuedNotification(Long userId, Long bookingId, String ticketCode) {
        UserInfo user = userServiceClient.getUser(userId);

        String message = String.format(
                "Hi %s, your ticket %s for booking #%d has been issued. Have a great trip!",
                user.firstName(), ticketCode, bookingId);

        log.info("Sending notification to {} ({}): {}", user.email(), user.phone(), message);

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setType("TICKET_ISSUED");
        notification.setChannel("EMAIL");
        notification.setRecipient(user.email());
        notification.setMessage(message);
        notificationDao.save(notification);
    }

    public void sendBookingCancelledNotification(Long userId, Long bookingId) {
        UserInfo user = userServiceClient.getUser(userId);

        String message = String.format(
                "Hi %s, your booking #%d has been cancelled. If you did not request this, please contact support.",
                user.firstName(), bookingId);

        log.info("Sending cancellation notification to {} ({}): {}", user.email(), user.phone(), message);

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setType("BOOKING_CANCELLED");
        notification.setChannel("EMAIL");
        notification.setRecipient(user.email());
        notification.setMessage(message);
        notificationDao.save(notification);
    }

    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationDao.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public List<NotificationResponse> getNotificationsByBooking(Long bookingId) {
        return notificationDao.findByBookingId(bookingId).stream().map(this::toResponse).toList();
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(), entity.getUserId(), entity.getBookingId(),
                entity.getType(), entity.getChannel(), entity.getRecipient(),
                entity.getMessage(), entity.getSentAt());
    }
}
