package com.example.ebus.fulfillment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${app.user-service-url}") String userServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public UserInfo getUser(Long userId) {
        return restClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .body(UserInfo.class);
    }
}
