# Architecture at a glance

Quick visual reference for how Minimonk is wired together. Diagrams are Mermaid — they render inline on GitHub and most markdown viewers.

For the canonical design rationale, see [`plans/warehouse-logistics-plan.md`](plans/warehouse-logistics-plan.md).

---

## 1. System overview (containers)

How the pieces are deployed and who talks to whom synchronously vs. asynchronously.

```mermaid
flowchart LR
    subgraph Client
        Web["apps/web<br/>React 19 + Vite<br/>:5173"]
    end

    subgraph Gateway["Edge"]
        GW["api-gateway<br/>Spring Cloud Gateway<br/>:8080"]
    end

    subgraph Services["Backend services (Spring Boot 3.3 / Java 21)"]
        US["user-service<br/>:8081"]
        WS["warehouse-service<br/>:8082"]
        OS["order-service<br/>:8083"]
        PS["payment-service<br/>:8084<br/><i>stateless</i>"]
    end

    subgraph Data["Per-service Postgres (no shared schema)"]
        UDB[("user-db<br/>:5433 / users")]
        WDB[("warehouse-db<br/>:5434 / warehouse")]
        ODB[("order-db<br/>:5435 / orders")]
    end

    MQ{{"RabbitMQ 3.13<br/>:5672 / :15672<br/>exchange: minimonk.events"}}

    Web -- HTTPS / JWT --> GW
    GW -- HTTP --> US
    GW -- HTTP --> WS
    GW -- HTTP --> OS

    US --- UDB
    WS --- WDB
    OS --- ODB

    WS <-. pub/sub .-> MQ
    OS <-. pub/sub .-> MQ
    PS <-. pub/sub .-> MQ

    classDef svc fill:#eef,stroke:#557
    classDef db fill:#efe,stroke:#575
    classDef mq fill:#fee,stroke:#a55
    class US,WS,OS,PS,GW svc
    class UDB,WDB,ODB db
    class MQ mq
```

**Rules of the road**

- Services talk to each other **only** through RabbitMQ events. There is no service-to-service HTTP.
- The gateway forwards HTTP to services via env-configured URLs (`USER_SERVICE_URL`, `WAREHOUSE_SERVICE_URL`, `ORDER_SERVICE_URL`).
- JWTs are validated **in every service**, not just the gateway (defense in depth — `common-security` provides `JwtSupport` + `JwtAuthenticationFilter`).
- `payment-service` is stateless: deterministic success/failure based on card number.

---

## 2. RabbitMQ topology

Single topic exchange `minimonk.events`; one durable queue per (consumer × event-it-listens-to); every queue has a dead-letter route back through the same exchange.

```mermaid
flowchart LR
    OS["order-service<br/>(publisher + consumer)"]
    WS["warehouse-service<br/>(publisher + consumer)"]
    PS["payment-service<br/>(publisher + consumer)"]

    EX{{"Topic exchange<br/>minimonk.events"}}

    subgraph WQ["warehouse queues"]
        wq1["warehouse.order-created<br/><i>← order.created</i>"]
        wq2["warehouse.payment-failed<br/><i>← payment.failed</i>"]
        wdlq["warehouse.dead-letter<br/><i>← warehouse.#.dead</i>"]
    end

    subgraph OQ["order queues"]
        oq1["orders.stock-reserved<br/><i>← stock.reserved</i>"]
        oq2["orders.stock-failed<br/><i>← stock.reservation.failed</i>"]
        oq3["orders.payment-succeeded<br/><i>← payment.succeeded</i>"]
        oq4["orders.payment-failed<br/><i>← payment.failed</i>"]
        oq5["orders.stock-released<br/><i>← stock.released</i>"]
        odlq["orders.dead-letter<br/><i>← orders.#.dead</i>"]
    end

    subgraph PQ["payment queues"]
        pq1["payments.stock-reserved<br/><i>← stock.reserved</i>"]
        pdlq["payments.dead-letter<br/><i>← payments.#.dead</i>"]
    end

    OS -- "publish: order.created" --> EX
    WS -- "publish: stock.reserved /<br/>stock.reservation.failed /<br/>stock.released" --> EX
    PS -- "publish: payment.succeeded /<br/>payment.failed" --> EX

    EX --> wq1 --> WS
    EX --> wq2 --> WS
    EX -.dlq.-> wdlq

    EX --> oq1 --> OS
    EX --> oq2 --> OS
    EX --> oq3 --> OS
    EX --> oq4 --> OS
    EX --> oq5 --> OS
    EX -.dlq.-> odlq

    EX --> pq1 --> PS
    EX -.dlq.-> pdlq
```

Defined in `services/{order,warehouse,payment}-service/.../config/RabbitConfig.java`. `RabbitConfig` is the most-connected node in the codebase graph — renaming an exchange/queue/binding here ripples through multiple services, so change deliberately.

---

## 3. Order lifecycle (happy path + failure branches)

End-to-end event choreography for one order. Listeners are **idempotent** (gated by `ProcessedEvent` table per service), so duplicate deliveries are safe.

