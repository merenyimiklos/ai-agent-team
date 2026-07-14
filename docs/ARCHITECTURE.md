# UgorjBe Phase 2 architecture

**Status:** Frozen for Phase 2 implementation<br>
**Date:** 2026-07-15<br>
**Product:** [PRODUCT_DECISION.md](PRODUCT_DECISION.md)<br>
**UX:** [UX_DIRECTION.md](UX_DIRECTION.md)<br>
**Wire contract:** [API_CONTRACT.md](API_CONTRACT.md)

This document is the shared implementation boundary for the ASP.NET Core API, native Android app, and administration web. The working authentication, booking, cancellation, favorites, expiry, capacity, and PostgreSQL row-lock behavior remain authoritative. A contract change requires coordinated documentation and tests before client-specific changes.

## 1. System decision

UgorjBe remains one modular monolith and one PostgreSQL database. Phase 2 adds a bounded map query and an administrator module to the API, a map-first Android presentation, and a React administration SPA.

```text
Android customer app ─┐
                      ├─ HTTPS/JSON ─ ASP.NET Core API ─ EF Core ─ PostgreSQL
React admin SPA ──────┘
```

There is no direct database access from either client, no second backend, no message broker, and no mock runtime catalog. The API is the authority for visibility, lifecycle, price, booking eligibility, capacity, and cancellation. Existing bookings are immutable snapshots except for their own cancellation status.

The administration web stack is React + TypeScript + Vite, React Router, TanStack Query, React Hook Form, and Zod. Exact mutually compatible package versions are selected and locked at implementation time. The SPA uses backend APIs only.

## 2. Repository and dependency boundaries

```text
/
|-- backend/
|   |-- src/
|   |   |-- UgorjBe.Api/
|   |   |-- UgorjBe.Application/
|   |   |-- UgorjBe.Domain/
|   |   `-- UgorjBe.Infrastructure/
|   `-- tests/
|-- android/
|-- web-admin/
|   |-- src/
|   |   |-- api/
|   |   |-- auth/
|   |   |-- components/
|   |   |-- features/
|   |   |-- routes/
|   |   `-- styles/
|   |-- tests/
|   |-- Dockerfile
|   `-- nginx.conf
|-- docs/
|-- docker-compose.yml
`-- .github/workflows/ci.yml
```

Backend dependencies remain one-way:

```text
Api -> Application -> Domain
Api -> Infrastructure -> Application + Domain
```

Identity, Catalog, Bookings, Favorites, and Administration are modules in the same process and DbContext. Application defines contracts/use cases; Infrastructure implements EF, JWT, clocks, seed, and queries; Api owns HTTP, authorization policies, CORS, OpenAPI, and problem responses.

## 3. Domain evolution and migration

### Users and roles

`User.Role` is a required lowercase value: `customer` or `admin`. Registration always creates `customer`. An administrator is created only by guarded development seed or an out-of-band production process. JWTs include the role claim; the backend policy `AdminOnly` requires `admin`.

`UserDto` gains `role`. This is an additive customer-client change.

### Providers

Providers become administrator-managed but are never hard-deleted. The Phase 2 API supports list, detail, create, and update. Existing foreign keys remain restrictive. A provider update does not rewrite historical booking snapshots.

### Offers and event location

Offer lifecycle becomes:

```text
DRAFT ───────> PUBLISHED <──────> UNPUBLISHED
  └──────────────┴────────────────────┴──────> ARCHIVED
