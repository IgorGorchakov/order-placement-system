package com.example.ebus.payment.controller;

import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @GetMapping("/booking/{bookingId}")
    public PaymentResponse getPaymentByBookingId(@PathVariable Long bookingId) {
        return paymentService.getPaymentByBookingId(bookingId);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getPaymentsByUserId(@PathVariable Long userId) {
        return paymentService.getPaymentsByUserId(userId);
    }
}
