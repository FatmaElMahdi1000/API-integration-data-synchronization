# Assessment Scope & Post-Feedback Clarifications

**Project:** Varthak Assessment API — User Synchronization Service
**Author:** Fatma El Mahdi
**Purpose:** Technical clarifications and responses to assessment feedback, kept separate from the user-facing [README.md](README.md).

This document consolidates the reasoning behind key implementation decisions and the changes made in response to review feedback. For the architectural deep-dive (ER diagrams, sequence flows, trade-offs), see [DESIGN.md](DESIGN.md).

---

## Table of Contents

1. [Pagination & Retry Clarification](#1-pagination--retry-clarification)
2. [Concurrency & Duplicate Handling Feedback](#2-concurrency--duplicate-handling-feedback)
3. [Mapper Correction](#3-mapper-correction)
4. [Transaction Rollback](#4-transaction-rollback)
5. [Customer Isolation](#5-customer-isolation)
6. [Failure Profile](#6-failure-profile)
7. [Flyway Migrations](#7-flyway-migrations)
8. [Oracle / Docker Decision](#8-oracle--docker-decision)
9. [UI Ambiguity](#9-ui-ambiguity)
10. [Additional Optimizations & Follow-up](#10-additional-optimizations--follow-up)

---

## 1. Pagination & Retry Clarification

**What was requested:** Traverse the external customer API's pages and handle transient network failures.

**Implementation:**

- `CustomerApiService.fetchAllPages()` iterates the external endpoint `GET /api/users?page={N}&limit=50` until `hasNextPage == false`.
- Each failed request is retried up to **3 attempts** with **exponential backoff** (`Thread.sleep(1500L * attempt)` → 1.5s, 3.0s).
- If all 3 attempts for a page fail, the sync operation throws and aborts cleanly (see [Transaction Rollback](#4-transaction-rollback)).

**Clarification:** The current implementation retries failed external requests as a whole. More granular classification of **transient vs. non-transient** HTTP failures (e.g., short-circuiting on `4xx` responses instead of retrying them) is a possible refinement. Retry classification remains an **optional follow-up improvement** — see [Section 10](#10-additional-optimizations--follow-up).

---

## 2. Concurrency & Duplicate Handling Feedback

**What was requested:** Ensure a user can exist only once in the database, and handle simultaneous synchronization attempts safely.

**Implementation — three layers of defense:**

1. **Intra-batch deduplication** — a `HashSet<String>` of external user IDs ensures each user payload is processed only once per sync run.
2. **Idempotent upsert** — `findByCustomerAndExternalUserId(customer, externalUserId)` is the normal lookup path: missing users are created, existing users updated.
3. **Database-level uniqueness** — a composite `UNIQUE` constraint on `(Customer_Id, EXTERNAL_USER_ID)` is the final guarantee against duplicates.

**Concurrent creation races:** when two synchronization runs create the same related/user record simultaneously, the insert is flushed and, on `DataIntegrityViolationException`, the implementation recovers by retrieving the record created by the competing transaction instead of failing.

**Verification:** concurrent synchronization was manually tested with two simultaneous requests; the resulting database contained **no duplicate** `(Customer_Id, EXTERNAL_USER_ID)` combinations.

---

## 3. Mapper Correction

**What changed:** The entity mapping approach was corrected to use **MapStruct** compile-time mappers instead of manual or reflection-based mapping.

**Implementation:**

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "id", target = "externalUserId")
    @Mapping(source = "createdAt", target = "externalCreatedAt")
    @Mapping(source = "updatedAt", target = "externalUpdatedAt")
    void MapExUserToEntity(ExUserDTO ExternalUserDTO, @MappingTarget User user);
}
```

**Rationale:** MapStruct generates plain, type-safe Java bytecode at compile time — zero runtime reflection overhead and no silent mapping drift when contracts change. External DTOs, JPA entities, and client-facing DTOs remain fully decoupled.

---

## 4. Transaction Rollback

**What was requested:** Synchronization must be atomic — a failed run must not leave partial data.

**Implementation:**

- `syncUsers()` is annotated with `@Transactional`.
- The entire pipeline (fetch → resolve → upsert → deactivate) commits as a single unit.
- If an unhandled failure occurs mid-sync, the transaction rolls back, preventing corrupt partial imports.

**Failure-path ordering:** the external fetch completes and is validated **before** any records are modified. If the fetch fails, the endpoint returns `HTTP 502 BAD_GATEWAY` and no `INSERT`/`UPDATE`/`DELETE` has been executed.

**Verification:** a failed synchronization leaves existing data intact.

---

## 5. Customer Isolation

**What was requested:** Data must not leak across customers.

**Implementation:**

- Every `User` and `Company` record carries a non-nullable `Customer_Id` foreign key back to `Internal_Customer`.
- Uniqueness is enforced **per tenant** via the composite `UNIQUE (Customer_Id, EXTERNAL_USER_ID)` constraint.
- Query endpoints scope results by `customerId`; a user/company from one tenant can never be returned for another.
- The `Customer_Id` is resolved from the registered customer at sync time, never taken from the external payload.

---

## 6. Failure Profile

**What was requested:** Test failure handling without breaking the default configuration.

**Implementation:** a dedicated `test-failure` Spring profile:

| Profile        | Port | External API                                   |
|----------------|------|------------------------------------------------|
| `default`      | 3030 | `https://assessment-api-gamma.vercel.app`      |
| `test-failure` | 2020 | `https://assessment-api-gamma.vercel.app/invalid` |

The profile points at an intentionally invalid external API path, so `502 BAD_GATEWAY` handling can be exercised without modifying the committed `dev` configuration. This is a deliberate, legitimate engineering decision — not part of the normal assessor setup.

---

## 7. Flyway Migrations

**What changed:** Schema management was moved to Flyway with auto-migration on startup.

**Implementation:**

- Migration script: `src/main/resources/db/migration/V1__create_schema.sql`.
- Applies to the `VARTHAK_APP` schema on application startup.
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate validates entities against the migrated schema and never modifies it.

**Clarification:** Flyway validates and applies only **pending** migrations on startup; already-applied migrations are left untouched. It does not re-create the schema on every boot.

**Startup race fix:** the application container uses `depends_on: condition: service_healthy` against Oracle's `healthcheck.sh`, so the app (and its migrations) only starts after Oracle is accepting connections.

---

## 8. Oracle / Docker Decision

**What was requested:** A production-grade relational persistence layer, reproducibly.

**Decisions:**

- **Oracle XE 21c** was chosen over an embedded (e.g., H2) database for industrial-grade ACID compliance, constraints enforcement, and closer alignment with enterprise production systems.
- **`gvenzl/oracle-xe:slim-faststart`** container image with auto-provisioned `VARTHAK_APP` user.
- **Health-gated startup** via `healthcheck.sh` + `depends_on: service_healthy` to eliminate the database-not-ready race.
- **Multi-stage Docker build** — Maven/Temurin 21 builder produces the JAR; a slim `eclipse-temurin:21-jre` runtime stage runs it (`EXPOSE 3030`).
- **Offline packaging** — pre-built images exported as `oracle-db.tar` and `varthak-app.tar`, loadable via `docker load` on machines with no internet access (see README, Option B).

---

## 9. UI Ambiguity

**What was clarified:** The assessment centers on **backend/architecture quality** — layered separation, data synchronization, and integration patterns — with UI deliberately out of scope.

**Resolution:** all interaction is exposed as clean REST APIs with auto-generated **Swagger UI** for discovery and testing. No UI framework is included; the assessor exercises the system through Swagger or cURL.

---

## 10. Additional Optimizations & Follow-up

Current implementation strengths that could be taken further in production:

1. **Retry classification** *(optional follow-up)*: the current implementation retries failed external requests; more granular classification of **transient vs. non-transient** HTTP failures (retrying `5xx`/timeouts, short-circuiting `4xx`) could be added in a future iteration.
2. **Asynchronous processing**: `POST /api/sync/users` could return `202 Accepted` with a `jobId`, decoupling long ingests from the HTTP request.
3. **Batch writes**: chunked `saveAll()` with JDBC batch sizing to further reduce roundtrips during large imports.
4. **Distributed locking (Redisson)**: prevent inter-node sync races in a multi-instance deployment.
5. **Data purge API**: a cleanup endpoint for permanently removing soft-deleted records (GDPR / compliance).

These are roadmap items only — the implementation as delivered is complete for the assessment scope, is idempotent, transactional, and containerized end-to-end.