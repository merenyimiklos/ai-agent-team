# AI Agent Team Working Agreements

## Mission

Build a working product consisting of an ASP.NET Core backend, a native Android application and a secure administration web application inspired by expiring-capacity marketplace mechanics, without copying another product's branding, text, visual assets, source code or distinctive screen designs.

The product turns expiring local capacity into discounted, bookable offers. The selected concept is **UgorjBe**: discounted same-day empty places in family activities and local experiences such as playhouses, workshops, swimming sessions, sports classes, museum programs and baby-parent activities.

The booking MVP and Phase 2 map/admin expansion are complete. Phase 3 defines the production-ready Android foundation. `CODEX_PHASE_4.md` defines the active reference-led visual-polish phase and must be implemented on top of the completed Phase 3 head, not on the older Phase 2 code.

## Required agent workflow

The root agent acts as engineering lead and orchestrator.

For Phase 4:

1. Verify the completed Phase 3 branch/PR, real API integration, release build, accessibility and performance gates before visual work.
2. Spawn `reference_ui_researcher` to create `docs/REFERENCE_APP_BENCHMARK.md` from current lawful public sources.
3. Spawn `mobile_experience_designer` to audit the real Phase 3 build and create `docs/PHASE4_ART_DIRECTION.md`.
4. Spawn `solution_architect` to validate navigation, state, dependency, release and test decisions.
5. Spawn `android_engineer` to implement the complete visual pass after specifications are stable.
6. Spawn `android_performance_engineer` to rerun release-like benchmarks and fix regressions.
7. Spawn `qa_engineer` to run functional, integration, UI, accessibility, theme, visual-state and regression verification.
8. Spawn `brand_originality_reviewer` for an independent reference-similarity/originality review.
9. Spawn `reviewer` after implementation. Reviewers must not be the original implementer.
10. The lead fixes all blocking/high-severity findings and reports exact commands, results and limitations.

Agents communicate through committed documents, contracts, test reports, issue/PR notes and results returned to the lead. Do not rely on undocumented assumptions.

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

Phase 4 changes visual identity and interaction polish, not these authoritative business rules.

## Administrator journey

An administrator can use the web application to:

- sign in securely;
- view a useful dashboard summary;
- list, search, create and update providers;
- list, search, filter, create and update events/offers;
- publish, unpublish or archive offers according to the documented lifecycle;
- enter Hungarian-local times that are converted to UTC over the API;
- manage address, coordinates, capacity, prices, age limits, category, imagery and accessibility details.

The web client must use authorized backend APIs and never access PostgreSQL directly. The current web design is a brand-quality reference for Android, but mobile must remain a native Compose experience. Phase 4 must not redesign the administration web except for a narrowly justified compatibility correction.

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
- Backend/API and Android DTO contracts must be regression-tested with representative real JSON.

### Android

- Kotlin and Jetpack Compose; do not migrate to Flutter or a WebView UI.
- Material 3 with an original tokenized visual identity.
- Google Maps Compose for map discovery.
- Single-activity architecture, Navigation Compose, ViewModel, Coroutines and Flow.
- Hilt for dependency injection.
- Retrofit/OkHttp for API access.
- Maintained image loading with caching and deterministic fallback.
- Airbnb Lottie Compose for small, purposeful, licensed or original animations.
- Official Compose animation/shared-transition APIs where appropriate.
- Macrobenchmark/Baseline Profiles and release-like performance measurement.
- DataStore or Room only where it adds clear value.
- Clear loading, skeleton, empty, stale, error, permission-denied, map-unavailable, offline and retry states.
- The debug build must support Android Emulator access to the local backend.
- A Maps API key must be supplied locally and never committed.
- Release must be minified/optimized, must not use cleartext or debug API configuration and must not expose secrets/demo-only UI.

### Administration web

