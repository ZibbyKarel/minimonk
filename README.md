# Minimonk

Compact training application for automated warehouse logistics.

## Local development

- Frontend only: `cd apps/web && npm install && npm run dev`
- Frontend build check: `cd apps/web && npm run build`
- Full stack: `docker compose -f docker/docker-compose.yml up --build`
- Backend tests, when Gradle is installed locally: `gradle test`

Demo users use password `password`:

- `customer` / `CUSTOMER`
- `operator` / `WAREHOUSE_OPERATOR`
- `admin` / `ADMIN`

## Demo Flow

1. Start the stack with Docker Compose.
2. Open `http://localhost:5173/login`.
3. Sign in as `customer` with password `password`.
4. Create an order with card `4242424242424242` to see the successful asynchronous flow.
5. Create an order with card `4000000000000002` to trigger deterministic payment failure, stock release, and cancellation.
6. Refresh or wait on the orders page; in-progress orders are polled automatically.

## Architecture Notes

- The API Gateway performs coarse JWT and role checks for frontend-facing routes.
- `order-service` and `warehouse-service` also validate JWTs and enforce endpoint roles locally.
- RabbitMQ consumers use durable queues with dead-letter routing.
- Stateful consumers record processed event ids to avoid duplicate order transitions and duplicate stock release.
- Product and order frontend calls use Orval-generated TanStack Query hooks.
- Order overview uses a DTO projection query to avoid lazy-loading order items per row.

## Current Gaps

- Full PostgreSQL/RabbitMQ Testcontainers integration coverage is still pending.
- There is no Gradle wrapper in the repository, so local backend verification requires an installed `gradle` binary or Docker builds.
