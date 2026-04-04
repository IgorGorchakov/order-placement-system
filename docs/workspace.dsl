workspace "Order Placement System" "System architecture for the e-bus order placement platform" {

    model {
        user = person "User" "A customer who books bus trips"

        orderSystem = softwareSystem "Order Placement System" "Handles trip search, booking, payment and fulfillment" {
            userService = container "user-service" "Manages user accounts and authentication" "Spring Boot" {
                tags "Service"
            }
            searchService = container "search-service" "Searches available trips and seats" "Spring Boot" {
                tags "Service"
            }
            bookingManager = container "booking-manager" "Manages booking lifecycle" "Spring Boot" {
                tags "Service"
            }
            paymentService = container "payment-service" "Processes payments" "Spring Boot" {
                tags "Service"
            }
            fulfillmentService = container "fulfillment-service" "Handles ticket fulfillment" "Spring Boot" {
                tags "Service"
            }
            kafka = container "Kafka Broker" "Message broker for async event-driven communication" "Apache Kafka" {
                tags "MessageBroker"
            }
        }

        # User interactions
        user -> userService "Registers / authenticates" "HTTP :8081"
        user -> searchService "Searches trips" "HTTP :8082"
        user -> bookingManager "Creates bookings" "HTTP :8083"

        # Kafka producers
        bookingManager -> kafka "Produces: booking-created, booking-confirmed, booking-cancelled" "Kafka (Outbox pattern)"
        paymentService -> kafka "Produces: payment-completed, payment-failed" "Kafka (Outbox pattern)"

        # Kafka consumers
        kafka -> paymentService "Consumes: booking-created" "Kafka"
        kafka -> bookingManager "Consumes: payment-completed, payment-failed" "Kafka"
        kafka -> fulfillmentService "Consumes: booking-confirmed, booking-cancelled" "Kafka"
        kafka -> searchService "Consumes: booking-cancelled, trip-created, seat-availability-updated" "Kafka"
    }

    views {
        container orderSystem "SystemArchitecture" "Overview of services and Kafka event flow" {
            include *
            autoLayout lr 400 100
        }

        styles {
            element "Person" {
                shape Person
                background #08427B
                color #ffffff
            }
            element "Service" {
                shape RoundedBox
                background #438DD5
                color #ffffff
            }
            element "MessageBroker" {
                shape Cylinder
                background #F5A623
                color #ffffff
            }
            relationship "Relationship" {
                thickness 2
            }
        }
    }

}
