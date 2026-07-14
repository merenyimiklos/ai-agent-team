# UgorjBe MVP architecture

**Status:** Frozen for MVP implementation  
**Date:** 2026-07-14  
**Product decision:** [PRODUCT_DECISION.md](PRODUCT_DECISION.md)  
**Wire contract:** [API_CONTRACT.md](API_CONTRACT.md)

This document is the shared implementation boundary for the ASP.NET Core API and the native Android client. Changes to a frozen decision require updating both documents and notifying both implementers before code changes.

## 1. Scope and architectural goals

UgorjBe is a Budapest-first marketplace for discounted, same-day empty places in family activities. The MVP proves one customer flow: authenticate, discover a suitable live offer, inspect it, reserve capacity, and receive a booking code. It also supports booking history, allowed cancellation, and favorite offers/providers.

The architecture optimizes for a buildable local MVP:

- one ASP.NET Core 8 modular monolith and one PostgreSQL database;
- one native Kotlin/Jetpack Compose Android application;
- synchronous JSON/HTTPS APIs only;
- pay on arrival; no payment provider or financial ledger;
- seeded providers and offers; no provider/admin application;
- no message broker, distributed cache, background worker, microservice, or cloud dependency.

The API is the authority for offer validity, price, capacity, booking status, and cancellation eligibility. Android must never infer that a reservation succeeded from cached data.

## 2. Repository structure

```text
/
|-- backend/
|   |-- UgorjBe.sln
|   |-- src/
|   |   |-- UgorjBe.Api/              # HTTP composition root, middleware, endpoints
|   |   |-- UgorjBe.Application/      # use cases, DTO mapping, ports, validation
|   |   |-- UgorjBe.Domain/           # entities, value rules, enums; no EF/HTTP refs
|   |   `-- UgorjBe.Infrastructure/   # EF Core, Npgsql, JWT, password hashing, seed
|   `-- tests/
|       |-- UgorjBe.UnitTests/
|       `-- UgorjBe.IntegrationTests/
|-- android/
|   |-- settings.gradle.kts
|   |-- build.gradle.kts
|   |-- gradle.properties
|   `-- app/
|       |-- build.gradle.kts
|       `-- src/
|           |-- main/java/hu/ugorjbe/app/
|           |   |-- data/             # Retrofit DTOs, API, repositories, token store
|           |   |-- domain/           # UI-facing models and repository interfaces
|           |   |-- di/               # Hilt modules
|           |   `-- ui/               # navigation, screens, ViewModels, theme
|           `-- test/                 # ViewModel/repository unit tests
|-- docs/
|   |-- PRODUCT_DECISION.md
|   |-- ARCHITECTURE.md
|   `-- API_CONTRACT.md
|-- docker-compose.yml
|-- .env.example
`-- README.md
```

Projects form a one-way dependency graph:

```text
Api -> Application -> Domain
Api -> Infrastructure -> Application + Domain
```

`Domain` has no ASP.NET Core, EF Core, JSON, or Npgsql references. `Application` defines use cases and ports. `Infrastructure` implements persistence, authentication, clock, and seeding. `Api` wires dependencies and translates use-case results into the contract. Modules are folders/namespaces inside this monolith: Identity, Catalog, Bookings, and Favorites. They share one EF `DbContext` and database transaction boundary in the MVP.

## 3. Backend runtime and dependencies

- .NET 8 SDK and ASP.NET Core Web API.
- EF Core 8 with `Npgsql.EntityFrameworkCore.PostgreSQL` 8.x.
- ASP.NET Core JWT bearer authentication.
- ASP.NET Core `PasswordHasher<TUser>` for salted, iterated password hashes.
- Built-in OpenAPI/Swagger generation and health checks.
- xUnit for unit and integration tests. Integration tests run against PostgreSQL (Testcontainers where Docker is available, otherwise the documented test database connection string).
- `TimeProvider` is injected wherever current time is needed so expiry rules are deterministic in tests.

Package versions must remain compatible with .NET 8. Do not introduce ASP.NET Identity tables solely for this MVP; its password hasher is used behind an application abstraction.

## 4. Android runtime and dependencies

- Kotlin, a current stable Android Gradle Plugin compatible with the available JDK, minimum SDK 26, target/compile SDK available in the build environment.
- Single activity, Jetpack Compose and Material 3.
- Navigation Compose; one `NavHost` with auth, discovery, detail, booking, favorites, and profile destinations.
- Hilt for dependency injection.
- ViewModel, coroutines, and `StateFlow` for UI state.
- Retrofit and OkHttp for HTTP. JSON names follow the API's camelCase contract.
- DataStore stores only the access token and minimal signed-in user identity. Catalog data is not persisted in Room for MVP.
- Repository calls expose success/failure results. Every network screen models loading, content, empty, error, and retry explicitly.

The debug base URL defaults to `http://10.0.2.2:8080/`, which maps the standard Android Emulator to the host API. The debug manifest permits cleartext only for local development. Release configuration must not contain a development signing key, API secret, or cleartext exception.

