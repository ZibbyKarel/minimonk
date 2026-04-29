# Minimonk Warehouse Logistics Plan

## Goal

Build a compact training application for automated warehouse logistics. The project should stay small enough to finish, but it should demonstrate backend-heavy topics that commonly appear in real microservice systems:

- API Gateway and independently deployable services
- JWT authentication and role-based authorization
- event-driven communication through RabbitMQ
- per-service PostgreSQL databases
- order processing with eventual consistency and compensation
- optimistic locking for concurrent stock reservations
- DTO-based REST interfaces with generated OpenAPI documentation
- frontend API clients generated from OpenAPI via Orval and TanStack Query
- N+1 query avoidance using DTO projections
- trace propagation through HTTP and RabbitMQ using `traceparent`
- Docker-based local development and demo deployment

## Architecture Decision Summary

- Repository style: Gradle multi-project monorepo, optimized for the Java/Spring backend.
- Frontend: React 19, TypeScript, Tailwind, TanStack Router, TanStack Query, React Hook Form, Orval.
- Backend: Java 21, Spring Boot 3.x, Spring Security, Spring Cloud Gateway, Spring AMQP, Spring Data JPA, Hibernate.
- Database: PostgreSQL, with one database per microservice.
- Messaging: RabbitMQ with JSON messages.
- Migrations: Flyway.
- API documentation: `springdoc-openapi`, generated from annotated backend controllers and DTOs.
- Runtime: everything runs through Docker Compose.
- Observability phase 1: propagate and log `traceparent` / trace id across services and RabbitMQ.

## Repository Layout

```txt
minimonk/
  settings.gradle.kts
  build.gradle.kts

  apps/
    web/

  services/
    api-gateway/
    user-service/
    order-service/
    warehouse-service/
    payment-service/

  libs/
    common-events/
    common-observability/
    common-security/

  docker/
    docker-compose.yml

  docs/
    warehouse-logistics-plan.md
```

## Backend Services

### API Gateway

Responsibilities:

- expose a single HTTP entrypoint for the frontend
- route requests to backend services
- validate JWT tokens for protected routes
- propagate `traceparent` header to downstream services
- apply coarse-grained authorization at route level where useful

Suggested routes:

- `/api/auth/**` -> `user-service`
- `/api/products/**` -> `warehouse-service`
- `/api/orders/**` -> `order-service`

### User Service

Responsibilities:

- manage demo users and roles
- authenticate users
- issue JWT tokens
- expose user identity data needed by other services

Roles:

- `CUSTOMER`: create orders and view own orders
- `WAREHOUSE_OPERATOR`: view stock and reservation state
- `ADMIN`: administrative access, including broader read access

Initial users can be inserted with Flyway seed data.

### Warehouse Service

Responsibilities:

- manage products and stock quantities
- expose product list for order creation
- reserve stock after an order is created
- release stock when payment fails
- demonstrate optimistic locking for concurrent reservations

Product model:

```txt
Product
  id
  sku
  name
  description
  price
  availableQuantity
  reservedQuantity
  version
```

Reservation behavior:

- on `OrderCreated`, attempt to reserve stock
- if enough stock exists, decrement `availableQuantity`, increment `reservedQuantity`, and publish `StockReserved`
- if stock is unavailable, publish `StockReservationFailed`
- use JPA `@Version` for optimistic locking
- retry a small number of times on optimistic lock conflicts

Compensation behavior:

- on `PaymentFailed`, release previously reserved stock
- increment `availableQuantity`
- decrement `reservedQuantity`
- publish `StockReleased`

### Order Service

Responsibilities:

- accept order creation requests
- persist orders and order items
- publish `OrderCreated`
- listen to warehouse and payment events
- maintain order status
- expose order overview for frontend
- demonstrate N+1 avoidance using DTO projections

Order status flow:

```txt
CREATED
STOCK_RESERVED
PAYMENT_PENDING
PAID
READY_FOR_PICKING
COMPLETED
```

Failure flow:

```txt
CREATED
STOCK_RESERVATION_FAILED
CANCELLED
```

or:

```txt
CREATED
STOCK_RESERVED
PAYMENT_PENDING
PAYMENT_FAILED
STOCK_RELEASED
CANCELLED
```

N+1 demonstration:

- endpoint: `GET /orders`
- one user can have multiple orders, each with multiple order items
- avoid loading entity graphs lazily in a loop
- use DTO projection for order overview rows

Example projection target:

```txt
OrderOverviewDto
  orderId
  customerId
  status
  totalAmount
  itemCount
  createdAt
  updatedAt
```

