# Varthak Assessment API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle XE](https://img.shields.io/badge/Database-Oracle%20XE%2021c-red.svg)](https://www.oracle.com/database/)
[![Flyway](https://img.shields.io/badge/DB%20Migration-Flyway-CC3333.svg)](https://flywaydb.org/)
[![MapStruct](https://img.shields.io/badge/Mapping-MapStruct-blue.svg)](https://mapstruct.org/)
[![Swagger](https://img.shields.io/badge/API%20Docs-Swagger%20UI-green.svg)](https://swagger.io/)
[![Docker](https://img.shields.io/badge/Container-Docker%20Compose-2496ED.svg)](https://www.docker.com/)

A production-grade Spring Boot backend that integrates with an external customer identity API, synchronizes user and organization data into a local Oracle XE 21c relational database, enforces strict deduplication, reconciles deactivated records, and exposes clean, decoupled REST endpoints — all wrapped in a minimal, reproducible Docker Compose footprint.

This project is a **Varthak technical assessment** focused on **layered software architecture**: clean separation of concerns, robust data synchronization, and integration patterns, with deliberately minimal UI overhead.

---

## Table of Contents

- [1. Project Overview & Architecture](#1-project-overview--architecture)
- [2. Key Features & Fixes Implemented](#2-key-features--fixes-implemented)
- [3. Technology Stack](#3-technology-stack)
- [4. Repository Layout](#4-repository-layout)
- [5. Prerequisites](#5-prerequisites)
- [6. Quick Start (For the Assessor)](#6-quick-start-for-the-assessor)
- [7. Deployment Options](#7-deployment-options)
  - [Option A — Standard Docker Compose (Source Build)](#option-a--standard-docker-compose-source-build)
  - [Option B — Offline Tar Package (Pre-built Images)](#option-b--offline-tar-package-pre-built-images)
- [8. Endpoints & API Access](#8-endpoints--api-access)
- [9. Synchronization Behavior](#9-synchronization-behavior)
- [10. Concurrency & Data Integrity](#10-concurrency--data-integrity)
- [11. Database Management & Migrations](#11-database-management--migrations)
- [12. Configuration Reference](#12-configuration-reference)
- [13. Failure Testing](#13-failure-testing)
- [14. Application Teardown](#14-application-teardown)
- [15. Testing](#15-testing)
- [16. Documents](#16-documents)
- [Author](#author)

---

## 1. Project Overview & Architecture

**Application Name:** Varthak Assessment API

The service acts as an **integration gateway** that:

1. **Registers customer organizations** as isolated tenants.
2. **Ingests paginated user profiles** from an external customer API (`https://assessment-api-gamma.vercel.app/`) using retries and exponential backoff.
3. **Normalizes and persists** the payload into a normalized Oracle schema, strictly decoupling external contracts from internal domain models.
4. **Reconciles state** so users removed upstream are soft-deactivated rather than hard-deleted, preserving audit history.

### Architectural Philosophy

- **Layered / Clean Architecture** — strict decoupling between the **API Controller layer**, **Business/Service layer**, **Persistence (Repository) layer**, and **DTO Mappers**.
- **DTO Isolation Boundary** — the external payload DTOs (`ExUserDTO`, `ExCompanyDTO`, `ExPaginationDTO`) and JPA entities (`User`, `Company`, `Role`, `Status`, `Customer`) are never leaked to REST consumers; compile-time **MapStruct** mappings bridge the layers.
- **Multi-Tenant partitioning** — every synced record is scoped to a parent `Customer` UUID, preventing cross-tenant data pollution.
- **Idempotency by default** — synchronization is safely retryable without duplicate rows or false-positive errors.
- **Non-destructive reconciliation** — upstream deletions map to `active = 0` (soft deactivation), never destructive `DELETE`.

```
                       External Customer API
                   (assessment-api-gamma.vercel.app)
                                 |
                     [GET /api/users?page=N&limit=50]
                                 v
              CustomerApiService  (RestClient + Retry/Backoff)
                                 |
                        [List<ExUserDTO>]
                                 v
       SynchronizeUser Service  (@Transactional)           <- Business Layer
        - HashSet dedup + Entity resolution caches
        - MapStruct user mapping
        - Missing-user soft deactivation
                                 |
                     [JPA Repositories]
                                 v
                       Oracle XE 21c  (VARTHAK_APP schema)
                                 |
                     [UserService / Controllers]
                                 |
                        REST Clients / Swagger UI
```

Full end-to-end diagrams (architecture, ER model, sequence flows) live in [DESIGN.md](DESIGN.md).

---

## 2. Key Features & Fixes Implemented

### Layered Architecture
- Strict decoupling between **Controllers**, **Services**, **Repositories**, and **DTO Mappers**.
- External API contracts, JPA entities, and client-facing DTOs are three isolated models connected only through MapStruct compile-time mappers — zero runtime reflection.

### Database Connection & Sync Fixes
- **Oracle XE 21c container startup synchronization** fixed via **Flyway auto-migrations** against the `VARTHAK_APP` schema, eliminating race conditions where the application started before the database was ready.
- The Compose file uses `depends_on: condition: service_healthy` with the Oracle container's `healthcheck.sh`, so the app only boots once Oracle is fully accepting connections.
- Flyway automatically **validates and applies any pending database migrations** when the application starts — no manual SQL execution required.

### Port Mapping & Health Checks
- **Standardized execution on Port `3030`** for the application, with Oracle on `1521`.
- **HikariCP** connection pooling and Spring Data JPA auto-configuration provide an optimized, production-ready datasource out of the box.
- `spring.jpa.hibernate.ddl-auto=validate` guarantees Hibernate validates entities against the Flyway-managed schema without ever modifying it.

### Containerization Optimizations
- **Multi-stage Docker builds** — a Maven/Temurin 21 builder stage compiles the JAR, which is then copied into a slim `eclipse-temurin:21-jre` runtime image.
- **Complete offline packaging** — pre-built images can be exported as `tar` archives and deployed on a machine with **zero internet access** (see [Option B](#option-b--offline-tar-package-pre-built-images)).

### Core Business Behavior (non-UI)
- **Intra-batch deduplication** via `HashSet<String>` of external user IDs.
- **Database-level idempotent upsert** (`findByCustomerAndExternalUserId`) — creates new users, updates changed ones, reactivates previously inactive ones.
- **N+1 query elimination** — in-run `HashMap` caches resolve `Company`, `Role`, and `Status` in O(1) instead of one roundtrip per user.
- **Soft deactivation** of users absent from the latest upstream pull.

---

## 3. Technology Stack

| Layer / Concern            | Technology                                      | Version          |
|----------------------------|-------------------------------------------------|------------------|
| Language                   | Java                                            | 21 (LTS)         |
| Framework                  | Spring Boot                                     | 4.1.1            |
| HTTP Client                | Spring `RestClient`                             | (Spring Framework 7) |
| Persistence                | Spring Data JPA / Hibernate                     | —                |
| Database                   | Oracle Database XE                             | 21c (`gvenzl/oracle-xe:slim-faststart`) |
| DB Migrations              | Flyway                                          | (Spring Boot-managed) |
| Oracle JDBC Driver         | `ojdbc11`                                       | 23.9.0.25.07     |
| Object Mapping             | MapStruct                                       | 1.7.0            |
| API Documentation          | SpringDoc OpenAPI / Swagger UI                  | 3.1.0            |
| Build                      | Maven + Maven Wrapper                           | 3.9+             |
| Containerization           | Docker & Docker Compose                         | —                |

---

## 4. Repository Layout

```text
.
├── src/main/java/com/example/varthakassesment/
│   ├── Client/                 # RestClient integration + auth config
│   ├── Controller/             # CustomerController, SyncController, UserController
│   ├── DTO/
│   │   ├── External/           # Upstream API contracts (ExUserDTO, ExCompanyDTO, ExPaginationDTO)
│   │   └── Internal/           # Client-facing contracts (UserDTO, CustomerDTO, SyncResponseDTO, ...)
│   ├── Mapper/                 # MapStruct compile-time mappers
│   ├── Model/                  # JPA entities (User, Company, Role, Status, Customer)
│   ├── Repo/                   # Spring Data JPA repositories
│   ├── Response/               # Uniform GeneralResponse<T> envelope
│   └── Service/                # CustomerService, SynchronizeUser, UserService
├── src/main/resources/db/migration/   # Flyway scripts (V1__create_schema.sql)
├── src/test/java/.../Services/        # Unit tests for the three core services
├── docker-compose.yml         # Orchestration (Oracle + App)
├── Dockerfile                 # Multi-stage build
├── load_and_run.bat           # Offline one-click Windows launcher
├── oracle-db.tar              # Pre-built Oracle XE image (offline mode)
└── varthak-app.tar            # Pre-built application image (offline mode)
```

---

## 5. Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Docker Engine with Compose v2).
  - Windows / macOS: Docker Desktop installed and **running**.
  - Linux: Docker Engine + `docker compose` plugin.
- ~6–8 GB of free disk space (the Oracle XE image is large).
- Internet connection — **only** for [Option A](#option-a--standard-docker-compose-source-build), which pulls the Maven build dependencies and base images. [Option B](#option-b--offline-tar-package-pre-built-images) works fully offline.

> No local Java/Maven installation is required — everything runs inside containers.

---

## 6. Quick Start (For the Assessor)

The fastest path from clean machine to a synced database:

1. **Clone this repository.**
2. **Set the external API token** (used to authenticate against the customer API):

   ```powershell
   # PowerShell
   $env:CUSTOMER_API_TOKEN="your_bearer_token_here"
   ```

   ```bash
   # Linux / macOS
   export CUSTOMER_API_TOKEN="your_bearer_token_here"
   ```

3. **Build and start the stack:**

   ```bash
   docker compose up --build -d
   ```

4. **Wait for Oracle's health check and Flyway migration.** Oracle's first boot takes 1–3 minutes; the application container waits for the `service_healthy` condition and then applies the Flyway schema migration automatically. Confirm both services are up:

   ```bash
   docker compose ps
   ```

5. **Open Swagger UI:** <http://localhost:3030/swagger-ui.html>
6. **Create a customer** (a `customerID` is returned — copy it for the next steps).
7. **Synchronize users** for that customer ID.
8. **Retrieve users** (optionally filtered by company).

> **Important:** Customer IDs are **generated when a customer is created**. Use the `customerID` returned by the create response for all subsequent synchronization and retrieval requests — there is no pre-existing, fixed customer ID.

The full requests for steps 6–8 are shown in [Section 8](#8-endpoints--api-access).

---

## 7. Deployment Options

### Option A — Standard Docker Compose (Source Build)

Builds the application image from source and starts both containers. Oracle's health check (`healthcheck.sh`) is used to guarantee the app only starts after the database is ready.

```bash
docker compose up --build -d
```

This starts two services:

| Service      | Image                              | Port      | Purpose                       |
|--------------|------------------------------------|-----------|-------------------------------|
| `oracle-db`  | `gvenzl/oracle-xe:slim-faststart`  | `1521`    | Oracle XE 21c (service `XEPDB1`) |
| `app`        | `varthak-assesment-app:latest`     | `3030`    | Spring Boot application       |

### Option B — Offline Tar Package (Pre-built Images)

For environments **without internet access**, the deliverable includes fully pre-built images:

| File                 | Description                                      |
|----------------------|--------------------------------------------------|
| `oracle-db.tar`      | Pre-built `gvenzl/oracle-xe:slim-faststart` image |
| `varthak-app.tar`    | Pre-built `varthak-assesment-app:latest` image     |
| `docker-compose.yml` | Service orchestration                            |
| `load_and_run.bat`   | One-click Windows launcher                       |

#### Windows

**Option 1 — Double-click** `load_and_run.bat`.

**Option 2 — Run manually in PowerShell or Command Prompt:**

```powershell
.\load_and_run.bat
```

#### Linux / macOS

```bash
docker load -i oracle-db.tar
docker load -i varthak-app.tar
docker compose up -d
```

The launcher/scaffold:
1. Loads the Oracle XE image from `oracle-db.tar`.
2. Loads the application image from `varthak-app.tar`.
3. Starts the stack with `docker compose up -d --no-build` (explicitly **no build**, so nothing is fetched from the network).

> The first Oracle boot can take 1–3 minutes as it initializes the instance. The app waits for Oracle's health check before running the Flyway migration.

---

## 8. Endpoints & API Access

Once both containers are healthy:

| Resource                       | URL                                |
|--------------------------------|------------------------------------|
| Base URL                       | `http://localhost:3030`            |
| Interactive Swagger UI         | `http://localhost:3030/swagger-ui.html` |
| OpenAPI Documentation Spec     | `http://localhost:3030/v3/api-docs` |

### Core Endpoints

| Method | Path                       | Description                                                  |
|--------|----------------------------|--------------------------------------------------------------|
| `POST` | `/api/customers`           | Registers a customer organization (tenant) and returns a new `customerID`. |
| `POST` | `/api/sync/users`          | Triggers ingestion, deduplication & reconciliation for a customer. |
| `GET`  | `/api/v1/users`            | Retrieves all synchronized users for a customer.             |
| `GET`  | `/api/v1/users?company=X`  | Filters users by company (case-insensitive substring match). |

All responses are wrapped in a standardized `GeneralResponse<T>` envelope:

```json
{
  "response": "OK | CREATED | BAD_REQUEST | CONFLICT | BAD_GATEWAY | INTERNAL_SERVER_ERROR",
  "message": "Human-readable status description",
  "data": { }
}
```

### Step-by-Step Flow

**Step 1 — Create a customer:**

```bash
curl -X POST http://localhost:3030/api/customers \
  -H "Content-Type: application/json" \
  -d '{"customerName":"assessment-api-gamma"}'
```

```json
{
  "response": "CREATED",
  "message": "Customer has been created successfully",
  "data": {
    "customerID": "40ac987a-f34b-43dc-9a1a-1d5e52d662c3",
    "customerName": "assessment-api-gamma",
    "usersIds": [],
    "companiesIds": []
  }
}
```

**Step 2 — Copy the `customerID` returned above, then synchronize users for it:**

```bash
curl -X POST http://localhost:3030/api/sync/users \
  -H "Content-Type: application/json" \
  -d '"<CUSTOMER_ID>"'
```

The first synchronization against the **current live dataset** produces approximately:

```json
{
  "response": "OK",
  "message": "User synchronization completed successfully",
  "data": { "created": 130, "updated": 0, "deactivated": 0 }
}
```

> That count reflects the dataset verified at the time of writing and is **not guaranteed** — it may vary as the external dataset changes. A second synchronization of the same data is expected to report `0 created / 0 updated / 0 deactivated` (idempotent).

**Step 3 — Retrieve users (optionally filtered by company):**

```bash
curl "http://localhost:3030/api/v1/users?customerId=<CUSTOMER_ID>"
curl "http://localhost:3030/api/v1/users?customerId=<CUSTOMER_ID>&company=Google"
```

---

## 9. Synchronization Behavior

For each customer, `POST /api/sync/users` executes a deterministic pipeline:

1. **Fetch the external users** (paginated, with retry/backoff up to 3 attempts).
2. **If the external fetch fails**, the endpoint returns `HTTP 502 BAD_GATEWAY` **before modifying any synchronization data**.
3. **Resolve `Company`, `Role`, and `Status`** within the customer context (in-memory caches avoid redundant roundtrips).
4. **Look up each user** by `(Customer_Id, EXTERNAL_USER_ID)`.
5. **Create** missing users.
6. **Update** users whose `externalUpdatedAt` changed.
7. **Reactivate** previously inactive users that reappear in the latest dataset.
8. **Soft-deactivate** (`active = 0`) previously active users missing from the latest successful external dataset.

### Uniqueness Guarantee

A database **`UNIQUE` constraint on `(Customer_Id, EXTERNAL_USER_ID)`** provides the final protection against duplicate users. Application-level lookup is the normal path, while database constraint violations are handled for concurrent creation races (see [Section 10](#10-concurrency--data-integrity)).

---

## 10. Concurrency & Data Integrity

User uniqueness is enforced at the **database level** through a composite `UNIQUE` constraint on `(Customer_Id, EXTERNAL_USER_ID)`. Related entity creation also handles concurrent creation races by flushing the insert and recovering from `DataIntegrityViolationException` by retrieving the record created by the competing synchronization.

Concurrent synchronization was **manually tested with two simultaneous requests**; the resulting database contained **no duplicate** `(Customer_Id, EXTERNAL_USER_ID)` combinations.

---

## 11. Database Management & Migrations

- **Engine:** Oracle Database XE 21c — container service `XEPDB1`, port `1521`.
- **Schema:** `VARTHAK_APP` (Flyway-managed, created automatically).
- **Automation:** Flyway automatically **validates and applies any pending migrations** on application startup — **no manual DDL required**. Already-applied migrations are left untouched.
- **Credentials (Compose):** user `VARTHAK_APP` / password `app123` (local development only).
- **Validation:** `spring.jpa.hibernate.ddl-auto=validate`, so Hibernate only validates entities against the migrated schema and never alters it.

### Schema Tables (`V1__create_schema.sql`)

| Table               | Purpose                                          |
|---------------------|--------------------------------------------------|
| `Internal_Customer` | Tenant / customer organizations                  |
| `Internal_Company`  | Company / department records per tenant          |
| `InternalROLE`      | Role lookup table (`Developer`, `Admin`, ...)    |
| `Internal_STATUS`   | Lifecycle status lookup (`Active`, `Suspended`, ...) |
| `InternalUSER`      | Synchronized user records                        |

Key constraints:
- Primary keys are `RAW(16)` UUIDs generated via Hibernate `GenerationType.UUID`.
- **Unique composite constraint** on `(Customer_Id, EXTERNAL_USER_ID)` enforces per-tenant user uniqueness at the database level.
- `active` (`NUMBER(1) DEFAULT 1`) enables non-destructive soft deactivation.

---

## 12. Configuration Reference

Environment variables injected by `docker-compose.yml` (override the local defaults in `application.properties`):

| Variable                          | Value                                              | Purpose                        |
|-----------------------------------|----------------------------------------------------|--------------------------------|
| `SPRING_DATASOURCE_URL`           | `jdbc:oracle:thin:@oracle-db:1521/XEPDB1`          | Oracle JDBC URL (compose DNS)  |
| `SPRING_DATASOURCE_USERNAME`      | `VARTHAK_APP`                                      | Database user                 |
| `SPRING_DATASOURCE_PASSWORD`      | `app123`                                           | Database password             |
| `CUSTOMER_API_TOKEN`              | `<token>`                                          | Bearer token for the external customer API |
| `ORACLE_PASSWORD`                 | `NewPassword`                                      | Oracle SYS password (container provisioning) |
| `APP_USER`                        | `VARTHAK_APP`                                      | Auto-created app schema user  |
| `APP_USER_PASSWORD`               | `app123`                                           | App user password            |

**Profiles** (set via `spring.profiles.active`):

| Profile        | Port | API Base URL                                | Use Case                       |
|----------------|------|---------------------------------------------|--------------------------------|
| `dev` (default)| 3030 | `https://assessment-api-gamma.vercel.app`   | Local development / assessment |
| `test-failure` | 2020 | `https://assessment-api-gamma.vercel.app/invalid` | Intentionally invalid external API (see [Section 13](#13-failure-testing)) |

---

## 13. Failure Testing

The default `dev` profile uses the **valid** external API URL. A separate `test-failure` profile points to an **intentionally invalid** external API path, so failure handling can be exercised without modifying the committed default configuration.

| Profile        | Port | External API                                  |
|----------------|------|-----------------------------------------------|
| `default`      | 3030 | `https://assessment-api-gamma.vercel.app`     |
| `test-failure` | 2020 | `https://assessment-api-gamma.vercel.app/invalid` |

To exercise it, run the app with `spring.profiles.active=test-failure` and trigger a sync.

**Verified behavior:**

```
External API failure
        ↓
HTTP 502 BAD_GATEWAY
        ↓
No partial synchronization
```

A failed upstream fetch aborts **before** any records are created, updated, or deactivated, and the `@Transactional` boundary rolls back cleanly.

---

## 14. Application Teardown

**Graceful shutdown** (stops containers, keeps named volumes/data):

```bash
docker compose down
```

**Full reset** (stops containers **and** removes the Oracle data volume — wipes all data and any previously created customer IDs):

```bash
docker compose down -v
```

To remove the locally loaded offline images as well:

```bash
docker rmi varthak-assesment-app:latest gvenzl/oracle-xe:slim-faststart
```

---

## 15. Testing

### Automated Unit Tests

```bash
mvn test
```

Coverage includes:
- External API pagination / null handling.
- User creation.
- User update detection.
- Idempotent second synchronization.
- Missing-user deactivation.
- Reactivation of inactive users.
- Customer / user service behavior.
- Failure scenarios.

| Test Class                      | Coverage                                              |
|---------------------------------|-------------------------------------------------------|
| `CustomerAPiServiceTest`        | External API pagination, retry/backoff, null handling |
| `SynchronizeUserServiceTest`    | Full sync lifecycle: dedup, create, update, deactivate |
| `UserServiceTest`               | User retrieval and company-based filtering            |

### Manually Verified Scenarios

| Scenario                           | Result                                        |
|------------------------------------|-----------------------------------------------|
| Initial synchronization            | 130 users created                             |
| Second synchronization             | 0 created / 0 updated / 0 deactivated        |
| Concurrent synchronization         | No duplicate users                           |
| Unknown customer                   | HTTP 404                                    |
| External API failure               | HTTP 502                                    |
| Failed sync leaves existing data intact | PASS                                     |
| Missing external user              | Soft-deactivated                            |
| Customer-scoped retrieval          | Verified                                    |
| Flyway migration                   | Applied successfully                        |
| Docker startup                     | Verified                                    |

---

## 16. Documents

- **[DESIGN.md](DESIGN.md)** — Detailed architecture: ER diagrams, sequence flows, deduplication strategy (409 vs. idempotent upsert), N+1 query solution, soft-delete rationale, multi-tenant scoping, and the production scalability roadmap.

---

## Author

**Fatma El Mahdi**