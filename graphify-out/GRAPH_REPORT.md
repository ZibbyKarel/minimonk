# Graph Report - .  (2026-05-10)

## Corpus Check
- Corpus is ~10,526 words - fits in a single context window. You may not need a graph.

## Summary
- 374 nodes · 484 edges · 63 communities detected
- Extraction: 75% EXTRACTED · 25% INFERRED · 0% AMBIGUOUS · INFERRED: 119 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Architecture & System Plan|Architecture & System Plan]]
- [[_COMMUNITY_RabbitMQ Configuration|RabbitMQ Configuration]]
- [[_COMMUNITY_Order Domain Model|Order Domain Model]]
- [[_COMMUNITY_Frontend & User Model|Frontend & User Model]]
- [[_COMMUNITY_Order Service API Layer|Order Service API Layer]]
- [[_COMMUNITY_Warehouse Product Model|Warehouse Product Model]]
- [[_COMMUNITY_Stock Reservation Flow|Stock Reservation Flow]]
- [[_COMMUNITY_Order Event Listeners|Order Event Listeners]]
- [[_COMMUNITY_Frontend API Client|Frontend API Client]]
- [[_COMMUNITY_JWT Authentication|JWT Authentication]]
- [[_COMMUNITY_Security Configuration|Security Configuration]]
- [[_COMMUNITY_Inventory Atomicity Tests|Inventory Atomicity Tests]]
- [[_COMMUNITY_Auth & Session Pages|Auth & Session Pages]]
- [[_COMMUNITY_Event Publisher Library|Event Publisher Library]]
- [[_COMMUNITY_Manual API Calls|Manual API Calls]]
- [[_COMMUNITY_API Exception Handling|API Exception Handling]]
- [[_COMMUNITY_Project Documentation|Project Documentation]]
- [[_COMMUNITY_Idempotent Event Tracking|Idempotent Event Tracking]]
- [[_COMMUNITY_Product Controller|Product Controller]]
- [[_COMMUNITY_Authentication Controller|Authentication Controller]]
- [[_COMMUNITY_Frontend Utilities|Frontend Utilities]]
- [[_COMMUNITY_API Gateway Service|API Gateway Service]]
- [[_COMMUNITY_Payment Service Entry|Payment Service Entry]]
- [[_COMMUNITY_Order Service Entry|Order Service Entry]]
- [[_COMMUNITY_Event Deduplication Repo|Event Deduplication Repo]]
- [[_COMMUNITY_Warehouse Service Entry|Warehouse Service Entry]]
- [[_COMMUNITY_User Service Entry|User Service Entry]]
- [[_COMMUNITY_Event Envelope|Event Envelope]]
- [[_COMMUNITY_Frontend Root Layout|Frontend Root Layout]]
- [[_COMMUNITY_Product Repository|Product Repository]]
- [[_COMMUNITY_Root Build Config|Root Build Config]]
- [[_COMMUNITY_Gradle Settings|Gradle Settings]]
- [[_COMMUNITY_Observability Build|Observability Build]]
- [[_COMMUNITY_Events Lib Build|Events Lib Build]]
- [[_COMMUNITY_Order Created Payload|Order Created Payload]]
- [[_COMMUNITY_Order Item Payload|Order Item Payload]]
- [[_COMMUNITY_Payment Result Payload|Payment Result Payload]]
- [[_COMMUNITY_Stock Reserved Payload|Stock Reserved Payload]]
- [[_COMMUNITY_Stock Reservation Failed Payload|Stock Reservation Failed Payload]]
- [[_COMMUNITY_Security Lib Build|Security Lib Build]]
- [[_COMMUNITY_Tailwind Config|Tailwind Config]]
- [[_COMMUNITY_Orval Code-Gen Config|Orval Code-Gen Config]]
- [[_COMMUNITY_Vite Build Config|Vite Build Config]]
- [[_COMMUNITY_PostCSS Config|PostCSS Config]]
- [[_COMMUNITY_Frontend Entry Point|Frontend Entry Point]]
- [[_COMMUNITY_Frontend Router|Frontend Router]]
- [[_COMMUNITY_Orders Page|Orders Page]]
- [[_COMMUNITY_API Gateway Build|API Gateway Build]]
- [[_COMMUNITY_Payment Service Build|Payment Service Build]]
- [[_COMMUNITY_Order Service Build|Order Service Build]]
- [[_COMMUNITY_Order Status Enum|Order Status Enum]]
- [[_COMMUNITY_Order Overview DTO|Order Overview DTO]]
- [[_COMMUNITY_Create Order Response|Create Order Response]]
- [[_COMMUNITY_Create Order Item Request|Create Order Item Request]]
- [[_COMMUNITY_Create Order Request|Create Order Request]]
- [[_COMMUNITY_Warehouse Service Build|Warehouse Service Build]]
- [[_COMMUNITY_Product DTO|Product DTO]]
- [[_COMMUNITY_User Service Build|User Service Build]]
- [[_COMMUNITY_Login Request|Login Request]]
- [[_COMMUNITY_Login Response|Login Response]]
- [[_COMMUNITY_Payment Requested Event|Payment Requested Event]]
- [[_COMMUNITY_Order Cancelled Event|Order Cancelled Event]]
- [[_COMMUNITY_Order Ready Event|Order Ready Event]]

