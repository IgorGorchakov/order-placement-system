package com.example.ebus.payment.service;

import com.example.ebus.payment.dao.PaymentDao;
import com.example.ebus.payment.dto.PaymentResponse;
import com.example.ebus.payment.entity.PaymentEntity;
import com.example.ebus.payment.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentDao paymentDao;

    @Override
    public PaymentResponse getPayment(Long id) {
        return toResponse(paymentDao.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id)));
    }

    @Override
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        return toResponse(paymentDao.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException(bookingId)));
    }

    @Override
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentDao.findByUserId(userId, pageable).map(this::toResponse);
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
