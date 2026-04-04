<div align="center">

# 🚌 E-Bus Platform

### Event-Driven Microservices for Bus Ticket Booking

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Kafka-7.6-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.13-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)](https://www.elastic.co/)

*A production-style microservices platform demonstrating saga orchestration, transactional outbox, distributed locking, and event-driven architecture.*

---

</div>

## 📋 Table of Contents

- [✨ Features](#-features)
- [🏗️ Architecture](#️-architecture)
- [🔧 Tech Stack](#-tech-stack)
- [🛡️ Reliability Patterns](#️-reliability-patterns)
- [📦 Services](#-services)
- [🔄 Booking Flow](#-booking-flow)
- [🚀 Getting Started](#-getting-started)
- [🧪 Testing](#-testing)
- [📊 Performance & Scaling](#-performance--scaling)
- [📁 Project Structure](#-project-structure)

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 👤 **User Management** | Registration, authentication, and stored payment methods |
| 🔍 **Trip Search** | Full-text search with filtering by date, price, operator, amenities, and city autocomplete |
| 💺 **Seat Booking** | Real-time seat locking via Redis to prevent double-booking |
| 💳 **Payment Processing** | Asynchronous, idempotent payment with retry and failure compensation |
| 🎫 **Ticket Generation** | PDF e-tickets with QR codes, email and SMS notifications |
| ↩️ **Cancellation & Refund** | Compensating actions with saga pattern |

---

## 🏗️ Architecture

### Service Architecture

```
                        ┌─────────────┐
                        │  🌐 Nginx   │
                        │   Gateway   │
                        │  :8080      │
                        └──────┬──────┘
                               │
          ┌────────────┬───────┼───────┬────────────┐
          │            │       │       │            │
    ┌─────▼─────┐ ┌───▼───┐ ┌─▼─────┐ ┌▼────────┐ ┌▼───────────┐
    │ 👤 User   │ │🔍 Search│ │📋 Booking│ │💳 Payment│ │🎫 Fulfillment│
    │  :8081    │ │  :8082  │ │  :8083   │ │  :8084   │ │   :8085      │
    └─────┬─────┘ └───┬───┘ └──┬──────┘ └──┬──────┘ └──────┬──────┘
          │           │        │           │               │
          │      ┌────▼──┐    ┌▼───┐    ┌──▼──┐           │
          │      │  🔎   │    │ 📨 │    │ 📨  │           │
          │      │  ES   │    │Kafka│    │Kafka│           │
          │      └───────┘    └──┬──┘    └──┬──┘           │
          │                     │          │               │
    ┌─────▼─────┐          ┌───▼──────────▼───────────────▼──┐
    │ 🐘 Postgres│          │        Apache Kafka              │
    └───────────┘          └──────────────────────────────────┘
```

### Communication Patterns

| Interaction | Type | Reason |
|:---|:---:|:---|
| User → Search (find trips) | 🔄 Sync | User is waiting for results |
| User → Booking (create booking) | 🔄 Sync | User needs immediate PENDING confirmation |
| Booking → Payment (process payment) | 📨 Async | Decoupled, retryable |
| Payment → User Service (get pay method) | 🔄 Sync | Needs data to proceed |
| Payment → Booking (payment result) | 📨 Async | Drives booking state machine |
| Booking → Fulfillment (confirmed) | 📨 Async | Fire and forget |
| Booking → Search (seat count update) | 📨 Async | Eventually consistent |

---

## 🔧 Tech Stack

| Component | Technology | Purpose |
|:---|:---|:---|
| 🖥️ Framework | Spring Boot 3.4 / Java 17 | REST APIs, dependency injection, data access |
| 🐘 Relational DB | PostgreSQL 16 | Source of truth for users, bookings, payments |
| 🍃 Document DB | MongoDB 7 | Flexible schemas: seat layouts, fleet specs |
| ⚡ Cache / Locks | Redis 7 | Seat locking (SETNX + TTL), session store, cache |
| 📨 Event Streaming | Apache Kafka (CP 7.6) | Async inter-service communication |
| 🔎 Search Engine | Elasticsearch 8.13 | Full-text trip search with facets |
| 🌐 API Gateway | Nginx | Reverse proxy routing to all services |
| 🔨 Build | Maven 3.9, multi-stage Docker | Single build, per-service runtime images |

---

## 🛡️ Reliability Patterns

### 1. Transactional Outbox — Guaranteed Event Delivery

> **Problem:** A crash between saving to PostgreSQL and publishing to Kafka loses the event, creating "ghost" bookings that never reach payment.

**Solution:** Both the business entity and an outbox event record are persisted in a single `@Transactional` database commit. A scheduled poller reads unpublished entries and forwards them to Kafka.

```
┌─────────────────────────────────────────────────────┐
│  @Transactional                                     │
│  ┌──────────────────┐  ┌─────────────────────────┐  │
│  │ Save Booking     │  │ Save OutboxEvent         │  │  ── single DB commit
│  └──────────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                          │
                    Poller (5s)
                          │
                          ▼
                    ┌───────────┐
                    │   Kafka   │
                    └───────────┘
```

- **Outbox entity** (`OutboxEventEntity`): stores `aggregateType`, `aggregateId`, `eventType`, `payload`, `createdAt`, and `processedAt`
- **Transactional write** (`OutboxService.saveEvent()`): called inside the same `@Transactional` method — both succeed or both roll back
- **Scheduled publisher**: polls every 5s with `@Scheduled(fixedDelay = 5000)`, sends each event to Kafka, then marks as processed

Implemented in both **Booking Manager** and **Payment Service** — every cross-boundary event goes through the outbox.

### 2. Saga Pattern — Payment Failure Compensation

> **Problem:** If payment fails after a booking is created, the system must undo the booking without distributed transactions (no 2PC).

**Solution:** A choreography-based saga where each service reacts to events:

```
  ✅ Happy Path                          ❌ Failure Path
  ─────────────                          ──────────────
  Booking: PENDING                       Booking: PENDING
      │                                      │
      ▼                                      ▼
  booking-created ──→ Payment            booking-created ──→ Payment
                      charge card                            charge card FAILS
                          │                                      │
                          ▼                                      ▼
                  payment-completed               payment-failed
                          │                                      │
                          ▼                                      ▼
                  Booking: CONFIRMED              Booking: CANCELLED
                  release locks                   release locks
                          │                                      │
                          ▼                                      ▼
                  booking-confirmed                booking-cancelled
                          │                                      │
                          ▼                                      ▼
                  Fulfillment: ticket              Fulfillment: notify user
```

### 3. Idempotent Payment Processing

> **Problem:** Kafka's at-least-once delivery may deliver the same event twice. Charging a card twice is unacceptable.

**Solution:** Multiple layers of protection:

- 🔒 **Database unique constraints** — `PaymentEntity` keyed by `bookingId`, duplicate insert rejected at DB level
- 🛡️ **Status guards** — consumers check `booking.getStatus() == PENDING` before acting
- 🎫 **Ticket deduplication** — `TicketService` checks for existing ticket before creating

### 4. Seat Locking — Redis SETNX + TTL

> **Problem:** Two users selecting the same seat concurrently could both succeed.

**Solution:** `SeatLockService` uses Redis atomic operations:

- **Lock:** `SETNX seat-lock:{tripId}:{seatNumber}` with 10-minute TTL
- **Rollback:** If locking seat "1B" fails after "1A" was locked, all prior locks are deleted
- **Counter:** `trip-availability:{tripId}` is atomically decremented/incremented
- **Auto-release:** 10-minute TTL ensures abandoned checkouts don't hold seats forever

### 5. Retry & Dead-Letter Queue

- **Kafka consumer retry** — Spring Kafka retries on exception; message not committed until success
- **Outbox as implicit retry** — if Kafka is down, entries stay with `processedAt = null` and retry next poll
- **Eventual consistency** — outbox (write-side) + at-least-once delivery (read-side) = guaranteed propagation

| Pattern | Purpose |
|:---|:---|
| 📤 Transactional Outbox | Guarantees event delivery even if Kafka is temporarily down |
| 🔄 Saga + Compensation | Payment failures trigger booking cancellation, no 2PC |
| 🔑 Idempotency Keys | Prevents duplicate charges (one per booking) |
| 🔒 Redis SETNX + TTL | Seat locks auto-expire after 10 min on abandoned checkout |
| 📬 DLQ | Failed refunds are retried, then land in dead-letter queue |

---

## 📦 Services

### 👤 User Service — `:8081`

Owns identity, profiles, and stored payment methods.

- User registration and login with password encryption (Spring Security Crypto)
- Payment method storage with tokenization
- Session management via Redis

**Tech:** PostgreSQL, Redis, Kafka

### 🔍 Search Service — `:8082`

Owns trip discovery (read-only, fast).

- Full-text search over trips indexed in Elasticsearch
- Filters: date, price range, operator, amenities, departure time
- City/stop autocomplete
- Consumes Kafka events to keep the search index in sync

**Tech:** Elasticsearch, Redis (query cache), Kafka (consumer)

### 📋 Booking Manager — `:8083` *(Orchestrator)*

Owns booking lifecycle and seat inventory. The most complex service.

- Seat availability checking and locking via Redis SETNX with 10-minute TTL
- Booking state machine: `PENDING → CONFIRMED → COMPLETED → CANCELLED → REFUNDED`
- Route, schedule, fleet, and pricing management
- Reliable event publishing via Transactional Outbox pattern
- Publishes: `booking-created`, `booking-confirmed`, `booking-cancelled`

**Tech:** PostgreSQL, MongoDB (seat layouts), Redis (seat locks), Kafka (producer + consumer)

### 💳 Payment Service — `:8084`

Owns money in and money out.

- Consumes `booking-created` events to initiate payment
- Fetches user's stored payment method from User Service (sync REST call)
- Charges card with idempotency key = bookingId
- Refund processing with retry + dead-letter queue
- Publishes: `payment-completed`, `payment-failed`

**Tech:** PostgreSQL, Kafka (producer + consumer)

### 🎫 Fulfillment Service — `:8085`

Owns everything after payment succeeds.

- Consumes `booking-confirmed` to generate e-tickets (PDF + QR code)
- Sends email and SMS notifications
- Scheduled reminders ("Your bus departs in 2h")
- Ticket status tracking

**Tech:** PostgreSQL, Kafka (consumer)

### Kafka Topics

| Topic | Producer | Consumers |
|:---|:---|:---|
| `booking-created` | 📋 Booking Manager | 💳 Payment Service |
| `booking-confirmed` | 📋 Booking Manager | 🎫 Fulfillment, 🔍 Search |
| `booking-cancelled` | 📋 Booking Manager | 💳 Payment, 🎫 Fulfillment, 🔍 Search |
| `payment-completed` | 💳 Payment Service | 📋 Booking Manager |
| `payment-failed` | 💳 Payment Service | 📋 Booking Manager |

---

## 🔄 Booking Flow

```
User                Search         Booking            Payment           Fulfillment
 │
 ├─ 🔍 search ──────→ query ES
 │                   ←── trips
 │
 ├─ 💺 select seats ──────────→ lock Redis (SETNX)
 │                              create PENDING
 │                              ──→ 📨 booking-created
 │                                                     │
 │                                                consume event
 │                                                fetch payment method
 │                                       ←── call User Service API
 │                                                charge card
 │                                                     │
 │                                         ┌─── ✅ success ──┐
 │                                         │                  │
 │                              📨 payment-completed          │
 │                                         │                  │
 │                              status → CONFIRMED            │
 │                              release lock                  │
 │                              ──→ 📨 booking-confirmed      │
 │                                                            │
 │                                                    🎫 generate PDF
 │                                                    📧 send email/SMS
 │
 │                                         ┌─── ❌ failure ──┐
 │                                         │                  │
 │                              📨 payment-failed             │
 │                                         │                  │
 │                              status → CANCELLED            │
 │                              release lock                  │
 │                              ──→ 📨 booking-cancelled      │
 │                                                            │
 │                                                    📧 notify user
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|:---|:---|
| 🐳 Docker & Docker Compose | Latest |
| ☕ Java | 17+ (for integration tests) |
| 🔨 Maven | 3.9+ |

### ▶️ Start the Platform

```bash
docker-compose up -d --build
```

This builds all services in a single Maven build stage, then creates per-service runtime images. Infrastructure starts first (PostgreSQL, MongoDB, Redis, Kafka, Elasticsearch), followed by the 5 microservices, and finally the Nginx API gateway on port **8080**.

Check that all services are healthy:

```bash
docker-compose ps
```

### ⏹️ Stop the Platform

```bash
docker-compose down
```

To also remove persisted data volumes:

```bash
docker-compose down -v
```

---

## 🧪 Testing

### Integration Tests

The project includes an end-to-end integration test (`HappyPathIT`) that runs against the live Docker Compose stack via the Nginx API gateway:

1. 👤 Register a user with a payment method
2. 🛣️ Create a route, bus, and trip
3. 🔐 Login
4. 🔍 Search for trips
5. 💺 Submit a booking (seats "1A", "1B")
6. ⏳ Wait for async event processing (payment, confirmation, ticketing)
7. ✅ Verify: booking → CONFIRMED, payment → COMPLETED, ticket → ISSUED, notifications sent

#### Option 1: One-Command Script (Recommended)

The project includes a script that builds the Docker stack, waits for all services to become healthy, and runs the tests automatically:

```bash
./integration-tests/src/test/resources/run-integration-tests.sh
```

You can customize it with environment variables:

| Variable | Default | Description |
|:---|:---|:---|
| `BASE_URL` | `http://localhost:8080` | Gateway URL to test against |
| `TIMEOUT` | `300` | Max seconds to wait for services to become healthy |

```bash
BASE_URL=http://myhost:8080 TIMEOUT=600 ./integration-tests/src/test/resources/run-integration-tests.sh
```

#### Option 2: Manual Steps

Start the platform first, then run the tests separately:

```bash
# 1. Build and start
docker-compose up -d --build

# 2. Wait until all services are healthy
docker-compose ps

# 3. Run tests
cd integration-tests
mvn verify
```

Override the target URL:

```bash
mvn verify -Dbase.url=http://your-host:8080
```

---

## 📊 Performance & Scaling

### Current Throughput (Single Instance)

#### Synchronous Path — Booking Creation

| Step | Operation | Latency |
|:---|:---|---:|
| 🔒 Seat locking | 2-4 Redis SETNX calls | ~2-4 ms |
| 💾 Save booking + outbox | Single PostgreSQL transaction | ~5-10 ms |
| 📤 HTTP response | Serialize and return | ~1 ms |
| **Total** | | **~8-15 ms** |

> With default Tomcat (200 threads) and HikariCP (10 connections), the DB pool is the bottleneck: **~500-1,000 bookings/second**.

#### Asynchronous Path — Event Processing

| Step | Bottleneck | Throughput |
|:---|:---|---:|
| 📤 Outbox poller | 5s delay, sequential, individual saves | ~200 evt/s |
| 📨 Kafka consumer (Payment) | Single-threaded | ~500-1,000 evt/s |
| 📨 Kafka consumer (Fulfillment) | Single-threaded + blocking REST | ~200-500 evt/s |
| 🔄 REST call (Payment → User) | Blocking, no timeout | ~100-300 calls/s |

**Overall async throughput: ~200 events/second**

#### Capacity Summary

| Metric | Estimate |
|:---|---:|
| 📋 Booking creation (sync API) | ~500-1,000 req/s |
| 🔄 End-to-end completion (async) | ~200/s |
| 🔍 Trip search (Elasticsearch) | ~2,000-5,000 req/s |
| 🔒 Concurrent seat locking | ~500/s |

### ⚠️ Current Bottlenecks

| # | Bottleneck | Component | Impact |
|:--:|:---|:---|:---|
| 1 | Outbox poller — 5s delay, 1-by-1 | `BookingEventProducer`, `PaymentEventProducer` | Up to 5s event latency |
| 2 | Single-threaded Kafka consumers | All `@KafkaListener` methods | One message at a time |
| 3 | Sequential Redis calls | `SeatLockService.lockSeats()` | N+1 round-trips per booking |
| 4 | Blocking REST, no timeout | `UserServiceClient` | Pipeline stalls if User Service slow |
| 5 | Default connection pools | HikariCP=10, no Redis/Mongo tuning | Pool exhaustion under load |
| 6 | No FK indexes | `findByBookingId()`, `findByUserId()` | Full table scans at scale |
| 7 | No rate limiting | Gateway and services | No spike/abuse protection |

### 📈 Scaling Strategies

<details>
<summary><b>🔧 Short-Term — Configuration Changes</b></summary>

**1. Tune connection pools**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
```

**2. Add Kafka consumer concurrency**

```java
@KafkaListener(topics = Topics.BOOKING_CREATED, groupId = "payment-service", concurrency = "3")
```

**3. Reduce outbox polling** — `fixedDelay = 5000` → `fixedDelay = 500` for 10x faster propagation

**4. Add timeouts and circuit breakers** — Resilience4j to prevent cascading failures

**5. Add database indexes**

```sql
CREATE INDEX idx_booking_user_id ON bookings(user_id);
CREATE INDEX idx_payment_booking_id ON payments(booking_id);
CREATE INDEX idx_ticket_booking_id ON tickets(booking_id);
CREATE INDEX idx_notification_booking_id ON notifications(booking_id);
```

</details>

<details>
<summary><b>⚙️ Medium-Term — Code Changes</b></summary>

**6. Pipeline Redis seat locks** — Lua script for atomic multi-seat locking in 1 round-trip (~5x improvement)

```lua
for i, key in ipairs(KEYS) do
  if redis.call('EXISTS', key) == 1 then
    for j = 1, i-1 do redis.call('DEL', KEYS[j]) end
    return 0
  end
  redis.call('SET', key, 'locked', 'EX', 600)
end
return 1
```

**7. Batch outbox processing** — `saveAll()` instead of individual `save()` calls

**8. Replace polling with CDC** — Debezium streams outbox changes directly to Kafka, sub-second propagation

</details>

<details>
<summary><b>🚀 Long-Term — Horizontal Scaling</b></summary>

**9. Scale service instances**

```bash
docker-compose up -d --scale booking-manager=3 --scale payment-service=3
```

**10. Scale infrastructure**

| Component | Strategy |
|:---|:---|
| PostgreSQL | Read replicas + PgBouncer |
| Redis | Redis Cluster for sharded seat locks |
| Kafka | More partitions + brokers |
| Elasticsearch | Data nodes + index sharding |
| Nginx | Cloud LB (ALB) or Kong |

**11. Event-driven autoscaling with KEDA** — Scale pods based on Kafka consumer lag

</details>

### Projected Capacity

| Scaling Level | Bookings/sec | Notes |
|:---|---:|:---|
| 🟡 Current | ~200 | Outbox poller + single-threaded consumers |
| 🟢 Config tuning | ~1,000-2,000 | Pool tuning + concurrency + faster polling |
| 🔵 3x instances | ~3,000-5,000 | Horizontal + partitioned Kafka |
| 🟣 Full production | ~10,000-50,000 | Debezium CDC, Redis Cluster, PgBouncer, K8s autoscaling |

---

## 📁 Project Structure

```
ebus-platform/
├── 📄 pom.xml                    # Parent POM (Spring Boot 3.4, Java 17)
├── 🐳 docker-compose.yml         # Full infrastructure + services
├── 🐳 Dockerfile                 # Multi-stage: single build, 5 runtime targets
├── 📂 docker/
│   ├── 🌐 nginx.conf             # API gateway routing
│   └── 🐘 init-postgres.sql      # Creates per-service databases
├── 📂 common-events/             # Shared Kafka event classes and topic definitions
├── 📂 user-service/              # 👤 Identity, auth, payment methods
├── 📂 search-service/            # 🔍 Elasticsearch-backed trip search
├── 📂 booking-manager/           # 📋 Booking lifecycle orchestrator
├── 📂 payment-service/           # 💳 Payment processing and refunds
├── 📂 fulfillment-service/       # 🎫 Tickets, notifications, reminders
└── 📂 integration-tests/         # 🧪 REST Assured end-to-end tests
```

---

<div align="center">

Built with ☕ Java, 🐳 Docker, and 📨 Kafka

</div>