## God Nodes (most connected - your core abstractions)
1. `RabbitConfig` - 27 edges
2. `Minimonk Warehouse Logistics Plan` - 22 edges
3. `Warehouse Service` - 13 edges
4. `SecurityConfig` - 12 edges
5. `CustomerOrder` - 12 edges
6. `Product` - 11 edges
7. `Order Service` - 10 edges
8. `RabbitMQ Messaging` - 10 edges
9. `OrderEventListener` - 9 edges
10. `ProductRepositoryTest` - 8 edges

## Surprising Connections (you probably didn't know these)
- `RabbitMQ` --semantically_similar_to--> `RabbitMQ Messaging`  [INFERRED] [semantically similar]
  README.md → docs/warehouse-logistics-plan.md
- `Orval` --semantically_similar_to--> `Orval OpenAPI Client Generator`  [INFERRED] [semantically similar]
  README.md → docs/warehouse-logistics-plan.md
- `DTO Projection Query` --semantically_similar_to--> `N+1 Query Avoidance via DTO Projections`  [INFERRED] [semantically similar]
  README.md → docs/warehouse-logistics-plan.md
- `JWT Authentication` --semantically_similar_to--> `JWT Authentication`  [INFERRED] [semantically similar]
  README.md → docs/warehouse-logistics-plan.md
- `PostgreSQL` --semantically_similar_to--> `PostgreSQL Per-Service Database`  [INFERRED] [semantically similar]
  README.md → docs/warehouse-logistics-plan.md

## Hyperedges (group relationships)
- **Minimonk Microservices** — plan_api_gateway, plan_user_service, plan_warehouse_service, plan_order_service, plan_payment_service [EXTRACTED 1.00]
- **Asynchronous Order Flow Events** — plan_event_order_created, plan_event_stock_reserved, plan_event_stock_reservation_failed, plan_event_payment_requested, plan_event_payment_succeeded, plan_event_payment_failed, plan_event_stock_released, plan_event_order_cancelled, plan_event_order_ready_for_picking [EXTRACTED 1.00]
- **Shared Backend Libraries** — plan_common_events_lib, plan_common_observability_lib, plan_common_security_lib [EXTRACTED 1.00]
- **Frontend Pages** — plan_login_page, plan_create_order_page, plan_orders_page [EXTRACTED 1.00]
- **Reliability Patterns** — plan_idempotent_handlers, plan_dead_letter_queues, plan_optimistic_locking, plan_eventual_consistency [EXTRACTED 1.00]
- **Role-Based Access Control** — plan_role_customer, plan_role_warehouse_operator, plan_role_admin, plan_jwt_auth [EXTRACTED 1.00]

## Communities

### Community 0 - "Architecture & System Plan"
Cohesion: 0.06
Nodes (59): API Gateway Service, common-events Library, common-observability Library, common-security Library, Create Order Page, Dead-Letter Queues, Rationale: Defense in Depth Authorization, Rationale: Deterministic Failing Card (+51 more)

### Community 1 - "RabbitMQ Configuration"
Cohesion: 0.09
Nodes (1): RabbitConfig

### Community 2 - "Order Domain Model"
Cohesion: 0.11
Nodes (3): CustomerOrder, CustomerOrderItem, CustomerOrderTest

### Community 3 - "Frontend & User Model"
Cohesion: 0.09
Nodes (6): AppUser, AppUserRepository, AuthService, submit(), TraceContext, TraceGatewayFilter

### Community 4 - "Order Service API Layer"
Cohesion: 0.13
Nodes (4): CustomerOrderRepository, OrderController, OrderService, PaymentListener

