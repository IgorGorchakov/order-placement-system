package com.example.ebus.fulfillment.client;

public record UserInfo(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phone
) {}
