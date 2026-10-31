# Low Level System Design - Movie Ticket Booking (BookMyShow style)

Java 17 + Spring Boot (core only) implementation of the movie ticket booking LLD.
There is **no web layer and no test suite**: the app is a CLI that runs the three
problem-statement scenarios in order and self-checks them.

The problem statement never asks for HTTP or a database, so the code stays inside
the requirements: in-memory state, modular OO design, a demo you can run in one
command, and the timeout bonus.

Original problem statement: [problem-statement.md](problem-statement.md)

---

## Run the demo

```bash
mvn spring-boot:run
# or
mvn package
java -jar target/lld-ticket-booking-1.0.0.jar
```

No port, no curl, no Postman. The JVM exits with code `0` only if every check passes.

Expected output:

```
==============================================================
 Movie Ticket Booking - LLD scenario demo
==============================================================

--- CASE 1 - U1 books, U2 must not see those seats ---
  ... U1 sees 20 available seats
  ... U1 booked 4 seats (booking=58ea2506)
  [PASS] U2 does not see U1's locked seats
  ... payment SUCCESS for User1
  [PASS] U2 sees 16 seats (20 total - 4 booked)
  ...
--- CASE 2 - payment fails / session cancelled, seats come back ---
  ...
--- CASE 3 - two users fight over overlapping seats ---
  ...
--- BONUS - unpaid UserBookingSession times out and releases seats ---
  ...

==============================================================
 Result: 17 passed, 0 failed
==============================================================
```

Each case builds a fresh movie/theatre/screen/show, so cases cannot pollute each
other. `[PASS]/[FAIL]` lines are the demo's own assertions - there is no JUnit.

---

## Architecture

```
com.uditagarwal
├── TicketBookingApplication     main(): boots Spring, runs ScenarioDemo, exits
├── application/
│   └── TicketBookingFacade      use-case boundary (id resolution + orchestration)
├── config/                      BookingProperties, PaymentProperties
├── model/                       Movie, Theatre, Screen, Seat, Show, Booking, SeatLock, Payment
├── repository/                  interfaces + in-memory ConcurrentHashMap implementations
├── services/                    booking, payment, availability, show, theatre, expiry
├── providers/                   SeatLockProvider + in-memory lock store
└── demo/
    └── ScenarioDemo             Case 1 / 2 / 3 / timeout bonus, sequential
```

Dependency direction: `demo -> application -> services -> repository/model`,
and `services -> providers`. Models know nothing about Spring.

`TicketBookingFacade` replaces the old REST controllers. It is a plain `@Component`
that resolves ids and calls services; the demo talks to it instead of juggling six
services. If an HTTP layer is ever needed, it can wrap this facade without touching
the domain.

### Key design decisions

- **Booking is a state machine.** `CREATED -> CONFIRMED | EXPIRED`; illegal
  transitions throw `InvalidStateException`.
- **Seat locking is a provider seam.** `SeatLockProvider` keeps locking swappable
  (in-memory now, Redis/DB later). All operations are synchronized because the lock
  store is shared mutable state.
- **Atomic check-and-lock.** `BookingService` serializes "are these seats free?"
  and "lock them" per show with a per-show monitor, so two users cannot both pass
  the check.
- **Payments are attempts, not a counter.** Every attempt is stored as a `Payment`.
  A repeated success callback returns the existing success instead of confirming twice.
- **Timeout handling.** `BookingExpiryService` expires unpaid bookings and releases
  locks, both on a schedule and through an explicit cutoff method for deterministic
  demos/tests.
- **Failures are explicit.** Domain exceptions (`NotFoundException`,
  `SeatTemporaryUnavailableException`, `SeatPermanentlyUnavailableException`,
  `BadRequestException`, `InvalidStateException`, `ScreenAlreadyOccupiedException`)
  carry messages and are thrown at the service boundary.

---

## Scenario mapping

| Requirement | Where |
|---|---|
| Available seats for a show | `SeatAvailabilityService` |
| Seats temporarily unavailable after selection | `SeatLockProvider.lockSeats` |
| Payment failure retries then release seats | `PaymentsService.paymentFailed` + config |
| Payment success -> confirmed ticket | `PaymentsService.paymentSucceeded` + `Booking.confirmBooking` |
| Explicitly close session before payment | `TicketBookingFacade.cancelBooking` -> `BookingService.cancelBooking` |
| Overlapping seat selection rejected | `BookingService` + `SeatLockProvider` |
| Configurable session timeout (bonus) | `BookingExpiryService` + `booking.lock-timeout-seconds` |
| Overlapping show slots rejected | `ShowService.checkIfShowCreationAllowed` |
| Max seats per booking | `BookingService.validateSeatSelection` |
| No duplicate seats / foreign seats | `BookingService.validateSeatSelection` |

---

## Configuration

`src/main/resources/application.yml`:

```yaml
booking:
  lock-timeout-seconds: 600      # UserBookingSession timeout
  max-seats-per-booking: 10
  expiry-scan-interval-ms: 30000
payment:
  allowed-retries: 0             # Case 2 assumes zero retries
```

Change `allowed-retries` to `2` for production-like retry behaviour; the demo's Case 2
is written for the problem statement's "maximum retries zero" assumption.

---

## Intentionally out of scope

- HTTP/REST endpoints, DTOs and validation annotations (not required by the statement).
- Database/JPA (`Use of DB not expected`).
- Authentication/authorization (the statement assumes users are already logged in).
- Seat types and per-type pricing (listed as a further improvement, not a requirement).
- Gateway integration in the payment success flow.

## Further improvements

- Seat types (Silver/Gold/Diamond) with pricing.
- A `SeatLockProvider` backed by Redis for multi-instance deployments.
- Replace in-memory repositories with JPA (models become id-referencing entities).
- Add a thin REST adapter over `TicketBookingFacade` if the target role asks for it.
