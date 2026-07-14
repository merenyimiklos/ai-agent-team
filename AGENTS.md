# AI Agent Team Working Agreements

## Mission
Build a working product consisting of an ASP.NET Core backend, a native Android application and a secure administration web application inspired by the marketplace mechanics of Munch, without copying its branding, text, visual assets, source code or distinctive screen designs.

The product turns expiring local capacity into discounted, bookable offers. The selected concept is **UgorjBe**: discounted same-day empty places in family activities and local experiences such as playhouses, workshops, swimming sessions, sports classes, museum programs and baby-parent activities.

The first booking MVP is complete. `CODEX_PHASE_2.md` defines the current expansion: a map-first Android redesign and an administrator web application.

## Required agent workflow
The root agent acts as engineering lead and orchestrator.

For the current expansion:

1. Spawn `product_designer` to research current UX guidance and write `docs/UX_DIRECTION.md`.
2. Spawn `solution_architect` to update architecture and API contracts.
3. Run `backend_engineer`, `android_engineer` and `web_admin_engineer` in parallel only after shared contracts are stable.
4. Spawn `qa_engineer` to create and run automated tests and verify the integrated flow.
5. Have `product_designer` perform a post-implementation UX review.
6. Spawn `reviewer` after implementation. The reviewer must not be the original implementer.
7. The lead integrates findings, fixes blocking issues and reports exact commands and test results.

Agents communicate through committed documents, API contracts, issue/PR notes and results returned to the lead. Do not rely on undocumented assumptions.

## Customer journey
A customer can:

- register and sign in;
- discover nearby, time-limited offers on a map or in a list;
- search, filter and sort offers;
- open offer and provider details;
- reserve one or more available places;
- receive a booking code or QR payload;
- view active and previous bookings;
- cancel when permitted;
- save favorite offers or providers.

## Administrator journey
An administrator can use the web application to:

- sign in securely;
- view a useful dashboard summary;
- list, search, create and update providers;
- list, search, filter, create and update events/offers;
- publish, unpublish or archive offers according to the documented lifecycle;
- enter Hungarian-local times that are converted to UTC over the API;
- manage address, coordinates, capacity, prices, age limits, category and accessibility details.

The web client must use authorized backend APIs. It must never access PostgreSQL directly.

## Technical baseline

### Backend

- .NET 8 or newer LTS-compatible ASP.NET Core Web API.
- PostgreSQL with Entity Framework Core migrations.
- Layered modular monolith; avoid unnecessary microservices.
- JWT authentication with secure password hashing and backend-enforced role authorization.
- OpenAPI/Swagger documentation.
- Dockerfile and root `docker-compose.yml`.
- Seed data that makes Android and admin web useful immediately.
- Unit tests and PostgreSQL integration tests.
- Efficient bounded map-area or radius queries; never return an unbounded global marker set.

### Android

- Kotlin and Jetpack Compose.
- Material 3 with an original visual identity.
- Google Maps Compose for map discovery.
- Single-activity architecture, Navigation Compose, ViewModel, Coroutines and Flow.
- Hilt for dependency injection.
- Retrofit/OkHttp for API access.
- DataStore or Room only where it adds clear value.
- Clear loading, empty, error, permission-denied and retry states.
- The debug build must support Android Emulator access to the local backend.
- A Maps API key must be supplied locally and never committed.

### Administration web

- TypeScript and a mainstream maintainable React-based stack selected by the lead.
- Responsive, accessible and original design based on `docs/UX_DIRECTION.md`.
- Backend API integration only; no direct database access.
- Role-aware UI backed by server-side authorization.
- Form validation, loading, empty, error, unauthorized and retry states.
- Unit/component tests, browser smoke coverage and a production build.
- Docker/local-development support and CI integration.

## API areas
The contract should cover:

- `/api/auth/register`, `/api/auth/login`, `/api/auth/me`;
- `/api/offers` list/search/filter/map-area and `/api/offers/{id}`;
- `/api/providers/{id}`;
- `/api/bookings` create/list/detail/cancel;
- `/api/favorites` add/remove/list;
- authorized administration endpoints for providers and offers;
- `/health`.

Use UTC in storage and ISO-8601 timestamps over the API. Display and accept administrator input clearly in `Europe/Budapest`, then convert explicitly. Model money with decimal values and an explicit ISO currency code. Prevent overbooking with a database-backed concurrency strategy.

## Quality gates
Before declaring the current phase complete:

- backend restore, build, format, unit tests and PostgreSQL integration tests pass;
- Android unit/UI tests and debug build pass;
- web lint, typecheck, tests and production build pass;
- Docker Compose starts the backend, database and admin web where configured;
- a seeded customer can complete map/list discovery -> detail -> reserve -> booking code;
- a seeded administrator can create and publish an event through the web UI;
- the created event can appear in Android discovery;
- customer tokens cannot access admin endpoints;
- overbooking, expired-offer, unpublished-offer and invalid admin-input cases have automated tests;
- README contains setup instructions, demo credentials and Google Maps key setup;
- no API keys, passwords, signing files or production secrets are committed.

## Git rules

- Never push implementation work directly to `main`.
- Use focused branches or worktrees and small commits.
- Do not merge a PR or deploy anything without human approval.
- Do not rewrite unrelated files.
- A code author cannot be the sole reviewer of that code.

## Product and design rules

- Reuse marketplace interaction patterns, not proprietary identity.
- Do not use the Munch name, logo, screenshots, copied palette, text or assets.
- Do not reproduce another map product's distinctive screen design.
- Prefer a warm, trustworthy, modern, family-friendly and accessibility-aware identity.
- On compact phones, keep three to five core destinations discoverable through bottom navigation rather than hiding them in a hamburger drawer.
- Treat Map and List as two presentations of Explore, with a prominent in-context switch.
- Use adaptive navigation rail or drawer for wider windows where appropriate.
- Hungarian is the default demo locale, but user-facing strings must be structured for localization.
- Payment processing remains mocked or pay-on-arrival unless a safe sandbox integration is explicitly approved.

## Definition of done
The result is not done when files merely exist. It is done when a fresh developer can follow the README, start the stack, supply a local Google Maps key, build the Android app and admin web, sign in with documented demo accounts, create and publish an event as administrator, discover it on Android and complete the booking flow as a customer.