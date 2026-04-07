package com.example.ebus.payment.service;

public interface PaymentEventPublisher {

    void publishPaymentCompletedEvent(Long paymentId, Object event);

    void publishPaymentFailedEvent(Long paymentId, Object event);
}