### Community 5 - "Warehouse Product Model"
Cohesion: 0.15
Nodes (3): Product, ProductService, ProductTest

### Community 6 - "Stock Reservation Flow"
Cohesion: 0.16
Nodes (3): StockReservation, StockReservationRepository, WarehouseEventListener

### Community 7 - "Order Event Listeners"
Cohesion: 0.19
Nodes (3): OrderEventListener, OrderEventListenerTest, PaymentListenerTest

### Community 8 - "Frontend API Client"
Cohesion: 0.19
Nodes (12): apiFetch(), create(), getCreateMutationOptions(), getListQueryKey(), getListQueryOptions(), list(), useCreate(), useList() (+4 more)

### Community 9 - "JWT Authentication"
Cohesion: 0.16
Nodes (3): JwtAuthenticationFilter, JwtSupport, JwtSupportTest

### Community 10 - "Security Configuration"
Cohesion: 0.21
Nodes (1): SecurityConfig

### Community 11 - "Inventory Atomicity Tests"
Cohesion: 0.47
Nodes (1): ProductRepositoryTest

### Community 12 - "Auth & Session Pages"
Cohesion: 0.25
Nodes (3): IndexRedirect(), LoginPage(), useSession()

### Community 13 - "Event Publisher Library"
Cohesion: 0.43
Nodes (1): RabbitEventPublisher

### Community 14 - "Manual API Calls"
Cohesion: 0.53
Nodes (4): authHeaders(), createOrder(), listOrders(), listProducts()

### Community 15 - "API Exception Handling"
Cohesion: 0.33
Nodes (1): ApiExceptionHandler

### Community 16 - "Project Documentation"
Cohesion: 0.4
Nodes (5): API Gateway, DTO Projection Query, JWT Authentication, Order Service, Warehouse Service

### Community 17 - "Idempotent Event Tracking"
Cohesion: 0.5
Nodes (1): ProcessedEvent

### Community 18 - "Product Controller"
Cohesion: 0.5
Nodes (1): ProductController

### Community 19 - "Authentication Controller"
Cohesion: 0.5
Nodes (1): AuthController

### Community 20 - "Frontend Utilities"
Cohesion: 0.67
Nodes (0): 

### Community 21 - "API Gateway Service"
Cohesion: 0.67
Nodes (1): ApiGatewayApplication

### Community 22 - "Payment Service Entry"
Cohesion: 0.67
Nodes (1): PaymentServiceApplication

### Community 23 - "Order Service Entry"
Cohesion: 0.67
Nodes (1): OrderServiceApplication

### Community 24 - "Event Deduplication Repo"
Cohesion: 0.67
Nodes (1): ProcessedEventRepository

### Community 25 - "Warehouse Service Entry"
Cohesion: 0.67
Nodes (1): WarehouseServiceApplication

### Community 26 - "User Service Entry"
Cohesion: 0.67
Nodes (1): UserServiceApplication

### Community 27 - "Event Envelope"
Cohesion: 1.0
Nodes (0): 

### Community 28 - "Frontend Root Layout"
Cohesion: 1.0
Nodes (0): 

### Community 29 - "Product Repository"
Cohesion: 1.0
Nodes (1): ProductRepository

### Community 30 - "Root Build Config"
Cohesion: 1.0
Nodes (0): 

### Community 31 - "Gradle Settings"
Cohesion: 1.0
Nodes (0): 

### Community 32 - "Observability Build"
Cohesion: 1.0
Nodes (0): 

### Community 33 - "Events Lib Build"
Cohesion: 1.0
Nodes (0): 

### Community 34 - "Order Created Payload"
Cohesion: 1.0
Nodes (0): 

### Community 35 - "Order Item Payload"
Cohesion: 1.0
Nodes (0): 

### Community 36 - "Payment Result Payload"
Cohesion: 1.0
Nodes (0): 

### Community 37 - "Stock Reserved Payload"
Cohesion: 1.0
Nodes (0): 

### Community 38 - "Stock Reservation Failed Payload"
Cohesion: 1.0
Nodes (0): 

### Community 39 - "Security Lib Build"
Cohesion: 1.0
Nodes (0): 

### Community 40 - "Tailwind Config"
Cohesion: 1.0
Nodes (0): 

### Community 41 - "Orval Code-Gen Config"
Cohesion: 1.0
Nodes (0): 

### Community 42 - "Vite Build Config"
Cohesion: 1.0
Nodes (0): 

### Community 43 - "PostCSS Config"
Cohesion: 1.0
Nodes (0): 

