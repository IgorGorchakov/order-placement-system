package com.example.ebus.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${app.user-service-url}") String userServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public List<UserPaymentMethod> getPaymentMethods(Long userId) {
        return restClient.get()
                .uri("/api/users/{id}/payment-methods", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
