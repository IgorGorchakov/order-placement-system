package com.example.ebus.payment.controller;

import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.service.PaymentQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public Page<PaymentResponse> getPaymentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paymentQueryService.getPaymentsByUserId(
                userId,
                PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending()));
    }
}
