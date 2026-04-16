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

/**
 * Processes incoming {@link BookingCreatedEvent}s by charging the user's payment method
 * and publishing the corresponding success or failure event via Kafka.
 *
 * <p>Duplicate-event safety is ensured through three layers:
 * <ol>
 *   <li>A <b>Redis distributed lock</b> ({@link DistributedLockService}) keyed per booking,
 *       which prevents concurrent processing of the same booking across multiple service instances.</li>
 *   <li>A double-checked <b>idempotency query</b> before and after the lock is acquired.</li>
 *   <li>A database <b>unique constraint</b> on {@code booking_id} as a last-resort guard.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessorImpl implements PaymentProcessor {

    private final PaymentDao paymentDao;
    private final UserServiceClient userServiceClient;
    private final PaymentEventPublisher eventPublisher;
    private final DistributedLockService distributedLockService;

    private static final String PAYMENT_LOCK_PREFIX = "payment-lock:booking:";

    @Override
    @Transactional
    public void processBookingCreated(BookingCreatedEvent event) {
        // First check if payment already exists (idempotency, no lock needed)
        if (paymentDao.findByBookingId(event.bookingId()).isPresent()) {
            log.warn("Payment already exists for bookingId={}, skipping duplicate event",
                event.bookingId());
            return;
        }

        // Acquire distributed lock to prevent race conditions across instances
        String lockKey = PAYMENT_LOCK_PREFIX + event.bookingId();
        String lockValue = distributedLockService.tryAcquire(lockKey);
        if (lockValue == null) {
            log.warn("Could not acquire distributed lock for bookingId={}, another instance is processing",
                event.bookingId());
            return;
        }

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
            distributedLockService.release(lockKey, lockValue);
        }
    }

    private boolean chargePaymentMethod(UserPaymentMethod method, BookingCreatedEvent event) {
        log.info("Charging {} {} via {} (provider={}) for bookingId={}",
                event.totalPrice(), event.currency(), method.type(), method.provider(), event.bookingId());
        return true;
    }

    private void createFailedPayment(BookingCreatedEvent event, String methodType, String provider, String reason) {
        // Lock already held by caller — no need to re-acquire
        PaymentEntity payment = new PaymentEntity();
        payment.setBookingId(event.bookingId());
        payment.setUserId(event.userId());
        payment.setAmount(event.totalPrice());
        payment.setCurrency(event.currency());
        payment.setPaymentMethodType(methodType);
        payment.setProvider(provider);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);

        try {
            payment = paymentDao.save(payment);

            PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                    payment.getId(), event.bookingId(), reason);
            eventPublisher.publishPaymentFailedEvent(payment.getId(), failedEvent);
        } catch (Exception e) {
            log.error("Failed to persist failed payment for bookingId={}", event.bookingId(), e);
        }
    }
}
