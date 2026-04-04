package com.example.ebus.payment.service;

import com.example.ebus.events.Topics;
import com.example.ebus.events.booking.BookingCreatedEvent;
import com.example.ebus.events.payment.PaymentCompletedEvent;
import com.example.ebus.events.payment.PaymentFailedEvent;
import com.example.ebus.payment.client.UserPaymentMethod;
import com.example.ebus.payment.client.UserServiceClient;
import com.example.ebus.payment.dao.PaymentDao;
import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.entity.PaymentEntity;
import com.example.ebus.payment.entity.PaymentStatus;
import com.example.ebus.payment.exception.PaymentNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentDao paymentDao;
    private final UserServiceClient userServiceClient;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentDao paymentDao, UserServiceClient userServiceClient,
                          OutboxService outboxService, ObjectMapper objectMapper) {
        this.paymentDao = paymentDao;
        this.userServiceClient = userServiceClient;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processBookingCreated(BookingCreatedEvent event) {
        try {
            List<UserPaymentMethod> methods = userServiceClient.getPaymentMethods(event.userId());
            UserPaymentMethod method = methods.stream()
                    .filter(UserPaymentMethod::defaultMethod)
                    .findFirst()
                    .orElse(methods.isEmpty() ? null : methods.get(0));

            if (method == null) {
                createFailedPayment(event, "NO_PAYMENT_METHOD", "unknown", "No payment method found for user");
                return;
            }

            // Simulate payment processing — in production this would call a payment gateway
            boolean success = chargePaymentMethod(method, event);

            PaymentEntity payment = new PaymentEntity();
            payment.setBookingId(event.bookingId());
            payment.setUserId(event.userId());
            payment.setAmount(event.totalPrice());
            payment.setCurrency(event.currency());
            payment.setPaymentMethodType(method.type());
            payment.setProvider(method.provider());

            if (success) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment = paymentDao.save(payment);

                PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                        payment.getId(), event.bookingId(), event.totalPrice(), event.currency());
                outboxService.saveEvent("Payment", payment.getId().toString(),
                        Topics.PAYMENT_COMPLETED, objectMapper.writeValueAsString(completedEvent));
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment declined by provider");
                payment = paymentDao.save(payment);

                PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                        payment.getId(), event.bookingId(), "Payment declined by provider");
                outboxService.saveEvent("Payment", payment.getId().toString(),
                        Topics.PAYMENT_FAILED, objectMapper.writeValueAsString(failedEvent));
            }
        } catch (Exception e) {
            log.error("Failed to process booking-created event for bookingId={}", event.bookingId(), e);
            createFailedPayment(event, "UNKNOWN", "unknown", e.getMessage());
        }
    }

    public PaymentResponse getPayment(Long id) {
        return toResponse(paymentDao.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id)));
    }

    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        return toResponse(paymentDao.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException(bookingId)));
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentDao.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private boolean chargePaymentMethod(UserPaymentMethod method, BookingCreatedEvent event) {
        // Simulated charge — always succeeds for now
        log.info("Charging {} {} via {} (provider={}) for bookingId={}",
                event.totalPrice(), event.currency(), method.type(), method.provider(), event.bookingId());
        return true;
    }

    private void createFailedPayment(BookingCreatedEvent event, String methodType, String provider, String reason) {
        try {
            PaymentEntity payment = new PaymentEntity();
            payment.setBookingId(event.bookingId());
            payment.setUserId(event.userId());
            payment.setAmount(event.totalPrice());
            payment.setCurrency(event.currency());
            payment.setPaymentMethodType(methodType);
            payment.setProvider(provider);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(reason);
            payment = paymentDao.save(payment);

            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    payment.getId(), event.bookingId(), reason);
            outboxService.saveEvent("Payment", payment.getId().toString(),
                    Topics.PAYMENT_FAILED, objectMapper.writeValueAsString(failedEvent));
        } catch (Exception e) {
            log.error("Failed to persist failed payment for bookingId={}", event.bookingId(), e);
        }
    }

    private PaymentResponse toResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.getId(), entity.getBookingId(), entity.getUserId(),
                entity.getAmount(), entity.getCurrency(),
                entity.getPaymentMethodType(), entity.getProvider(),
                entity.getStatus().name(), entity.getFailureReason(),
                entity.getCreatedAt());
    }
}
