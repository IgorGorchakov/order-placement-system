package com.example.ebus.payment.service;

import com.example.ebus.events.booking.BookingCreatedEvent;
import com.example.ebus.events.payment.PaymentCompletedEvent;
import com.example.ebus.events.payment.PaymentFailedEvent;
import com.example.ebus.payment.client.UserPaymentMethod;
import com.example.ebus.payment.client.UserServiceClient;
import com.example.ebus.payment.dao.PaymentDao;
import com.example.ebus.payment.entity.PaymentEntity;
import com.example.ebus.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorImpl implements PaymentProcessor {

    private final PaymentDao paymentDao;
    private final UserServiceClient userServiceClient;
    private final PaymentEventPublisher eventPublisher;
    
    // Lock to prevent race conditions during payment creation
    private final ReentrantLock paymentLock = new ReentrantLock();

    @Override
    @Transactional
    public void processBookingCreated(BookingCreatedEvent event) {
        // First check if payment already exists (idempotency)
        if (paymentDao.findByBookingId(event.bookingId()).isPresent()) {
            log.warn("Payment already exists for bookingId={}, skipping duplicate event",
                event.bookingId());
            return;
        }

        // Acquire lock to prevent race conditions
        paymentLock.lock();
        try {
            // Double-check idempotency after acquiring lock
            if (paymentDao.findByBookingId(event.bookingId()).isPresent()) {
                log.warn("Payment already exists for bookingId={}, skipping duplicate event (after lock)",
                    event.bookingId());
                return;
            }

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
                    eventPublisher.publishPaymentCompletedEvent(payment.getId(), completedEvent);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Payment declined by provider");
                    payment = paymentDao.save(payment);

                    PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                            payment.getId(), event.bookingId(), "Payment declined by provider");
                    eventPublisher.publishPaymentFailedEvent(payment.getId(), failedEvent);
                }
            } catch (DataIntegrityViolationException ex) {
                // Another instance already created the payment (race condition)
                log.warn("Duplicate payment for bookingId={}, ignoring", event.bookingId());
            } catch (Exception e) {
                log.error("Failed to process booking-created event for bookingId={}", event.bookingId(), e);
                throw e;
            }
        } finally {
            paymentLock.unlock();
        }
    }

    private boolean chargePaymentMethod(UserPaymentMethod method, BookingCreatedEvent event) {
        log.info("Charging {} {} via {} (provider={}) for bookingId={}",
                event.totalPrice(), event.currency(), method.type(), method.provider(), event.bookingId());
        return true;
    }

    private void createFailedPayment(BookingCreatedEvent event, String methodType, String provider, String reason) {
        // Acquire lock to prevent race conditions during failed payment creation
        paymentLock.lock();
        try {
            // Double-check idempotency after acquiring lock
            if (paymentDao.findByBookingId(event.bookingId()).isPresent()) {
                log.warn("Payment already exists for bookingId={}, skipping duplicate failed payment (after lock)",
                    event.bookingId());
                return;
            }
            
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
            eventPublisher.publishPaymentFailedEvent(payment.getId(), failedEvent);
        } catch (Exception e) {
            log.error("Failed to persist failed payment for bookingId={}", event.bookingId(), e);
        } finally {
            paymentLock.unlock();
        }
    }
}
