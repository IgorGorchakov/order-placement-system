package com.example.ebus.user.service;

import com.example.ebus.user.dao.PaymentMethodDao;
import com.example.ebus.user.dao.UserDao;
import com.example.ebus.user.dto.PaymentMethodRequest;
import com.example.ebus.user.dto.PaymentMethodResponse;
import com.example.ebus.user.entity.PaymentMethodEntity;
import com.example.ebus.user.entity.UserEntity;
import com.example.ebus.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final UserDao userDao;
    private final PaymentMethodDao paymentMethodDao;

    @Override
    @Transactional
    public PaymentMethodResponse addPaymentMethod(Long userId, PaymentMethodRequest request) {
        UserEntity user = userDao.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PaymentMethodEntity pm = PaymentMethodEntity.builder()
                .type(request.type())
                .provider(request.provider())
                .token(request.token())
                .defaultMethod(request.defaultMethod())
                .user(user)
                .build();

        return toPaymentResponse(paymentMethodDao.save(pm));
    }

    @Override
    public List<PaymentMethodResponse> getPaymentMethods(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return paymentMethodDao.findByUserId(userId).stream()
                .map(this::toPaymentResponse).toList();
    }

    private PaymentMethodResponse toPaymentResponse(PaymentMethodEntity pm) {
        return PaymentMethodResponse.builder()
                .id(pm.getId())
                .type(pm.getType())
                .provider(pm.getProvider())
                .token(pm.getToken())
                .defaultMethod(pm.isDefaultMethod())
                .build();
    }
}
