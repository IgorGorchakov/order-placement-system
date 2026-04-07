package com.example.ebus.payment.service;

import com.example.ebus.events.booking.BookingCreatedEvent;

public interface PaymentProcessor {

    void processBookingCreated(BookingCreatedEvent event);
}
