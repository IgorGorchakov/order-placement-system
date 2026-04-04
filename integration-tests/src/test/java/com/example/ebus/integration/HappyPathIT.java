package com.example.ebus.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class HappyPathIT {

    private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void fullBookingHappyPath() throws InterruptedException {
        // --- Arrange: populate reference data via REST API ---

        String email = "happy-path-" + System.currentTimeMillis() + "@test.com";
        String password = "SecurePass123!";

        Long userId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", password,
                        "firstName", "Happy",
                        "lastName", "Path",
                        "phone", "+1234567890"
                ))
                .post("/api/users")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "type", "CARD",
                        "provider", "visa",
                        "token", "tok_test_" + System.currentTimeMillis(),
                        "defaultMethod", true
                ))
                .post("/api/users/{id}/payment-methods", userId)
                .then().statusCode(201);

        Long routeId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "origin", "CityA",
                        "destination", "CityB",
                        "distanceKm", 200,
                        "estimatedDurationMinutes", 180

                ))
                .post("/api/routes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        Long busId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "plateNumber", "BUS-" + System.currentTimeMillis(),
                        "operatorName", "TestBus Co",
                        "totalSeats", 40
                ))
                .post("/api/buses")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        LocalDateTime departure = LocalDateTime.now().plusDays(7);
        LocalDateTime arrival = departure.plusHours(3);

        Long tripId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "routeId", routeId,
                        "busId", busId,
                        "departureTime", departure.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "arrivalTime", arrival.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "price", 25.00,
                        "currency", "USD",
                        "totalSeats", 40,
                        "operatorName", "TestBus Co"
                ))
                .post("/api/trips")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // --- Act: behave as a user ---

        // 1. Login
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .post("/api/auth/login")
                .then().statusCode(200)
                .body("id", equalTo(userId.intValue()));

        // 2. Search for trips
        given()
                .queryParam("origin", "CityA")
                .queryParam("destination", "CityB")
                .queryParam("date", departure.toLocalDate().toString())
                .get("/api/search/trips")
                .then().statusCode(200);

        // 3. Submit booking request
        Long bookingId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "userId", userId,
                        "tripId", tripId,
                        "seatNumbers", List.of("1A", "1B")
                ))
                .post("/api/bookings")
                .then().statusCode(201)
                .body("status", equalTo("PENDING"))
                .extract().jsonPath().getLong("id");

        // 4. Wait for async processing (payment → confirmation → ticket → notification)
        Thread.sleep(10_000);

        // --- Assert: verify the booking completed end-to-end ---

        given()
                .get("/api/bookings/{id}", bookingId)
                .then().statusCode(200)
                .body("status", equalTo("CONFIRMED"));

        given()
                .get("/api/payments/booking/{id}", bookingId)
                .then().statusCode(200)
                .body("status", equalTo("COMPLETED"));

        given()
                .get("/api/tickets/booking/{id}", bookingId)
                .then().statusCode(200)
                .body("status", equalTo("ISSUED"));

        given()
                .get("/api/notifications/booking/{id}", bookingId)
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }
}