```

- `DRAFT` is server-valid but not public or bookable.
- `PUBLISHED` is discoverable only while all existing live/bookable predicates also hold.
- `UNPUBLISHED` is hidden from discovery and rejects new bookings; it may be republished.
- `ARCHIVED` is terminal, hidden from discovery, and rejects new bookings.
- Publish and republish set `PublishedAtUtc` on first publish and `UpdatedAtUtc` on every transition.
- Archive sets `ArchivedAtUtc`. Unpublish/archive never cancel an existing booking or change reserved quantity.
- A draft that has never been published returns public `404`. A known, previously published offer may still be opened from a favorite/booking link and reports an authoritative unavailable reason.

Each offer gains its own `Address` fields, including WGS84 coordinates. This supports an event away from the provider's registered address. Public offer DTOs expose offer `address`; the nested provider keeps its provider address for compatibility. New bookings snapshot the offer address. The migration copies each existing provider address to its existing offers.

The migration:

1. adds offer postal/city/street/country/latitude/longitude columns and backfills from provider;
2. maps existing `WITHDRAWN` values to `UNPUBLISHED`;
3. adds `PublishedAtUtc`, `ArchivedAtUtc`, and update concurrency configuration;
4. preserves all IDs, bookings, favorites, quantities, and price snapshots;
5. adds the indexes below before enabling the new queries.

Required indexes:

- `offers(status, starts_at_utc, booking_cutoff_utc)`;
- `offers(status, latitude, starts_at_utc)` and `offers(status, longitude, starts_at_utc)` so PostgreSQL can bitmap-combine both bounded coordinate predicates;
- existing provider/category/start indexes;
- `providers(updated_at_utc, id)`;
- `offers(updated_at_utc, id)`;
- existing booking, email, code, and favorite indexes.

No PostGIS dependency is required for Phase 2. The visible-area query first applies indexed rectangular latitude/longitude predicates in SQL. Optional radius/distance filtering uses the existing Haversine calculation after that bounded candidate set; it must not load a global catalog into memory.

### Booking invariants during administration

The existing `SELECT ... FOR UPDATE` booking/cancellation transaction is unchanged. Administrator mutations also enforce:

- `totalCapacity >= reservedQuantity`;
- reserved quantity is read-only;
- with any confirmed booking, provider, offer location, start/end, cancellation cutoff, and currency cannot change;
- price edits affect only later bookings because every booking keeps its price snapshot;
- unpublish/archive does not release capacity;
- cancellation still restores capacity exactly once even when the offer is unpublished or archived.

Updates use optimistic concurrency. Admin detail DTOs expose opaque `version` derived from PostgreSQL `xmin`. PUT and lifecycle requests require that version. A mismatch is `409 CONCURRENCY_CONFLICT`.

## 4. Public discovery and map query

`GET /api/offers` remains the one-based paged list endpoint (default 20, maximum 50). It gains an optional all-or-none bounding box so server-paged list consumers can use the same geographic predicate.

`GET /api/offers/map` is the map-first endpoint. A bounding box is mandatory:

- `south < north` and `west < east`;
- anti-meridian-crossing boxes are rejected in Phase 2;
- latitude span is at most 2 degrees and longitude span at most 3 degrees;
- south/west edges are inclusive; north/east edges are exclusive;
- `limit` defaults to 100 and is 1–200.

The endpoint supports the complete public discovery filter set and deterministic sorting. The database query applies lifecycle, cutoff, start, capacity, time, text/category/age/price, and bounding predicates before projection. It reads `limit + 1` rows; at most `limit` are returned. The extra row sets `isTruncated=true` without an unbounded count query.

Map and list share the same application-level `DiscoverySpecification` so eligibility cannot drift. Android's Explore ViewModel stores one map envelope; Map and List render the exact loaded items and toggling views does not request data. The list reveals the bounded result in local chunks of 20. A truncated response is not paged beyond 200; both presentations ask the user to zoom or narrow filters. General non-map lists continue to use `GET /api/offers` pagination.

Camera behavior follows `UX_DIRECTION.md`:

1. initial and filter/search requests use the visible bounds;
2. movement keeps stale markers and does not continuously request;
3. camera-idle thresholds only expose `Keresés ezen a területen`;
4. the action sends the exact visible bounds and current filters;
5. only the newest request generation may replace state;
6. failed area search retains stale results and offers retry.

The API does not receive camera events or infer viewport changes. The Android client owns the searched-area baseline and request cancellation.

## 5. Authentication, authorization, and browser session

JWT signing, issuer validation, expiry, HMAC key length, password hashing, and generic login failure remain unchanged. Access tokens last two hours and contain `sub`, `email`, `role`, `jti`, `iat`, and `exp`. The existing configured audience is retained for compatibility; it identifies UgorjBe first-party clients even if its local configuration label predates the web client.

Authorization rules:

- public: health, login/register, public offers/map, public offer/provider detail;
- authenticated customer or admin: `/api/auth/me`;
- customer-owned booking/favorite APIs: authenticated user and ownership checks;
- `/api/admin/**`: authenticated plus `AdminOnly`;
- absent/invalid/expired token: `401 AUTH_REQUIRED`;
- valid token without the admin role: `403 AUTH_FORBIDDEN`.

The web SPA keeps its bearer token only in React in-memory auth state. It never writes the token to localStorage, sessionStorage, IndexedDB, URL, logs, or cookies. Page refresh/browser close requires login again. Logout clears the token and TanStack Query cache. A `401` clears the session and returns to login with a relative intended route; `403` shows an unauthorized screen and does not pretend the mutation succeeded. Client role checks control presentation only.

Registration cannot request or elevate a role. The development seed adds:

- customer: `demo@ugorjbe.local` / `UgorjBe123!`;
- administrator: `admin@ugorjbe.local` / `UgorjBeAdmin123!`.

These are documented local fixtures, never production credentials.

## 6. Administrator module

The exact endpoints and DTOs are in `API_CONTRACT.md`. The module supplies:

- dashboard counts and five next published offers;
- provider list/detail/create/update;
- offer list/detail/create/update;
- publish, unpublish, and archive transitions.

There are no delete endpoints. All collection queries are bounded, one-based, default 20, maximum 50, and end with ID ascending as a stable tie-breaker. Dashboard `nextOffers` is capped at five.

Create produces a valid `DRAFT`. Publish performs readiness validation again with one injected UTC `now`. An archived offer is immutable. Invalid transitions do not mutate data. State mutations and capacity-sensitive updates use transactions, and all writes update `UpdatedAtUtc`.

## 7. Time and Hungarian administrator input

The database and wire contract remain UTC: PostgreSQL `timestamptz` and ISO-8601 strings ending in `Z`. The admin API rejects local or offset timestamps for offer write requests. The SPA displays and edits wall times in IANA zone `Europe/Budapest`.

Web conversion uses a tested IANA-zone library such as Luxon:

1. parse the exact `datetime-local` text as a Budapest wall time;
2. round-trip it to the same local text; a mismatch is a nonexistent spring-forward time and blocks submit;
3. inspect possible offsets; any count other than one is invalid/ambiguous and blocks submit rather than choosing silently;
4. convert the one valid instant to UTC with `Z` and preview it before submit;
5. convert API UTC values back through `Europe/Budapest` for edit/display.

The form always labels the zone. Browser/system timezone is not used for business conversion. Backend validation still enforces ordering, future publish readiness, cutoffs, and UTC input.

## 8. Android architecture and Maps key

Android remains a single-activity Compose app with Hilt, Retrofit/OkHttp, coroutines/Flow, and a real backend repository. Phase 2 adds Google Maps Compose, the Maps utility Compose clustering library, adaptive navigation components, and location permission handling.

One saved `ExploreViewModel` owns presentation, committed filters, searched/current bounds, result envelope, selection, camera baseline, request generation, permission state, and stale/error state. Map and List do not own separate filters or repositories. Approximate foreground location is optional; no background permission or raw location history is stored. Budapest `47.4979, 19.0402` at zoom 12 is the fallback.

Use the Google Maps Secrets Gradle Plugin:

- root `local.properties` or another gitignored local secrets file contains `MAPS_API_KEY=...`;
- checked-in `secrets.defaults.properties` contains only `MAPS_API_KEY=DEFAULT_API_KEY` so CI compiles;
- manifest metadata reads the generated placeholder;
- no key is placed in Kotlin, resources, Gradle source, CI YAML, screenshots, or logs;
- developers restrict the key to Android application ID and signing-certificate SHA-1, and enable only Maps SDK for Android.

A missing/default key must render a recoverable Map-unavailable state and a real backend List action, not mock offers or a crash.

## 9. Local URLs, CORS, and reverse proxy

The frozen port topology resolves the previous documentation/Compose inconsistency:

| Surface | Local URL | Purpose |
| --- | --- | --- |
| Admin Vite dev | `http://localhost:5173` | hot-reload SPA |
| API host | `http://localhost:8081` | Swagger, API, health |
| Compose admin web | `http://localhost:8080` | built SPA and same-origin proxy |
| Android Emulator API | `http://10.0.2.2:8081` | host API from emulator |
| PostgreSQL host | `localhost:5432` | local development only |

The Vite development client calls `http://localhost:8081`. Development CORS permits exactly configurable `http://localhost:5173`, methods GET/POST/PUT/DELETE/OPTIONS, and headers Authorization/Content-Type. Credentials are disabled and wildcard origins are forbidden. Production/Compose does not require CORS: nginx serves the SPA and proxies `/api/*` and `/health` to the API container on port 8080.

Android uses the API host port 8081 through `10.0.2.2`. The Retrofit base URL contains no `/api` segment.

## 10. Docker and configuration

Root Compose contains:

- `db`: PostgreSQL 16, health check, named volume;
- `api`: container port 8080, host 8081, migrations and development seed;
- `admin-web`: nginx/static SPA, host 8080, depends on healthy API, proxies API/health.

No secret is baked into an image. `.env.example` contains placeholders only. Vite runtime/compile configuration contains a non-secret API base setting; Compose builds for relative same-origin paths. Development seed is guarded by `SeedData__Enabled=true`. Production auto-migration, production seed, deployment, TLS termination, and secret management are outside this phase.

## 11. CI

GitHub Actions runs independent jobs:

- backend: locked restore, Release build, format verification, unit tests, PostgreSQL integration tests;
- Android: JDK 17, SDK install, unit/UI tests that do not need a billable map, optional configured lint, `assembleDebug` with default placeholder key;
- web: `npm ci`, lint, typecheck, unit/component tests, production build, and browser smoke against real API/PostgreSQL;
- Compose smoke: build/start all three services, wait for `http://localhost:8081/health`, verify `http://localhost:8080` and its proxied health, then tear down volumes.

The browser smoke logs in as the seeded admin, creates a uniquely named provider/offer, publishes it, and confirms it through the public discovery API. It uses local development credentials and no external Google Maps service.

## 12. Test and security gates

Backend PostgreSQL integration tests cover:

- map bound validation, shared filters, cap/truncation, geographic inclusion/exclusion;
- unauthenticated admin `401` and customer `403` on every admin controller family;
- admin provider/offer success and field errors;
- lifecycle transitions and unpublished/archived booking rejection;
- protected-field and capacity conflicts with existing bookings;
- optimistic concurrency;
- all original expiry, overbooking, cancellation, ownership, and favorite tests.

Android tests cover shared Map/List state, filter synchronization, camera dirty thresholds, newest-request wins, selection, permission denial, map failure/list fallback, navigation, and original booking flows. Instrumented tests do not require a real API key.

Web tests cover auth memory behavior, route guards, validation/error mapping, Budapest conversion including DST gap/overlap, responsive/accessible components, provider/offer forms, lifecycle confirmations, and a real browser smoke.

Logs and analytics must redact bearer tokens, passwords, connection-string passwords, and Maps keys. Remote image URLs are treated as untrusted display input. Admin write URLs accept only HTTP/HTTPS and are never fetched server-side, preventing SSRF.

## 13. Frozen cross-client decisions

1. API host is 8081; Compose admin is 8080; Vite is 5173; emulator API is `10.0.2.2:8081`.
2. Map discovery is `GET /api/offers/map` with required bounded bbox, maximum 200 items, and explicit truncation.
3. Map and List use the same eligibility implementation and loaded Explore result set.
4. Offer location is separate from provider address; existing data is backfilled.
5. Admin states are `DRAFT`, `PUBLISHED`, `UNPUBLISHED`, and `ARCHIVED` with only the transitions above.
6. Backend role authorization is authoritative; customer tokens receive `403 AUTH_FORBIDDEN`.
7. Admin bearer tokens are memory-only; no refresh token or browser persistence.
8. UTC remains the wire/storage format; web conversion is explicitly `Europe/Budapest` and blocks ambiguous/nonexistent local input.
9. Existing row-lock booking/cancellation transactions, snapshots, idempotency, and ownership behavior are unchanged.
10. Maps keys are local, gitignored, application-restricted, and replaceable by a harmless CI placeholder.
11. Admin writes use opaque versions and reject lost updates.
12. OpenAPI and tests must match `API_CONTRACT.md` before any independent client contract change.