## 5. Domain model

All IDs are UUIDs generated by the API. Database names use `snake_case`; C# and JSON use their language conventions.

### User

- `Id`
- `Email`, plus `NormalizedEmail` with a unique index
- `PasswordHash`
- `DisplayName`
- `Locale` (`hu-HU` in MVP)
- `Role` (`Customer`; provider/admin identities are out of scope)
- `CreatedAtUtc`

Email comparison is case-insensitive through the invariant normalized value. Child names, birth dates, and child personal data are not stored.

### Provider

- `Id`, `Name`, `Description`
- postal address fields and public contact fields
- `Latitude`, `Longitude`
- optional `ImageUrl`, `AccessibilityInfo`
- `CreatedAtUtc`, `UpdatedAtUtc`

Providers are seeded/read-only through the public contract.

### Offer

- `Id`, `ProviderId`, `Title`, `Description`, `Category`
- `StartsAtUtc`, `EndsAtUtc`, `BookingCutoffUtc`, `CancelUntilUtc`
- `MinChildAge`, `MaxChildAge`, `AccompanimentRequired`
- optional `AccessibilityInfo`, `ImageUrl`
- `OriginalUnitPrice`, `DiscountedUnitPrice`, `Currency`
- `TotalCapacity`, `ReservedQuantity`
- `Status` (`Published`, `Withdrawn`)
- `CreatedAtUtc`, `UpdatedAtUtc`
- PostgreSQL row concurrency token (`xmin`) or equivalent EF concurrency token

Categories are the closed MVP values `PLAYHOUSE`, `WORKSHOP`, `MOVEMENT`, `SWIMMING`, `SPORT`, `MUSEUM`, and `PARENT_CHILD`. Additions require a contract change because Android renders localized labels by enum value.

Invariants:

- `EndsAtUtc > StartsAtUtc`.
- `BookingCutoffUtc <= StartsAtUtc`; a booking is accepted only while `now < BookingCutoffUtc` and `now < StartsAtUtc`.
- `CancelUntilUtc <= StartsAtUtc`.
- `0 <= MinChildAge <= MaxChildAge <= 18`.
- both prices are non-negative and discounted price is no greater than original price;
- currency is a three-letter uppercase ISO 4217 code; seeded data uses `HUF`;
- `TotalCapacity > 0` and `0 <= ReservedQuantity <= TotalCapacity`.

Availability is derived as `TotalCapacity - ReservedQuantity`. An offer is live only when published, before cutoff and start, and availability is positive. The database also enforces capacity and monetary check constraints.

### Booking

- `Id`, `UserId`, `OfferId`
- persisted `Status` (`Confirmed` or `Cancelled`)
- `Quantity`
- unit and total price snapshots plus currency
- offer title, provider name, start/end, and address snapshots for stable history
- unique human-readable `BookingCode`
- deterministic non-secret `QrPayload`
- `CreatedAtUtc`, optional `CancelledAtUtc`
- concurrency token

The contract exposes three booking states:

```text
                  customer cancellation at/before CancelUntilUtc
CONFIRMED ------------------------------------------------------> CANCELLED
    |
    | time reaches EndsAtUtc while still confirmed (derived, not persisted)
    v
COMPLETED
```

`COMPLETED` is a read-time projection of persisted `Confirmed`; no scheduler is required. `CANCELLED` and `COMPLETED` are terminal. Cancellation is allowed only for the owner, only while persisted status is confirmed, and when `now <= CancelUntilUtc` and `now < StartsAtUtc`. Repeating cancellation of an already-cancelled booking is idempotent and returns the cancelled representation. A completed booking cannot be cancelled.

