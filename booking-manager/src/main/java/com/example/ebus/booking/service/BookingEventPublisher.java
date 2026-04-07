package com.example.ebus.booking.service;

public interface BookingEventPublisher {

    void publishBookingCreatedEvent(Long bookingId, Object event);

    void publishBookingCancelledEvent(Long bookingId, Object event);
}
