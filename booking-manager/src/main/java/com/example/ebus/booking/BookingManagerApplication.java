package com.example.ebus.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookingManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingManagerApplication.class, args);
    }
}