### Favorites

- `FavoriteOffer(UserId, OfferId, CreatedAtUtc)` with composite primary key.
- `FavoriteProvider(UserId, ProviderId, CreatedAtUtc)` with composite primary key.

Adding an existing favorite and removing a missing favorite are idempotent.

## 6. Booking transaction and overbooking prevention

The offer summary's `availablePlaces` is advisory. Creation always revalidates under a PostgreSQL row lock.

Create-booking algorithm:

1. Authenticate the customer and validate `quantity` is between 1 and 10.
2. Begin an EF Core transaction at `READ COMMITTED`.
3. Load the offer and provider using `SELECT ... FOR UPDATE` on the offer row.
4. Read one injected UTC `now` value and, while holding the lock, validate published status, cutoff/start, and `TotalCapacity - ReservedQuantity >= quantity`.
5. Increment `ReservedQuantity`, create the booking with price and display snapshots, generate a unique code, and save both records.
6. Commit, then return `201 Created` with the authoritative booking.

Concurrent creators for one offer serialize on the same row. The second transaction sees the first committed quantity and receives `INSUFFICIENT_CAPACITY` rather than overbooking. A database constraint on reserved quantity is a final backstop. Do not replace this with an in-memory lock, a preflight count, or Android-side capacity checks.

Cancellation uses one transaction: lock the booking row, return idempotently if already cancelled, lock the referenced offer row, revalidate time and ownership, set cancelled fields, decrement `ReservedQuantity`, save, and commit. All rollback paths leave capacity unchanged. Integration tests must use two independent database connections/tasks competing for the final place and assert exactly one success.

## 7. Authentication and authorization

Registration and login return an access token plus the current user DTO. There is no refresh token, password reset, email verification, logout endpoint, or social login in MVP.

- JWT signing uses an HMAC SHA-256 key supplied only by `Jwt__SigningKey` environment configuration; it must be at least 32 bytes.
- Validate signature, issuer (`ugorjbe-api`), audience (`ugorjbe-android`), and expiry with at most 60 seconds clock skew.
- Access lifetime is two hours.
- Claims are `sub` (user UUID), `email`, `role=customer`, `jti`, `iat`, and `exp`.
- Passwords are 8-128 characters and must include an uppercase letter, lowercase letter, and digit. Never log or return them.
- Login uses a generic `AUTH_INVALID_CREDENTIALS` response to avoid account enumeration.

Public endpoints are health, Swagger in Development, registration/login, offers, and provider details. `/api/auth/me`, all bookings, and all favorites require a valid customer bearer token. A user can read/cancel only their own booking; the API returns `404 BOOKING_NOT_FOUND` for another user's ID.

## 8. Data, time, money, and location conventions

- Persist instants as PostgreSQL `timestamptz`; pass UTC `DateTimeOffset` values through application boundaries.
- JSON timestamps are ISO 8601 UTC strings with `Z`, such as `2026-07-14T12:30:00Z`.
- Budapest business-day defaults use the IANA zone `Europe/Budapest` and are converted to UTC after DST-aware calculation. The database/session timezone is UTC.
- Prices are PostgreSQL `numeric(12,2)`, C# `decimal`, JSON numbers, and Android decimal-safe values. Never use floating point for money. Each money value includes an ISO code.
- Coordinates use WGS84 decimal degrees. PostgreSQL stores `numeric(9,6)` latitude and longitude; MVP distance filtering uses the Haversine formula in a translatable SQL expression. Results expose kilometers rounded to one decimal. No PostGIS dependency is needed.
- JSON property names are camelCase. Enum values are uppercase snake case. Missing optional data is `null`, not an empty sentinel.

## 9. API composition and errors

The exact endpoints, DTOs, filters, pagination, and examples are frozen in `API_CONTRACT.md`. Controllers/minimal endpoints may vary internally, but generated OpenAPI must describe that contract.

All non-2xx API responses use `application/problem+json` and the same RFC 7807 extension fields (`code`, `traceId`, and optional field `errors`). Validation is performed at the API/application boundary; domain invariants are enforced again before persistence. Unhandled exceptions are logged with the trace ID and mapped to a generic problem without stack traces.

