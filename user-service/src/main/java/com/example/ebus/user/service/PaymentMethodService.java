package com.example.ebus.user.service;

import com.example.ebus.user.dto.PaymentMethodRequest;
import com.example.ebus.user.dto.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {

    PaymentMethodResponse addPaymentMethod(Long userId, PaymentMethodRequest request);

    List<PaymentMethodResponse> getPaymentMethods(Long userId);
}
