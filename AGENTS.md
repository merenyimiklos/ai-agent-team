# AI Agent Team Working Agreements

## Mission
Build a working MVP consisting of an ASP.NET Core backend and a native Android application inspired by the marketplace mechanics of Munch, without copying its branding, text, visual assets, source code, or distinctive screen designs.

The product must turn expiring local capacity into discounted, bookable offers. The lead agent owns the final product decision. It must compare at least three underserved themes, record the decision in `docs/PRODUCT_DECISION.md`, and then continue autonomously. If research is inconclusive, use the fallback concept **UgorjBe**: discounted same-day empty places in family activities and local experiences such as playhouses, workshops, swimming sessions, sports classes, museum programs, and baby-parent activities.

## Required agent workflow
The root agent acts as engineering lead and orchestrator.

1. Spawn `product_researcher` to compare candidate markets and define the MVP.
2. Spawn `solution_architect` to produce the architecture and API contract.
3. After the product decision and API contract are stable, run `backend_engineer` and `android_engineer` in parallel where possible.
4. Spawn `qa_engineer` to create and run automated tests and verify the full flow.
5. Spawn `reviewer` after implementation. The reviewer must not implement the original feature.
6. The lead integrates findings, fixes blocking issues, and reports exact commands and test results.

Agents communicate through committed documents, API contracts, issue/PR notes, and results returned to the lead. Do not rely on undocumented assumptions.

## MVP user journey
A customer can:
- register and sign in;
- browse nearby, time-limited offers;
- filter and sort offers;
- open offer and provider details;
- reserve one or more available places;
- receive a booking code or QR payload;
- view active and previous bookings;
- cancel when permitted;
- save favorite offers or providers.

A provider can be represented through seeded data and backend endpoints. A separate provider app or full admin web UI is not required for the first MVP.

## Technical baseline
### Backend
- .NET 8 or newer LTS-compatible ASP.NET Core Web API.
- PostgreSQL with Entity Framework Core migrations.
- Layered modular monolith; avoid unnecessary microservices.
- JWT authentication with secure password hashing.
- OpenAPI/Swagger documentation.
- Dockerfile and root `docker-compose.yml` for API and PostgreSQL.
- Seed data that makes the Android app useful immediately.
- Unit tests and integration tests using a real or containerized PostgreSQL-compatible setup where practical.

### Android
- Kotlin and Jetpack Compose.
- Material 3 with an original visual identity.
- Single-activity architecture, Navigation Compose, ViewModel, Coroutines and Flow.
- Hilt for dependency injection.
- Retrofit/OkHttp for API access.
- Room or DataStore only where offline/cache behavior adds clear value.
- Clear loading, empty, error, and retry states.
- The debug build must support Android Emulator access to the local backend.

## Initial API areas
The final contract may refine names, but it should cover:
- `/api/auth/register`, `/api/auth/login`, `/api/auth/me`;
- `/api/offers` list/search/filter and `/api/offers/{id}`;
- `/api/providers/{id}`;
- `/api/bookings` create/list/detail/cancel;
- `/api/favorites` add/remove/list;
- `/health`.

Use UTC in storage and ISO-8601 timestamps over the API. Model money with decimal values and an explicit ISO currency code. Prevent overbooking with a database-backed concurrency strategy.

## Quality gates
Before declaring the MVP complete:
- backend restore, build, unit tests, and integration tests pass;
- Android unit tests and debug build pass;
- Docker Compose starts the backend and database;
- a seeded user can complete browse -> detail -> reserve -> booking code using the Android app;
- overbooking and expired-offer cases have automated tests;
- the repository contains setup instructions and demo credentials;
- no API keys, passwords, signing files, or production secrets are committed.

## Git rules
- Never push directly to `main` for implementation work after this bootstrap.
- Use focused branches or worktrees and small commits.
- Do not merge a PR or deploy anything without human approval.
- Do not rewrite unrelated files.
- A code author cannot be the sole reviewer of that code.

## Product and design rules
- Reuse marketplace interaction patterns, not proprietary identity.
- Do not use the Munch name, logo, screenshots, colors as a copied palette, text, or assets.
- Prefer a warm, trustworthy, family-friendly and accessibility-aware design.
- Hungarian is the default demo locale, but user-facing strings must be structured for localization.
- Payment processing is mocked or represented as pay-on-arrival in MVP unless the lead documents a safe sandbox integration.

## Definition of done
The result is not done when files merely exist. It is done when a fresh developer can follow the README, start PostgreSQL and the API, build the Android app, log in with documented demo credentials, and complete the core booking flow.