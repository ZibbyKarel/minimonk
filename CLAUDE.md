# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## graphify

This project has a graphify knowledge graph at `graphify-out/`.

- Before answering architecture or codebase questions, read `graphify-out/GRAPH_REPORT.md` for god nodes and community structure.
- If `graphify-out/wiki/index.md` exists, navigate it instead of reading raw files.
- After modifying code files in this session, run `graphify update .` to keep the graph current (AST-only, no API cost).

## Repository layout

Polyglot monorepo with a Gradle multi-project backend and a Vite/React frontend:

- `services/` — Spring Boot 3.3.5 microservices on Java 21 (`api-gateway`, `user-service`, `warehouse-service`, `order-service`, `payment-service`).
- `libs/` — shared Gradle modules consumed by services (`common-events`, `common-observability`, `common-security`).
- `apps/web/` — React 19 + Vite SPA. API hooks are generated from the gateway's OpenAPI by Orval (`npm run generate:api`); manually-written calls under `src/` are the legacy escape hatch.
- `docker/` — `docker-compose.yml` (full stack: Postgres-per-service, RabbitMQ, all services, web) and `Dockerfile.service` (per-service build).
- `docs/plans/warehouse-logistics-plan.md` — the canonical design doc referenced by the README.

## Commands

Backend (no Gradle wrapper in repo — `gradlew` exists but the README explicitly notes a wrapper is missing for backend verification; use a locally installed `gradle` or Docker):

- `gradle build` — compile + test everything.
- `gradle test` — run all JUnit Platform tests across modules.
- `gradle :services:order-service:test` — one module's tests.
- `gradle :services:order-service:test --tests "OrderEventListenerTest"` — single test class.
- `gradle :services:order-service:bootRun` — run one service locally (needs its Postgres/RabbitMQ deps reachable).

Frontend (`apps/web`):

- `npm install && npm run dev` — Vite dev server on `:5173`.
- `npm run build` — `tsc -b && vite build`. Use this as the type-check/lint gate before declaring frontend work done.
- `npm run lint` — ESLint.
- `npm run generate:api` — regenerate TanStack Query hooks from the gateway OpenAPI spec via Orval. Run after backend contract changes.

Full stack:

- `docker compose -f docker/docker-compose.yml up --build` — brings up RabbitMQ, three Postgres instances (one per stateful service), all five backend services, and the web SPA.

Demo credentials (password is always `password`): `customer`, `operator`, `admin`. Card `4242424242424242` succeeds; `4000000000000002` deterministically fails payment to exercise the cancellation/stock-release path.

## Architecture

Five Spring Boot services behind an API gateway, communicating with each other only via RabbitMQ events (no service-to-service HTTP). Each stateful service owns its own Postgres database; there is no shared schema.

- **`api-gateway`** (`:8080`) — Spring Cloud Gateway. Performs coarse JWT + role checks for frontend routes and forwards to downstream services via env-configured URLs.
- **`user-service`** (`:8081`, `users` DB) — `AppUser`, `AuthService`, `AuthController`. Issues JWTs; the gateway and every other service validate them locally (defense in depth — do not centralize this in the gateway alone).
- **`warehouse-service`** (`:8082`, `warehouse` DB) — `Product`, `ProductService`, `StockReservation`. Consumes order events and emits `StockReserved`/`StockReservationFailed`. Inventory atomicity is enforced at the SQL level; see `ProductRepositoryTest` for the contract.
- **`order-service`** (`:8083`, `orders` DB) — `CustomerOrder`, `OrderService`, `OrderEventListener`, `PaymentListener`. Orchestrates the order lifecycle by reacting to warehouse + payment events. Order list query uses a DTO projection (`OrderOverviewDto`) to avoid lazy-loading items per row.
- **`payment-service`** (`:8084`, stateless) — consumes `PaymentRequested`, deterministically succeeds/fails by card number, emits `PaymentSucceeded`/`PaymentFailed`.

Async order flow: `OrderCreated → StockReserved | StockReservationFailed → PaymentRequested → PaymentSucceeded | PaymentFailed → OrderReadyForPicking | OrderCancelled (+ StockReleased)`. The hyperedge group in `GRAPH_REPORT.md` lists every event.

Cross-cutting patterns (treat these as load-bearing, not optional):

- **JWT validation in every service**, not only the gateway — `common-security` provides `JwtSupport` + `JwtAuthenticationFilter`. Each `SecurityConfig` wires endpoint-level role checks.
- **Idempotent event handlers** via `ProcessedEvent` + `ProcessedEventRepository`. Listeners record processed event IDs before acting, so duplicate deliveries don't double-transition orders or double-release stock.
- **Durable queues with dead-letter routing** configured centrally in `RabbitConfig` (warehouse + order services). It's the most-connected node in the graph — touching exchange/queue/binding names ripples through both services.
- **Event publishing** goes through `RabbitEventPublisher` (from `common-events`) wrapping payloads in an `EventEnvelope` with a UUID used for dedup.
- **Frontend data fetching** prefers Orval-generated TanStack Query hooks (`useList`, `useCreate`, etc.). The manual `createOrder`/`listOrders`/`listProducts`/`apiFetch` helpers exist for paths Orval doesn't cover — don't add new ones without a reason.

## Known gaps (from README)

- No full Testcontainers coverage for Postgres + RabbitMQ integration paths yet.
- No Gradle wrapper checked in; backend verification needs a local `gradle` install or Docker builds.