```mermaid
sequenceDiagram
    autonumber
    actor C as Customer (web)
    participant GW as api-gateway
    participant OS as order-service
    participant MQ as minimonk.events
    participant WS as warehouse-service
    participant PS as payment-service

    C->>GW: POST /api/orders (JWT)
    GW->>OS: POST /orders
    OS->>OS: persist CustomerOrder (PENDING)
    OS-->>MQ: order.created
    OS-->>C: 201 {orderId}

    MQ->>WS: order.created
    alt Stock available
        WS->>WS: atomic decrement (SQL) +<br/>insert StockReservation
        WS-->>MQ: stock.reserved
        MQ->>PS: stock.reserved
        MQ->>OS: stock.reserved (status → RESERVED)
        OS-->>MQ: payment.requested
        Note over PS: deterministic by card #
        alt 4242…
            PS-->>MQ: payment.succeeded
            MQ->>OS: status → READY_FOR_PICKING
            OS-->>MQ: order.ready_for_picking
        else 4000…0002
            PS-->>MQ: payment.failed
            MQ->>OS: status → CANCELLED
            MQ->>WS: payment.failed → release reservation
            WS-->>MQ: stock.released
            MQ->>OS: order.cancelled
        end
    else Out of stock
        WS-->>MQ: stock.reservation.failed
        MQ->>OS: status → CANCELLED
        OS-->>MQ: order.cancelled
    end
```

The flow is also summarised one-liner-style as: `order.created → stock.reserved | stock.reservation.failed → payment.requested → payment.succeeded | payment.failed → order.ready_for_picking | order.cancelled (+ stock.released)`.

---

## 4. Frontend → backend wiring

```mermaid
flowchart LR
    subgraph Frontend["apps/web (React 19)"]
        Pages["Pages<br/>(Login, CreateOrder, Orders)"]
        Hooks["Orval-generated<br/>TanStack Query hooks<br/>(useList, useCreate, …)"]
        Manual["Manual API helpers<br/>createOrder / listOrders /<br/>listProducts / apiFetch<br/><i>legacy escape hatch</i>"]
    end

    OpenAPI[["api-gateway<br/>OpenAPI spec"]]
    GW["api-gateway :8080"]

    OpenAPI -- "npm run generate:api<br/>(Orval)" --> Hooks
    Pages --> Hooks
    Pages --> Manual
    Hooks -- "fetch + JWT bearer" --> GW
    Manual -- "fetch + JWT bearer" --> GW
```

After backend contract changes, regenerate the hooks: `cd apps/web && npm run generate:api`. Prefer the generated hooks; don't add new manual helpers without a reason.

---

## 5. Cross-cutting patterns (load-bearing, not optional)

| Pattern | Where it lives | What it guarantees |
| --- | --- | --- |
| **JWT in every service** | `libs/common-security` (`JwtSupport`, `JwtAuthenticationFilter`); each service's `SecurityConfig` | A bypassed/compromised gateway can't grant unauthorized access; endpoint-level role checks per service. |
| **Idempotent handlers** | `ProcessedEvent` + `ProcessedEventRepository` in order- and warehouse-service | Duplicate event deliveries don't double-transition orders or double-release stock. |
| **Durable queues + DLQ** | `RabbitConfig` in each consumer service | Messages survive broker restarts; poison messages route to `*.dead-letter` instead of looping. |
| **Event envelope + dedup ID** | `RabbitEventPublisher` (`libs/common-events`) wraps payloads in `EventEnvelope` with a UUID | Stable ID for the idempotency check above. |
| **Atomic stock decrement** | SQL-level in warehouse-service; contract pinned by `ProductRepositoryTest` | No oversells under concurrent reservations. |
| **DTO projection for order list** | `OrderOverviewDto` in `OrderService` | Avoids N+1 lazy-loading items per row on the orders list. |

---

## 6. Repo map (where to look)

```
services/
  api-gateway/        Spring Cloud Gateway, JWT pre-check
  user-service/       AppUser, AuthController, JWT issuance
  warehouse-service/  Product, StockReservation, WarehouseEventListener
  order-service/      CustomerOrder, OrderEventListener, PaymentListener
  payment-service/    stateless, deterministic by card #
libs/
  common-events/      RabbitEventPublisher, EventEnvelope, payload DTOs
  common-observability/
  common-security/    JwtSupport, JwtAuthenticationFilter
apps/web/             React 19 + Vite SPA, Orval-generated hooks
docker/
  docker-compose.yml  full stack: rabbit + 3× postgres + 5 services + web
docs/
  plans/warehouse-logistics-plan.md   canonical design doc
graphify-out/         knowledge-graph snapshot (see GRAPH_REPORT.md)
```

---

## Demo credentials

Password is always `password` for: `customer`, `operator`, `admin`.
Card `4242424242424242` → payment succeeds. Card `4000000000000002` → deterministic failure (exercises the cancellation + stock-release branch).
