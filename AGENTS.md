# AI Agent Team Working Agreements

## Mission

Build a working product consisting of an ASP.NET Core backend, a native Android application and a secure administration web application inspired by the marketplace mechanics of Munch, without copying its branding, text, visual assets, source code or distinctive screen designs.

The product turns expiring local capacity into discounted, bookable offers. The selected concept is **UgorjBe**: discounted same-day empty places in family activities and local experiences such as playhouses, workshops, swimming sessions, sports classes, museum programs and baby-parent activities.

The booking MVP and Phase 2 map/admin expansion are complete. `CODEX_PHASE_3.md` defines the active expansion: a production-ready Android visual, motion, image, accessibility, performance and release-quality redesign. Preserve the working backend and administration web unless a verified integration defect or narrowly required presentation field must be fixed.

## Required agent workflow

The root agent acts as engineering lead and orchestrator.

For Phase 3:

1. Reproduce and fix current Android/API integration defects before redesign work, especially map-response deserialization.
2. Spawn `product_designer` to audit the current customer experience and define target-audience/product principles.
3. Spawn `mobile_experience_designer` to create `docs/MOBILE_DESIGN_SYSTEM.md`, `docs/MOTION_SPEC.md` and the licensed asset strategy.
4. Spawn `solution_architect` to freeze dependency, navigation, image, Lottie, testing and release decisions.
5. Spawn `android_engineer` to implement the complete Android redesign after specifications are stable.
6. Spawn `android_performance_engineer` to add Macrobenchmark/Baseline Profile coverage, measure critical journeys and verify release optimization.
7. Spawn `qa_engineer` to run functional, integration, UI, accessibility, visual-state and regression verification.
8. Have `mobile_experience_designer` perform a post-implementation visual and motion review.
9. Spawn `reviewer` after implementation. The reviewer must not be the original implementer.
10. The lead integrates findings, fixes all blocking/high-severity issues and reports exact commands, results and limitations.

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

Phase 3 changes presentation, motion and production quality, not these authoritative business rules.

## Administrator journey

An administrator can use the web application to:

- sign in securely;
- view a useful dashboard summary;
- list, search, create and update providers;
- list, search, filter, create and update events/offers;
- publish, unpublish or archive offers according to the documented lifecycle;
- enter Hungarian-local times that are converted to UTC over the API;
- manage address, coordinates, capacity, prices, age limits, category, imagery and accessibility details.

The web client must use authorized backend APIs. It must never access PostgreSQL directly. The current web design is a brand-quality reference for Android, but mobile must remain a native Compose experience.

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

- Kotlin and Jetpack Compose.
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

- TypeScript and a mainstream maintainable React-based stack selected by the lead.
- Responsive, accessible and original design based on the product design documents.
- Backend API integration only; no direct database access.
- Role-aware UI backed by server-side authorization.
- Form validation, loading, empty, error, unauthorized and retry states.
- Unit/component tests, browser smoke coverage and a production build.
- Docker/local-development support and CI integration.
- Phase 3 should not redesign the administration web except for narrowly justified brand/data compatibility changes.

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

## Phase 3 design and motion rules

- Create an original youthful, image-first, premium and fast-to-scan mobile experience without excluding families.
- Learn from broad current lifestyle/social-product principles, not proprietary identity or distinctive layouts.
- Do not copy TikTok, Instagram, Munch, Airbnb, Google Maps or another product.
- Do not hide the four compact top-level destinations in a hamburger drawer.
- Treat Map and List as two presentations of Explore, with preserved shared state.
- Use motion to explain continuity, hierarchy, state and success; never delay a task for decoration.
- Lottie is required but limited to purposeful success/empty/loading or brand moments, with static/native fallbacks and documented licenses.
- Respect reduced/system-disabled animation expectations.
- Use remote imagery when available and an intentional category fallback when not.
- The app must still look complete with every image request failing.
- Never use addictive engagement mechanics, feeds, stories, reels, autoplay media or dark patterns.
- Hungarian is the default demo locale; all visible strings remain localization-ready.

## Quality gates

Before declaring Phase 3 complete:

- all existing backend restore/build/format/unit/integration gates remain green;
- administration web lint/typecheck/tests/build remain green;
- Android DTO contract tests cover the map envelope and representative API responses;
- Android unit/UI/accessibility/screenshot or golden tests and debug build pass;
- Android release build passes with R8/minification and safe resource shrinking;
- connected tests pass on a configured Google APIs emulator/device;
- Baseline Profile is generated and a measured benchmark report identifies device/build/commands/results;
- Docker Compose starts backend, database and admin web;
- a seeded/admin-created published offer appears in both Android Map and List;
- complete discovery -> detail -> reserve -> success -> booking code -> cancellation remains functional;
- favorites and error recovery remain functional;
- light/dark, compact/expanded, 200% font scaling, TalkBack semantics and reduced-motion behavior are reviewed;
- Lottie/image failures degrade safely;
- post-implementation design review and independent engineering review have no unresolved blocking/high-severity findings;
- README and Phase 3 reports contain exact setup, build, release, test, Maps and benchmark instructions;
- no API keys, passwords, signing files, proprietary fonts/assets or production secrets are committed.

## Git rules

- Never push implementation work directly to `main`.
- Use focused branches or worktrees and small commits.
- Do not merge a PR or deploy anything without human approval.
- Do not rewrite unrelated files.
- A code author cannot be the sole reviewer of that code.
- Dependency and third-party asset additions must include purpose, source and license review.

## Definition of done

The result is not done when screenshots or redesigned files merely exist. It is done when a fresh developer can start the stack, supply a local Maps key, build debug and release Android variants, sign in, discover real offers on Map/List, complete booking/favorite/cancellation flows, observe the production visual/motion system and safe fallbacks, run documented tests/benchmarks and review a draft pull request containing measured evidence and no unresolved blocking defects.