### Payment Service

Responsibilities:

- listen for `StockReserved`
- simulate payment processing
- publish `PaymentSucceeded` or `PaymentFailed`

Payment behavior:

- use deterministic failure for a configured demo card number
- example failing card: `4000000000000002`
- all other card numbers can succeed by default
- random failure can be added later as an optional mode

## Frontend

Pages:

- `/login`
- `/create-order`
- `/orders`

### Login

- user enters demo credentials
- frontend receives JWT
- token is stored for the current browser session
- API requests include `Authorization: Bearer <token>`

### Create Order

- display product table from `warehouse-service` through the gateway
- allow user to enter quantities for selected products
- submit a single order request
- show optimistic UI feedback after submission
- redirect or link to order overview

### Orders

- display orders and current statuses
- use generated TanStack Query hooks from Orval
- periodically refetch order statuses while orders are in progress

## API Contracts And DTO Rules

REST endpoints must use explicit DTO objects. JPA entities must not be exposed directly.

DTO categories:

- request DTOs for incoming API payloads
- response DTOs for outgoing API payloads
- event DTOs for RabbitMQ payloads
- projection DTOs for optimized read models

Backend controllers should be annotated so `springdoc-openapi` can generate clear OpenAPI contracts. Use OpenAPI annotations when automatic inference is not enough, especially for:

- authentication requirements
- role requirements
- response status codes
- validation errors
- request and response examples where helpful

Frontend generation:

- each service exposes `/v3/api-docs`
- Orval consumes OpenAPI specs and generates TypeScript clients
- Orval generates TanStack Query hooks
- frontend runtime calls go through API Gateway

Initial approach:

- Orval can read service OpenAPI specs directly in development
- generated clients should use a shared fetcher configured with the gateway base URL
- OpenAPI aggregation through API Gateway can be added later

## Messaging

Use RabbitMQ topic exchanges with JSON messages.