### Community 44 - "Frontend Entry Point"
Cohesion: 1.0
Nodes (0): 

### Community 45 - "Frontend Router"
Cohesion: 1.0
Nodes (0): 

### Community 46 - "Orders Page"
Cohesion: 1.0
Nodes (0): 

### Community 47 - "API Gateway Build"
Cohesion: 1.0
Nodes (0): 

### Community 48 - "Payment Service Build"
Cohesion: 1.0
Nodes (0): 

### Community 49 - "Order Service Build"
Cohesion: 1.0
Nodes (0): 

### Community 50 - "Order Status Enum"
Cohesion: 1.0
Nodes (0): 

### Community 51 - "Order Overview DTO"
Cohesion: 1.0
Nodes (0): 

### Community 52 - "Create Order Response"
Cohesion: 1.0
Nodes (0): 

### Community 53 - "Create Order Item Request"
Cohesion: 1.0
Nodes (0): 

### Community 54 - "Create Order Request"
Cohesion: 1.0
Nodes (0): 

### Community 55 - "Warehouse Service Build"
Cohesion: 1.0
Nodes (0): 

### Community 56 - "Product DTO"
Cohesion: 1.0
Nodes (0): 

### Community 57 - "User Service Build"
Cohesion: 1.0
Nodes (0): 

### Community 58 - "Login Request"
Cohesion: 1.0
Nodes (0): 

### Community 59 - "Login Response"
Cohesion: 1.0
Nodes (0): 

### Community 60 - "Payment Requested Event"
Cohesion: 1.0
Nodes (1): PaymentRequested Event

### Community 61 - "Order Cancelled Event"
Cohesion: 1.0
Nodes (1): OrderCancelled Event

### Community 62 - "Order Ready Event"
Cohesion: 1.0
Nodes (1): OrderReadyForPicking Event

## Knowledge Gaps
- **20 isolated node(s):** `ProductRepository`, `API Gateway`, `Warehouse Service`, `Product Model`, `StockReservationFailed Event` (+15 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Event Envelope`** (2 nodes): `create()`, `EventEnvelope.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Frontend Root Layout`** (2 nodes): `RootLayout.tsx`, `RootLayout()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Product Repository`** (2 nodes): `ProductRepository`, `ProductRepository.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Root Build Config`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Gradle Settings`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Observability Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Events Lib Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Created Payload`** (1 nodes): `OrderCreatedPayload.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Item Payload`** (1 nodes): `OrderItemPayload.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Payment Result Payload`** (1 nodes): `PaymentResultPayload.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stock Reserved Payload`** (1 nodes): `StockReservedPayload.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Stock Reservation Failed Payload`** (1 nodes): `StockReservationFailedPayload.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Security Lib Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Tailwind Config`** (1 nodes): `tailwind.config.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Orval Code-Gen Config`** (1 nodes): `orval.config.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Vite Build Config`** (1 nodes): `vite.config.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `PostCSS Config`** (1 nodes): `postcss.config.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Frontend Entry Point`** (1 nodes): `main.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Frontend Router`** (1 nodes): `router.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Orders Page`** (1 nodes): `OrdersPage.tsx`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `API Gateway Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Payment Service Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Service Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Status Enum`** (1 nodes): `OrderStatus.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Overview DTO`** (1 nodes): `OrderOverviewDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Create Order Response`** (1 nodes): `CreateOrderResponse.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Create Order Item Request`** (1 nodes): `CreateOrderItemRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Create Order Request`** (1 nodes): `CreateOrderRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Warehouse Service Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Product DTO`** (1 nodes): `ProductDto.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Service Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Login Request`** (1 nodes): `LoginRequest.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Login Response`** (1 nodes): `LoginResponse.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Payment Requested Event`** (1 nodes): `PaymentRequested Event`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Cancelled Event`** (1 nodes): `OrderCancelled Event`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Order Ready Event`** (1 nodes): `OrderReadyForPicking Event`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Are the 2 inferred relationships involving `Warehouse Service` (e.g. with `Minimonk` and `Flyway Migrations`) actually correct?**
  _`Warehouse Service` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ProductRepository`, `API Gateway`, `Warehouse Service` to the rest of the system?**
  _20 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Architecture & System Plan` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `RabbitMQ Configuration` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Order Domain Model` be split into smaller, more focused modules?**
  _Cohesion score 0.11 - nodes in this community are weakly interconnected._
- **Should `Frontend & User Model` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Order Service API Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.13 - nodes in this community are weakly interconnected._