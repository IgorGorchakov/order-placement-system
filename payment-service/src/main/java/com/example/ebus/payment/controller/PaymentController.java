package com.example.ebus.payment.controller;

import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.service.PaymentQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentQueryService paymentQueryService;

    public PaymentController(PaymentQueryService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentQueryService.getPayment(id);
    }

    @GetMapping("/booking/{bookingId}")
    public PaymentResponse getPaymentByBookingId(@PathVariable Long bookingId) {
        return paymentQueryService.getPaymentByBookingId(bookingId);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getPaymentsByUserId(@PathVariable Long userId) {
        return paymentQueryService.getPaymentsByUserId(userId);
    }
}
