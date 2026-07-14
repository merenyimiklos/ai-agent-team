# Codex task: build the first working MVP

Act as the lead engineer and product owner for this repository. Read `AGENTS.md` first and follow it throughout the task.

Use the configured specialist agents. Coordinate them through written artifacts and the shared API contract. Do not merely create plans: continue through implementation, automated testing, defect fixing, and final review.

## Phase 1 — product decision

Spawn `product_researcher` to investigate at least three Munch-like marketplace opportunities where local inventory or capacity expires with time. The concept must be useful in Hungary, differentiated enough to test, operationally feasible for a small MVP, and capable of repeat use.

The research should consider, but not be limited to:

- discounted same-day empty places in children’s and family activities;
- last-minute cancelled appointments at local service providers;
- expiring capacity in workshops, sports classes, cultural programs or hobby experiences.

Choose one concept. Do not copy the Munch brand or its exact product. Record the evidence, scoring, target users, value proposition, monetization hypothesis, MVP scope, non-goals and risky assumptions in `docs/PRODUCT_DECISION.md`.

If research cannot establish a clearly better choice, use **UgorjBe**: a marketplace where families discover and reserve discounted same-day empty places in nearby playhouses, workshops, swimming sessions, sports classes, museum activities and parent-child programs.

## Phase 2 — architecture and contract

Spawn `solution_architect`. Create:

- `docs/ARCHITECTURE.md`;
- `docs/API_CONTRACT.md` or an OpenAPI source file;
- a concrete repository structure;
- domain entities and booking state transitions;
- authentication and authorization rules;
- a transactional overbooking-prevention design;
- local-development and seed-data strategy.

The agreed contract must be stable before backend and Android implementation diverge.

## Phase 3 — implementation

Run `backend_engineer` and `android_engineer` in parallel where safe.

Backend requirements:

- ASP.NET Core on a current supported .NET version;
- PostgreSQL and EF Core migrations;
- JWT registration, login and current-user endpoint;
- offer list, search, filters, sorting and details;
- provider details;
- create/list/detail/cancel bookings;
- favorites;
- expiry and capacity enforcement;
- database-backed overbooking protection;
- deterministic demo seed data and documented credentials;
- Swagger/OpenAPI, health endpoint, Dockerfile and root Docker Compose;
- unit and integration tests.

Android requirements:

- native Kotlin and Jetpack Compose;
- Material 3 and an original visual identity;
- Navigation Compose, ViewModel, Coroutines/Flow, Hilt, Retrofit/OkHttp;
- login and registration;
- nearby/time-limited offer discovery, filters and details;
- reservation and booking code or QR payload;
- active and previous bookings;
- cancellation and favorites;
- complete loading, empty, error and retry states;
- simple Android Emulator connection to the local backend;
- unit tests and a successful debug build.

Payment is mocked or pay-on-arrival for this MVP. A provider application and production deployment are out of scope.

## Phase 4 — QA and correction

Spawn `qa_engineer`. It must run the actual builds and tests, add missing automated tests, and verify the seeded end-to-end flow against the real backend where the environment permits.

At minimum test:

- registration and login;
- expired offer rejection;
- invalid capacity;
- competing reservations and overbooking prevention;
- cancellation rules;
- favorites;
- API error mapping;
- Android loading/error/retry behavior;
- browse -> detail -> reserve -> booking-code flow.

Fix all blocking and high-severity defects, then rerun the relevant commands.

## Phase 5 — independent review

Spawn `reviewer` after implementation and QA. The reviewer must not be the original implementer. Address blocking and high-severity findings, and rerun tests after fixes.

## Completion report

Update `README.md` so a new developer can start the database and backend, build the Android app, use demo credentials and complete the primary flow.

Return a final report containing:

1. selected product concept and why;
2. implemented functionality;
3. repository structure;
4. exact commands executed;
5. build and test results, including failures fixed;
6. demo credentials and local URLs;
7. known limitations and risky assumptions;
8. reviewer findings and their disposition;
9. anything that still requires human verification.

Do not merge to `main`, publish an app, deploy production infrastructure, purchase services, or add real secrets without explicit human approval.