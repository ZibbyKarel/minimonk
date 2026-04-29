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

## Implementation Phases

### Phase 1: Project Skeleton And Infrastructure

- create Gradle multi-project structure
- add Spring Boot service skeletons
- add React/Vite frontend skeleton
- add Docker Compose for all services and infrastructure
- add health endpoints
- add basic logging
- add `traceparent` propagation baseline

Done when:

- the whole stack starts with Docker Compose
- each service has a health endpoint
- frontend can call the gateway
- trace id appears in logs

### Phase 2: Authentication And Gateway

- implement demo users in `user-service`
- implement login endpoint
- issue JWT tokens with roles
- configure Spring Security in Gateway
- route frontend API calls through Gateway
- protect endpoints by role

Done when:

- frontend login works
- protected routes reject unauthenticated requests
- role-based access can be demonstrated

### Phase 3: Products And OpenAPI/Orval

- implement product read API in `warehouse-service`
- seed products with Flyway
- expose OpenAPI docs
- configure Orval in frontend
- generate TanStack Query hooks
- build product table and order form

Done when:

- frontend renders products using generated hooks
- DTO contracts are visible in OpenAPI
- no JPA entities are returned from controllers

### Phase 4: Event-Driven Order Flow

- implement order creation in `order-service`
- publish `OrderCreated`
- implement warehouse reservation listener
- publish stock reservation events
- implement payment listener
- publish payment result events
- update order status from events
- show orders and statuses in frontend

Done when:

- user can create an order from the frontend
- order status changes asynchronously
- success and failure paths are visible

### Phase 5: Concurrency And Compensation

- add optimistic locking to stock updates
- handle optimistic lock retries
- implement deterministic payment failure card
- release stock after payment failure
- add integration tests for concurrent reservations

Done when:

- concurrent stock reservation test passes
- payment failure returns stock to availability
- order ends in a cancelled state after failed payment

### Phase 6: Query Optimization And N+1 Demonstration

- implement order overview with DTO projection
- seed multiple orders and items for one user
- document the avoided N+1 scenario
- add test or logging-based demonstration

Done when:

- order overview uses projection DTOs
- the endpoint avoids lazy loading loops
- the approach is documented in code or project docs

### Phase 7: Hardening And Demo Polish

- add idempotent event handling
- add dead-letter queues
- improve error responses
- add frontend loading/error states
- add README instructions
- optionally add Jaeger/OpenTelemetry visualization

Done when:

- project can be started from a clean checkout
- demo scenarios are documented
- core flows are covered by tests

## Open Questions

- Should RabbitMQ exchanges and queues be declared manually in infrastructure config, or automatically by Spring configuration?
- Should the first version use one PostgreSQL container per service, or one container with multiple databases?
- Should OpenAPI specs be consumed directly from services, or should API Gateway aggregate them in a later phase?
- Should random payment failure be added after the deterministic failing card scenario?