- TypeScript and a mainstream maintainable React-based stack.
- Responsive, accessible and original design based on product design documents.
- Backend API integration only; no direct database access.
- Role-aware UI backed by server-side authorization.
- Form validation, loading, empty, error, unauthorized and retry states.
- Unit/component tests, browser smoke coverage and a production build.
- Docker/local-development support and CI integration.

## API areas

The contract covers:

- `/api/auth/register`, `/api/auth/login`, `/api/auth/me`;
- `/api/offers` list/search/filter/map-area and `/api/offers/{id}`;
- `/api/providers/{id}`;
- `/api/bookings` create/list/detail/cancel;
- `/api/favorites` add/remove/list;
- authorized administration endpoints for providers and offers;
- `/health`.

Use UTC in storage and ISO-8601 timestamps over the API. Display and accept administrator input clearly in `Europe/Budapest`, then convert explicitly. Model money with decimal values and an explicit ISO currency code. Prevent overbooking with a database-backed concurrency strategy.

## Phase 4 design and originality rules

- Research GreenGo, wigo, Munch, Free2move and historically relevant SHARE NOW material as quality references only.
- Treat an ambiguous reference name such as `cleengo` as unresolved until an authoritative link or package name is supplied.
- Learn from principles such as map-first discovery, floating controls, image-led cards, concise hierarchy and one clear primary action.
- Do not copy logos, names, exact palettes, font combinations, icons, markers, copy, assets, signature motion or distinctive whole-screen compositions.
- The result must be youthful, bold, uncluttered, image-led, fast to scan, map-native and unmistakably UgorjBe.
- Do not hide the four compact top-level destinations in a hamburger drawer.
- Treat Map and List as two presentations of Explore with preserved shared state.
- Use motion to explain continuity, hierarchy, state and success; never delay a task for decoration.
- Keep Lottie purposeful, finite where appropriate, safely licensed and backed by static/native fallbacks.
- Respect reduced/system-disabled animation expectations.
- The app must still look complete when every image request fails.
- Never add feeds, stories, reels, autoplay media, addictive engagement mechanics or dark patterns.
- Hungarian is the default demo locale; all visible strings remain localization-ready.

## Quality gates

Before declaring Phase 4 complete:

- all Phase 3 backend, web and Android gates remain green;
- Android DTO contract tests cover representative API responses;
- Android unit/UI/accessibility/screenshot or golden tests and debug build pass;
- Android release build passes with R8/minification and safe resource shrinking;
- connected tests pass on a configured Google APIs emulator/device;
- Baseline Profile remains generated and consumed, and critical benchmarks are rerun;
- Docker Compose starts backend, database and admin web;
- a seeded/admin-created published offer appears in both Android Map and List;
- discovery -> detail -> reserve -> success -> booking code -> cancellation remains functional;
- favorites and error recovery remain functional;
- light/dark, compact/expanded, 200% font scaling, TalkBack semantics and reduced-motion behavior are reviewed;
- image/Lottie/Maps/backend failures degrade safely;
- reference benchmark, art direction, visual review and captured Phase 4 evidence exist;
- independent originality and engineering reviews have no unresolved blocking/high-severity findings;
- no API keys, passwords, signing files, proprietary fonts/assets or production secrets are committed.

## Git rules

- Never push implementation work directly to `main`.
- Use focused branches or worktrees and small commits.
- Do not merge a PR or deploy anything without human approval.
- Do not rewrite unrelated files.
- A code author cannot be the sole reviewer of that code.
- Dependency and third-party asset additions must include purpose, source and license review.

## Definition of done

The result is not done when screenshots or redesigned files merely exist. It is done when a fresh developer can start the stack, supply a local Maps key, build debug and release Android variants, sign in, discover real offers on Map/List, complete booking/favorite/cancellation flows, observe the original Phase 4 visual/motion system and safe fallbacks, run documented tests/benchmarks and review a draft pull request containing measured evidence and no unresolved blocking defects.