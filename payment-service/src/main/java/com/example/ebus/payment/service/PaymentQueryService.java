package com.example.ebus.payment.service;

import com.example.ebus.payment.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentQueryService {

    PaymentResponse getPayment(Long id);

    PaymentResponse getPaymentByBookingId(Long bookingId);

    Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable);
}
