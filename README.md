# 🚕 Ride Booking — Booking Service

Core business service responsible for creating bookings, managing ride status and safely assigning drivers.

The Booking Service coordinates with Auth Service, Location Service and Socket Service to implement the main ride-booking workflow.

---

## 🎯 Responsibilities

* Create bookings
* Update booking status
* Retrieve passenger/driver information
* Request nearby drivers
* Send ride requests to Socket Service
* Assign drivers
* Manage booking lifecycle
* Perform concurrency-safe driver assignment
* Validate authenticated requests

---

## 🛠️ Tech Stack

| Technology                        | Purpose                |
| --------------------------------- | ---------------------- |
| Java                              | Programming language   |
| Spring Boot                       | Backend framework      |
| Spring Data JPA                   | Database access        |
| Hibernate                         | ORM                    |
| MySQL                             | Booking database       |
| Spring Security / JWT integration | Request authentication |
| Retrofit / RestTemplate           | Service communication  |
| Gradle                            | Build tool             |

---

## 🏗️ Architecture

```text
                         Client
                           │
                           │ Create Booking
                           ▼
                  ┌──────────────────┐
                  │ Booking Service  │
                  │                  │
                  │ Controller       │
                  │      │           │
                  │      ▼           │
                  │ Booking Service  │
                  │      │           │
                  └──────┼───────────┘
                         │
            ┌────────────┼────────────┐
            │            │            │
            ▼            ▼            ▼
          Auth       Location       Socket
        Service       Service       Service
            │            │            │
            │            ▼            │
            │          Redis          │
            │                         │
            └────────────┬────────────┘
                         ▼
                    Booking DB
```

---

# 🔄 Booking Creation Flow

```text
Passenger
   │
   │ POST /api/v1/booking
   │ Authorization: Bearer JWT
   ▼
Booking Service
   │
   │ Validate Token
   ▼
Auth Service
   │
   │ Valid
   ▼
Create Booking
   │
   │ ASSIGNING_DRIVER
   ▼
Location Service
   │
   ▼
Nearby Driver IDs
   │
   ▼
Socket Service
   │
   ▼
Nearby Drivers
```

---

# 📊 Booking Status

The booking lifecycle includes states such as:

```text
CREATED
   │
   ▼
ASSIGNING_DRIVER
   │
   │ Driver accepts
   ▼
SCHEDULED
   │
   ▼
ONGOING
   │
   ▼
COMPLETED
```

---

# 📍 Nearby Driver Discovery

Booking Service delegates location operations to Location Service.

```text
Booking Service
      │
      │ Passenger coordinates
      ▼
Location Service
      │
      ▼
Redis GEO
      │
      ▼
Nearby Driver IDs
      │
      ▼
Booking Service
```

Booking Service does not directly access the Location Service's Redis data.

---

# 📡 Ride Request Dispatch

After receiving nearby driver IDs, Booking Service sends a ride request to Socket Service.

```text
Booking Service
       │
       │ RideRequestDto
       ▼
Socket Service
       │
       ├──► Driver 1
       ├──► Driver 2
       └──► Driver 3
```

The Socket Service handles the real-time delivery.

---

# 🔒 Atomic Driver Assignment

The most important concurrency problem solved in Booking Service is preventing multiple drivers from being assigned to the same booking.

Suppose several drivers accept the same booking simultaneously:

```text
Driver 1 ─────┐
Driver 2 ─────┼──► Booking #22
Driver 3 ─────┘
```

A normal application-level check can suffer from a race condition.

Therefore, the Booking Service uses a conditional database update.

Conceptually:

```sql
UPDATE booking
SET booking_status = 'SCHEDULED',
    driver_id = ?
WHERE id = ?
  AND booking_status = 'ASSIGNING_DRIVER'
  AND driver_id IS NULL;
```

The affected-row count determines the result.

```text
1 row updated
     │
     ▼
Assignment successful

0 rows updated
     │
     ▼
Booking already assigned
or unavailable
```

Example:

```text
Driver 1 → SUCCESS
Driver 2 → REJECTED
Driver 3 → REJECTED
```

The database becomes the final authority for the assignment operation.

---

# 🧵 Concurrency Scenario

```text
                  Booking #22
                       │
               ASSIGNING_DRIVER
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
      Driver 1      Driver 2     Driver 3
          │            │            │
          └────────────┼────────────┘
                       │
                Atomic UPDATE
                       │
                ┌──────┴──────┐
                ▼             ▼
              Winner       Rejected
                │
                ▼
            SCHEDULED
```

This prevents duplicate driver assignment under concurrent requests.

---

# 🔗 Service Communication

Booking Service communicates with:

### Auth Service

Used to validate authentication tokens.

### Location Service

Used to discover nearby drivers.

### Socket Service

Used to send real-time ride requests.

```text
                Booking Service
                 /      |      \
                /       |       \
               ▼        ▼        ▼
            Auth    Location   Socket
```

---

# 🌐 Main APIs

### Create Booking

```http
POST /api/v1/booking
Authorization: Bearer <JWT>
```

### Update Booking

```http
PATCH /api/v1/booking/{bookingId}
Authorization: Bearer <JWT>
```

### Internal Booking Update

```http
PATCH /api/v1/booking/internal/{bookingId}
```

The internal endpoint is used by trusted service-to-service communication for operations such as driver assignment.

---

# 🧪 Testing

The Booking Service has been tested using:

* Postman
* Multiple driver WebSocket sessions
* MySQL verification
* Redis-based nearby-driver discovery
* Concurrent driver acceptance

The concurrency scenario was intentionally tested by allowing multiple drivers to attempt accepting the same booking.

---

# 🧠 Key Engineering Decisions

* Booking owns booking state.
* Location operations are delegated to Location Service.
* Real-time communication is delegated to Socket Service.
* Authentication is handled by Auth Service.
* Database-level atomicity protects driver assignment.
* Booking data is isolated in its own database.

---

# 🔮 Future Improvements

* Ride cancellation
* Automatic cancellation of outstanding driver requests
* Payment Service
* Kafka/event-driven ride events
* Outbox pattern
* Idempotency keys
* API Gateway
* Distributed tracing
* Automated integration testing

---

## 🔗 Related Services

* [Auth Service](https://github.com/adarsh25tiwari/Ride-Booking-AuthService)
* [Location Service](https://github.com/adarsh25tiwari/Ride-Booking-LocationService)
* [Socket Service](https://github.com/adarsh25tiwari/Ride-Booking-SocketService)
* [Review Service](https://github.com/adarsh25tiwari/Ride-Booking-ReviewService)
* [Service Discovery](https://github.com/adarsh25tiwari/Ride-Booking-ServiceDiscovery)
* [Entity Library](https://github.com/adarsh25tiwari/Ride-Booking-EntityService)
