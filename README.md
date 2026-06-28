# 🎟️ Marketplace — Multi-Database Persistence Study with Lock and Cache

Project built during the **Santander Bootcamp on [DIO](https://www.dio.me/)**, focused on exploring how a Spring Boot application can manage multiple databases simultaneously, with **Redis-based caching**, **distributed locking**, and **inter-module communication via internal events**.

---

## 🧭 Overview

The application simulates a ticket marketplace split into three well-defined bounded contexts:

| Module | Responsibility | Database |
|---|---|---|
| **Registration** | Customer management | MySQL (port 3307) |
| **Catalog** | Event showcase with enriched metadata | MySQL (port 3308) + MongoDB + Redis (cache) |
| **Ticketing** | Seat reservation with concurrency control | PostgreSQL + Redis (lock) |

Each module has its own `DataSource`, `EntityManagerFactory`, and `TransactionManager`, configured manually — without relying on Spring Boot's autoconfiguration.

---

## 🏗️ Architecture

The project follows **Hexagonal Architecture** (Ports & Adapters) principles, clearly separating:

- **Domain** — entities and repository interfaces (no framework dependencies)
- **Application** — use cases orchestrating business rules
- **Infrastructure** — concrete implementations: JPA, MongoDB, Redis, HTTP controllers, event listeners

Inter-module communication happens through **Spring's Application Events** (`ApplicationEventPublisher`), keeping modules decoupled without the need for an external message broker.

```
src/main/java/com/estudojava/marketplace/
├── common/              # Shared event DTOs across modules
├── registration/        # Customer registration context
├── catalog/             # Event showcase context
└── ticketing/           # Seat reservation context
```

---

## 🔧 Tech Stack

- **Java 25** with Virtual Threads (`spring.threads.virtual.enabled=true`)
- **Spring Boot 4.1**
- **Spring Data JPA** — Hibernate with multiple `EntityManager` instances
- **Spring Data MongoDB** — event metadata storage
- **Spring Data Redis** — showcase cache and seat locking (via Jedis)
- **Spring Data REST** — automatic REST CRUD for JPA entities
- **HikariCP** — dedicated connection pool per DataSource
- **MySQL 9.6** (two databases: registration and catalog)
- **PostgreSQL 18** (ticketing database)
- **MongoDB 8.2** (catalog metadata)
- **Redis 8.6** (two separate instances: cache and lock)
- **Docker Compose** to spin up the full infrastructure locally
- **Lombok** to reduce boilerplate

---

## 📦 Modules in Detail

### Registration

Manages customer registration. Uses **Spring Data REST** to automatically expose CRUD endpoints over the JPA repository.

When a customer is created or updated, a `@RepositoryEventHandler` intercepts the Spring Data REST lifecycle event and publishes a `CustomerCreated` internal event to the other modules.

**Flow:**
```
POST /customers (via Spring Data REST)
  → CustomerEventHandler.afterCreate()
  → publisher.publishEvent(CustomerCreated)
  → [Ticketing receives it and replicates the customer to its own database]
```

---

### Catalog

Displays the event showcase by combining data from two different sources:

- **MySQL** — structured event data (title, date)
- **MongoDB** — rich metadata (description, technical requirements, seat map by sector)

The `BrowseShowcaseUsecase` fetches events from MySQL and, for each one, enriches the data with MongoDB metadata **asynchronously** (`@Async` + `CompletableFuture`). The final result is **cached in Redis** with `@Cacheable`.

**Flow:**
```
GET /showcase
  → BrowseShowcaseUsecase.execute()
  → [Redis cache hit? Return immediately]
  → [Cache miss?]
      → JpaEventRepository.findAll() (MySQL)
      → EventEnricher.enrich() for each event (async, MongoDB)
      → CompletableFuture.join() to consolidate results
      → Serialized result saved to Redis
```

When an `EventMetadata` document is saved to MongoDB, the `EventMetadataEventListener` publishes an `EventUpdated` event, which replicates structured data to the Ticketing module.

---

### Ticketing

Manages seat reservations with distributed concurrency control using Redis.

**Reservation flow:**
```
POST /ticketing/events/{eventId}/seats/select
  + Header: X-CUSTOMER-ID
  + Body: { seatId }
  → SelectSeatUseCase.execute()
  → Checks if the seat exists in PostgreSQL
  → Tries to acquire a lock in Redis (SET NX, 30s TTL)
  → Success: 201 Created
  → Lock already exists: SeatAlreadyReservedException (conflict)
```

The lock is represented by the `SeatLock` entity annotated with `@RedisHash(timeToLive = 30)`, meaning it **expires automatically after 30 seconds** — preventing eternal locks in case of failure.

The `WorkOfUnitEventRepository` acts as a composite repository, coordinating operations between the `EventCrudRepository` (PostgreSQL/JPA) and the `RedisSeatLockRepository`.

---

## ⚙️ Multi-DataSource Configuration

One of the core learnings of this project is how to configure multiple databases in the same Spring Boot application without autoconfiguration. Each module has its own `@Configuration` class defining:

- `DataSourceProperties` — reads properties from `application.properties`
- `HikariDataSource` — dedicated connection pool
- `LocalContainerEntityManagerFactoryBean` — isolated EntityManager per module
- `JpaTransactionManager` — independent transactions per context

The `registration` module is marked as `@Primary` so Spring knows which one to use as default. The other modules use `@Qualifier("catalog")` and `@Qualifier("ticketing")` to isolate their beans.

---

## 🚀 Getting Started

**Prerequisites:** Docker, Java 25, Gradle

```bash
# Clone the repository
git clone <repo-url>
cd marketplace

# Start the infrastructure (databases and Redis)
docker compose up -d

# Run the application
./gradlew bootRun
```

Spring Boot automatically detects the `compose.yml` and waits for all container healthchecks to pass before starting (`spring.docker.compose.lifecycle-management=start_only`).

---

## 🌐 Endpoints

| Method | Endpoint | Module | Description |
|---|---|---|---|
| `GET` | `/showcase` | Catalog | List events with metadata (Redis cache) |
| `POST` | `/ticketing/events/{eventId}/seats/select` | Ticketing | Reserve a seat (Redis lock) |
| `*` | `/customers/**` | Registration | Customer CRUD via Spring Data REST |
| `GET` | `/actuator/health` | — | Health check with details for all datasources |

> The HAL Explorer is available at `/` to navigate Spring Data REST resources.

---

## 📚 Concepts Explored

- Manual configuration of multiple `DataSource` instances with HikariCP in Spring Boot
- Multiple `EntityManagerFactory` and `TransactionManager` coexisting in the same JVM
- Distributed cache with Redis and `@Cacheable` (result must implement `Serializable`)
- Distributed lock with Redis using `@RedisHash` with automatic TTL
- Asynchronous inter-module communication via `ApplicationEventPublisher` without an external broker
- Parallel data enrichment with `@Async` and `CompletableFuture`
- Hexagonal Architecture with domain, application, and infrastructure separation
- Spring Data REST + `@RepositoryEventHandler` for CRUD lifecycle hooks
- Java 25 Virtual Threads enabled for better I/O throughput
