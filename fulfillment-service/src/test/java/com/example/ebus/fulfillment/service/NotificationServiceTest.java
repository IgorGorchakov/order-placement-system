package com.example.ebus.fulfillment.service;

import com.example.ebus.fulfillment.client.UserInfo;
import com.example.ebus.fulfillment.client.UserServiceClient;
import com.example.ebus.fulfillment.dao.NotificationDao;
import com.example.ebus.fulfillment.dto.NotificationResponse;
import com.example.ebus.fulfillment.entity.NotificationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private NotificationService notificationService;

    private UserInfo sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new UserInfo(200L, "john@example.com", "John", "Doe", "1234567890");
    }

    @Test
    void sendTicketIssuedNotification_Success() {
        when(userServiceClient.getUser(200L)).thenReturn(sampleUser);
        when(notificationDao.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity notification = invocation.getArgument(0);
            notification.setId(1L);
            return notification;
        });

        notificationService.sendTicketIssuedNotification(200L, 100L, "TKT-ABC12345");

        verify(notificationDao).save(argThat(notification ->
                notification.getUserId().equals(200L) &&
                notification.getBookingId().equals(100L) &&
                notification.getType().equals("TICKET_ISSUED") &&
                notification.getRecipient().equals("john@example.com")
        ));
    }

    @Test
    void sendBookingCancelledNotification_Success() {
        when(userServiceClient.getUser(200L)).thenReturn(sampleUser);
        when(notificationDao.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity notification = invocation.getArgument(0);
            notification.setId(2L);
            return notification;
        });

        notificationService.sendBookingCancelledNotification(200L, 100L);

        verify(notificationDao).save(argThat(notification ->
                notification.getUserId().equals(200L) &&
                notification.getBookingId().equals(100L) &&
                notification.getType().equals("BOOKING_CANCELLED") &&
                notification.getRecipient().equals("john@example.com")
        ));
    }

    @Test
    void getNotificationsByUser_Success() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(1L);
        notification.setUserId(200L);
        notification.setBookingId(100L);
        notification.setType("TICKET_ISSUED");
        notification.setChannel("EMAIL");
        notification.setRecipient("john@example.com");
        notification.setMessage("Test message");

        when(notificationDao.findByUserId(200L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByUser(200L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(200L);
        assertThat(responses.get(0).type()).isEqualTo("TICKET_ISSUED");
    }

    @Test
    void getNotificationsByUser_EmptyList() {
        when(notificationDao.findByUserId(200L)).thenReturn(List.of());

        List<NotificationResponse> responses = notificationService.getNotificationsByUser(200L);

        assertThat(responses).isEmpty();
    }

    @Test
    void getNotificationsByBooking_Success() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(1L);
        notification.setUserId(200L);
        notification.setBookingId(100L);
        notification.setType("TICKET_ISSUED");
        notification.setChannel("EMAIL");
        notification.setRecipient("john@example.com");
        notification.setMessage("Test message");

        when(notificationDao.findByBookingId(100L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotificationsByBooking(100L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).bookingId()).isEqualTo(100L);
    }

    @Test
    void getNotificationsByBooking_EmptyList() {
        when(notificationDao.findByBookingId(100L)).thenReturn(List.of());

        List<NotificationResponse> responses = notificationService.getNotificationsByBooking(100L);

        assertThat(responses).isEmpty();
    }
}