List endpoints use stable, one-based offset pagination. Queries always add `id ASC` as a final deterministic tie-breaker. Maximum page size is 50.

## 10. EF Core schema and migrations

`UgorjBeDbContext` owns one schema and the tables `users`, `providers`, `offers`, `bookings`, `favorite_offers`, and `favorite_providers`. Required indexes include:

- unique `users.normalized_email`;
- `offers(status, starts_at_utc, booking_cutoff_utc)`;
- `offers(provider_id, starts_at_utc)` and `offers(category, starts_at_utc)`;
- `bookings(user_id, created_at_utc DESC)`;
- unique `bookings.booking_code`;
- both favorite composite primary keys and target foreign-key indexes.

Foreign keys restrict deletion of users/providers/offers referenced by bookings. Seeded entities are not deleted through the MVP API.

The initial migration is committed under `UgorjBe.Infrastructure/Persistence/Migrations`. On container startup the API applies committed migrations with bounded retries before seeding. Production auto-migration is not implied by this local MVP behavior.

## 11. Deterministic local seed

Development/test seeding is idempotent and guarded by configuration (`SeedData__Enabled=true`). It uses fixed UUIDs and content for:

- customer `demo@ugorjbe.local` / `UgorjBe123!`, display name `Demó Család`;
- at least four Budapest providers across the chosen categories;
- at least eight offers with varied age, time, distance, price, accessibility, and capacity;
- one favorite offer/provider and representative previous bookings where useful.

On a fresh database, offer times are calculated from the current `Europe/Budapest` local date (future sessions today where possible, plus tomorrow fallback) so discovery remains useful. Entity identities and all non-temporal content remain fixed. Seed timestamps use the injected clock. Seeding must not reset capacity or bookings in an existing database.

Demo credentials are intentionally public development fixtures, not a production secret. JWT keys and database passwords are provided by local environment variables. `.env.example` contains names/placeholders only.

## 12. Local execution and observability

Root `docker-compose.yml` defines:

- `db`: PostgreSQL with a health check and named volume;
- `api`: builds `backend/src/UgorjBe.Api/Dockerfile`, waits for healthy PostgreSQL, listens on host port `8080`, applies migrations, and seeds development data.

The connection string points to service host `db`. Configuration is via normal ASP.NET environment variables. `/health` returns `200` only when the process is running and PostgreSQL is reachable, otherwise `503`.

Logs are structured console logs. Every request has an ASP.NET trace identifier returned in problem responses. Passwords, bearer tokens, connection-string passwords, and child data must never be logged.

## 13. Test boundaries

Unit tests cover domain validation, booking/cancellation decisions with a fake `TimeProvider`, price totals, and Android ViewModel state transitions/error mapping.

PostgreSQL integration tests cover:

- registration, duplicate email, login, and current user;
- default discovery and every material filter/sort rule;
- expired/cutoff offer rejection;
- quantity validation and insufficient capacity;
- two competing reservations for final capacity;
- cancellation before/after cutoff and capacity restoration;
- booking ownership and active/previous projections;
- idempotent favorite add/remove;
- the common problem-details envelope.

Android tests use fake repositories/MockWebServer where useful to verify login and browse-detail-reserve-code, loading/empty/error/retry, token attachment, capacity conflict display, cancellation, and favorites. A debug build is part of the quality gate.

## 14. Frozen cross-client decisions

Neither implementer may independently change these items:

1. Endpoint paths, JSON property names, enum spellings, pagination envelope, or problem `code` values in `API_CONTRACT.md`.
2. UTC wire timestamps and Budapest-only default day calculation.
3. `Money` as decimal amount plus ISO currency; no formatted price strings from the API.
4. Persisted booking states and derived `COMPLETED` behavior.
5. Cancellation boundary (`now <= cancelUntilUtc` and before start) and idempotent repeated cancellation.
6. PostgreSQL row-lock transaction as the capacity authority.
7. Public catalog/provider reads and bearer-protected me/bookings/favorites.
8. Favorite endpoint idempotency.
9. Emulator debug base URL and `/api` path ownership (base URL has no `/api`; Retrofit paths do).
10. Booking QR payload format: `ugorjbe://booking/{bookingId}?code={bookingCode}`. It is a display/check-in payload, not an authentication credential.

Any proposed change must first amend the two architecture documents and add/adjust contract tests.
