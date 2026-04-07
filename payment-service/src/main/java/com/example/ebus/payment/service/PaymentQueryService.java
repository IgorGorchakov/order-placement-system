package com.example.ebus.payment.service;

import com.example.ebus.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentQueryService {

    PaymentResponse getPayment(Long id);

    PaymentResponse getPaymentByBookingId(Long bookingId);

    List<PaymentResponse> getPaymentsByUserId(Long userId);
}