Shared event envelope:

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "traceId": "trace-id",
  "occurredAt": "2026-04-29T12:00:00Z",
  "payload": {}
}
```

RabbitMQ headers:

- include W3C `traceparent`
- include event id
- include event type

Suggested events:

- `OrderCreated`
- `StockReserved`
- `StockReservationFailed`
- `PaymentRequested`
- `PaymentSucceeded`
- `PaymentFailed`
- `StockReleased`
- `OrderCancelled`
- `OrderReadyForPicking`

The initial implementation should use message listeners rather than direct synchronous service-to-service calls for the core order flow.

## Consistency And Failure Handling

The system uses eventual consistency. Each service owns its own database and transaction boundary.

Order creation flow:

1. Frontend sends order request to API Gateway.
2. Gateway forwards request to `order-service`.
3. `order-service` creates order with status `CREATED`.
4. `order-service` publishes `OrderCreated`.
5. `warehouse-service` listens and attempts stock reservation.
6. `warehouse-service` publishes `StockReserved` or `StockReservationFailed`.
7. `payment-service` listens for successful stock reservation and simulates payment.
8. `payment-service` publishes `PaymentSucceeded` or `PaymentFailed`.
9. `order-service` updates the order status based on events.
10. `warehouse-service` releases stock when payment fails.

Important reliability topics to include in the plan or implementation notes:

- idempotent event handlers
- event id tracking to avoid double processing
- retries for transient optimistic lock conflicts
- dead-letter queues for poison messages
- compensating stock release after failed payment

For the training version, these can be implemented incrementally.

## Security

Authentication:

- JWT issued by `user-service`
- Gateway validates JWT for protected frontend-facing routes
- backend services also validate JWT where they expose protected HTTP endpoints

Authorization:

- `CUSTOMER` can create orders and view own orders
- `WAREHOUSE_OPERATOR` can view stock and reservation state
- `ADMIN` can access administrative and broader read endpoints

Service-to-service messaging:

- RabbitMQ is trusted inside the Docker network for the training project
- message payloads must still include enough identity/context to audit user-originated actions

## Observability

Phase 1 observability:

- accept or create `traceparent` at API Gateway
- propagate `traceparent` to downstream HTTP requests
- propagate `traceparent` through RabbitMQ message headers
- log trace id in every service
- include trace id in important business logs, especially order id and event id transitions

Possible later extension:

- OpenTelemetry Java agent or SDK
- Jaeger in Docker Compose
- distributed trace visualization for one complete order flow

## Testing Strategy

### Unit Tests

- order status transitions
- stock reservation rules
- payment decision rules
- authorization helper logic

### Integration Tests

Use Testcontainers for:

- PostgreSQL
- RabbitMQ

Important integration scenarios:

- successful order flow
- stock reservation failure
- payment failure and stock release
- duplicate event handling
- unauthorized access

### Concurrency Test

Goal:

- prove that concurrent orders cannot reserve more stock than exists

Scenario:

- seed one product with limited quantity
- start many concurrent order/reservation attempts for that product
- verify final `availableQuantity` and `reservedQuantity`
- verify only the expected number of reservations succeeded
- verify optimistic lock conflicts are retried or handled predictably

### N+1 Test / Demonstration

Goal:

- show how the order overview avoids N+1 queries

Approach:

- seed one user with multiple orders and order items
- implement order overview using DTO projection
- optionally enable Hibernate SQL logging for the demo profile
- verify the endpoint does not lazily load order items per order row

## Docker Compose

Docker Compose should include:

- API Gateway
- User Service
- Order Service
- Warehouse Service
- Payment Service
- Frontend
- PostgreSQL databases
- RabbitMQ with management UI

Database layout:

- either one PostgreSQL container with multiple databases
- or one PostgreSQL container per service

For clarity in a training project, prefer one PostgreSQL container per service if resource usage stays reasonable. If startup becomes too heavy, switch to one PostgreSQL container with multiple databases while preserving logical ownership per service.

## Current Implementation Snapshot

Status after commit `39086f2`:

- The repository skeleton is in place: Gradle multi-project backend, React/Vite frontend, shared libraries, Docker Compose, RabbitMQ, and per-service PostgreSQL containers.
- The gateway routes `/api/auth/**`, `/api/products/**`, and `/api/orders/**` to the correct services and adds a `traceparent` header when one is missing.
- `user-service` has seeded demo users, login, password verification, JWT issuing, and OpenAPI exposure.
- `order-service` and `warehouse-service` now validate JWTs locally with the shared servlet `JwtAuthenticationFilter`, enforce endpoint roles, and reject unauthenticated protected requests.
- `api-gateway` validates JWT presence and syntax for protected frontend-facing routes, but coarse role checks are currently enforced in downstream services rather than at gateway route level.
- `warehouse-service` exposes products through DTOs, seeds product data with Flyway, and protects product access for `CUSTOMER`, `WAREHOUSE_OPERATOR`, and `ADMIN`.
- `order-service` creates orders, publishes `OrderCreated`, exposes order overview DTOs, and uses projection queries for the order list.
- `warehouse-service`, `payment-service`, and `order-service` implement the asynchronous order flow through RabbitMQ events.
- Stock reservation uses JPA optimistic locking and a small retry loop.
- The deterministic failing card path exists through `minimonk.payment.failing-card`.
- Payment failure releases stock and publishes `StockReleased`; the order service transitions failed payment orders to `CANCELLED` after stock release.
- The frontend has login, create-order, and orders views using TanStack Query, session storage JWTs, and manual API helpers.
- Orval is configured, but generated clients/hooks are not committed and the frontend currently uses `apps/web/src/api/manual.ts`.
- There are currently no project tests.

## Phase Status

| Phase | Status | Notes |
| --- | --- | --- |
| Phase 1: Project Skeleton And Infrastructure | Mostly done | Structure, Compose, health endpoints, and trace header propagation exist. Still needs clean-start verification and trace-id log verification. |
| Phase 2: Authentication And Gateway | Mostly done | Login/JWT, gateway validation, and backend service role checks exist. Gateway-level role checks are intentionally deferred or can be re-added if wanted. |
| Phase 3: Products And OpenAPI/Orval | Partially done | Product API, DTOs, Flyway seed, OpenAPI deps, Orval config, and UI exist. Generated Orval hooks are still missing from the frontend runtime. |
| Phase 4: Event-Driven Order Flow | Mostly done | Core RabbitMQ flow exists. Needs end-to-end verification, better frontend in-progress polling, and clearer success/failure demo states. |
| Phase 5: Concurrency And Compensation | Partially done | Optimistic locking, retry, deterministic payment failure, and stock release exist. Needs integration/concurrency tests and idempotency. |
| Phase 6: Query Optimization And N+1 Demonstration | Partially done | Order overview projection exists. Needs seeded demo data, documentation, and a test or logging-based proof. |
| Phase 7: Hardening And Demo Polish | Remaining | Tests, dead-letter queues, idempotency, error response polish, README/demo docs, and optional tracing visualization remain. |

## Finalized Implementation Plan

### Phase 1: Project Skeleton And Infrastructure

- [x] create Gradle multi-project structure
- [x] add Spring Boot service skeletons
- [x] add React/Vite frontend skeleton
- [x] add Docker Compose for all services and infrastructure
- [x] add health endpoints
- [x] add basic logging baseline
- [x] add `traceparent` propagation baseline through gateway and RabbitMQ publishers
- [ ] verify the whole stack starts from a clean checkout with Docker Compose
- [ ] verify trace id appears in useful service logs during an order flow

Done when:

- the whole stack starts with Docker Compose
- each service has a health endpoint
- frontend can call the gateway
- trace id appears in logs

### Phase 2: Authentication And Gateway

- [x] implement demo users in `user-service`
- [x] implement login endpoint
- [x] issue JWT tokens with roles
- [x] configure Spring Security in Gateway for JWT validation
- [x] route frontend API calls through Gateway
- [x] validate JWTs in `order-service` and `warehouse-service`
- [x] protect service endpoints by role
- [ ] decide whether gateway should also enforce coarse route-level roles or stay as token validation plus downstream authorization
- [ ] add tests for unauthenticated and unauthorized access

Done when:

- frontend login works
- protected routes reject unauthenticated requests
- role-based access can be demonstrated

### Phase 3: Products And OpenAPI/Orval

- [x] implement product read API in `warehouse-service`
- [x] seed products with Flyway
- [x] expose OpenAPI docs
- [x] configure Orval in frontend
- [ ] generate TanStack Query hooks from OpenAPI
- [ ] replace manual frontend API helpers with generated Orval hooks where practical
- [x] build product table and order form
- [ ] add OpenAPI annotations for auth, role expectations, status codes, and validation errors where useful

Done when:

- frontend renders products using generated hooks
- DTO contracts are visible in OpenAPI
- no JPA entities are returned from controllers

### Phase 4: Event-Driven Order Flow

- [x] implement order creation in `order-service`
- [x] publish `OrderCreated`
- [x] implement warehouse reservation listener
- [x] publish stock reservation events
- [x] implement payment listener
- [x] publish payment result events
- [x] update order status from events
- [x] show orders and statuses in frontend
- [ ] add frontend polling/refetch interval while orders are in progress
- [ ] verify successful order flow end to end through Docker Compose
- [ ] verify stock reservation failure flow end to end
- [ ] verify payment failure and stock release flow end to end

Done when:

- user can create an order from the frontend
- order status changes asynchronously
- success and failure paths are visible

### Phase 5: Concurrency And Compensation

- [x] add optimistic locking to stock updates
- [x] handle optimistic lock retries
- [x] implement deterministic payment failure card
- [x] release stock after payment failure
- [ ] make stock release idempotent for duplicate `PaymentFailed` events
- [ ] add integration tests for concurrent reservations
- [ ] add integration test for payment failure returning stock to availability
- [ ] add assertions that failed-payment orders end in `CANCELLED`

Done when:

- concurrent stock reservation test passes
- payment failure returns stock to availability
- order ends in a cancelled state after failed payment

### Phase 6: Query Optimization And N+1 Demonstration

- [x] implement order overview with DTO projection
- [ ] seed multiple orders and items for one user in a demo/test profile
- [ ] document the avoided N+1 scenario
- [ ] add test or logging-based demonstration
- [ ] optionally add Hibernate SQL logging for a demo profile

Done when:

- order overview uses projection DTOs
- the endpoint avoids lazy loading loops
- the approach is documented in code or project docs

### Phase 7: Hardening And Demo Polish

- [ ] add idempotent event handling and event id tracking
- [ ] add dead-letter queues for poison messages
- [ ] improve backend validation and error responses
- [ ] add frontend loading, empty, and error states
- [ ] add README instructions for local dev, Docker Compose, demo users, and demo scenarios
- [ ] add focused unit tests for status transitions, stock rules, payment decisions, and JWT helpers
- [ ] add Testcontainers integration tests for PostgreSQL and RabbitMQ flows
- [ ] optionally add Jaeger/OpenTelemetry visualization

Done when:

- project can be started from a clean checkout
- demo scenarios are documented
- core flows are covered by tests

## Decisions And Open Questions

- RabbitMQ exchanges and queues are declared automatically by Spring configuration for now.
- The first version uses one PostgreSQL container per stateful service.
- Orval should keep consuming OpenAPI specs directly from services during development; gateway aggregation can remain a later enhancement.
- Random payment failure remains optional. The deterministic failing card is enough for repeatable demos and tests.
- Gateway authorization strategy remains open: either keep the gateway as token validation only, or restore coarse route-level role checks in addition to service-level authorization.
