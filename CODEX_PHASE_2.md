# Codex task: map-first redesign and administration web

Act as the lead engineer and product owner for this repository. Read `AGENTS.md`, the current implementation, `docs/PRODUCT_DECISION.md`, `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md` and this file before changing code.

The first MVP already exists. Extend and improve it without breaking the working authentication, booking, cancellation, favorites, capacity and expiry flows.

Use the configured specialist agents. Do not stop after research, plans, wireframes or scaffolding. Continue through implementation, automated testing, defect fixing, independent UX review, code review, commits, push and a draft pull request. Do not merge the pull request.

## Phase 1 — UX research and design direction

Spawn `product_designer` first.

The designer must review the current Android UI and research current official Android and Material 3 guidance. Record the final decision in `docs/UX_DIRECTION.md` with implementation-ready specifications.

Required direction unless research establishes a clearly better accessible solution:

- Keep four top-level customer destinations discoverable through primary navigation, such as Explore, Bookings, Favorites and Profile.
- On compact phones use a bottom navigation bar rather than hiding core destinations in a hamburger drawer.
- On tablets and expanded windows use adaptive navigation rail or an appropriate permanent drawer.
- Make Explore map-first.
- Treat Map and List as two views of Explore, not separate global destinations.
- Add a prominent `Lista` / `Térkép` toggle, segmented control or floating pill above the bottom navigation.
- Keep search and filters available and synchronized in both views.
- Define a substantially more modern original visual identity. Do not merely change colors or corner radii.

The redesign must define tokens for color, typography, spacing, shape, elevation, icons and motion, with light/dark themes and accessibility requirements. It must remain warm, trustworthy and family-friendly without copying Munch, Airbnb, Google Maps or other products.

## Phase 2 — architecture and API contract update

Spawn `solution_architect` after the UX direction is stable.

Update `docs/ARCHITECTURE.md` and `docs/API_CONTRACT.md` before parallel implementation. Decide and document:

- how Android retrieves offers for the visible map area, using a viewport/bounding box or another efficient spatial query;
- pagination and limits for map and list results;
- how `Search this area` behaves after camera movement;
- authorization and role design for administrators;
- provider and offer administration state transitions;
- UTC storage and Europe/Budapest input/display behavior;
- admin-web authentication/session strategy;
- CORS and local development URLs;
- Google Maps API-key configuration without committed secrets;
- Docker Compose and CI changes.

Avoid microservices. Keep the ASP.NET Core modular monolith and PostgreSQL database.

## Phase 3 — backend expansion

Spawn `backend_engineer`.

Implement backend support required by Android map discovery and the administration web.

### Map and discovery

- Support efficient retrieval of currently bookable offers in the visible map area or documented radius.
- Preserve existing text search, filters, sorting, expiry, capacity and distance behavior.
- Validate coordinates and prevent unbounded result sets.
- Return the fields Android needs for markers and selected-offer previews.
- Keep offer details and booking rules authoritative on the server.

### Administration

- Add an `admin` role with backend-enforced authorization.
- Seed one development administrator with documented demo credentials. Never reuse demo secrets for production.
- Add secure admin endpoints for provider list/detail/create/update.
- Add secure admin endpoints for offer/event list/detail/create/update and publish/unpublish/archive or equivalent documented lifecycle operations.
- Validate time ordering, booking/cancellation cutoffs, prices, capacity, age range, coordinates and required fields.
- Do not allow changes that corrupt existing bookings or make `reserved_quantity > total_capacity`.
- Customer tokens must receive 403 for every admin operation.
- Update OpenAPI and migrations.

Add integration tests covering unauthenticated 401, customer 403, admin success, validation failures, event lifecycle, map bounds/radius queries and preservation of booking invariants.

## Phase 4 — Android map-first redesign

Spawn `android_engineer` after `docs/UX_DIRECTION.md` and the updated API contract are available.

Use the official Google Maps Compose library for the native map experience.

Implement:

- a map-first Explore screen;
- event markers and selected-marker state;
- clustering when marker density warrants it;
- selected event preview using a polished card or bottom sheet;
- open-details and booking actions from the selected event;
- synchronized search and filters across map and list;
- a clear `Search this area` action after meaningful camera movement;
- a visible Map/List toggle that preserves current filters and selection where sensible;
- optional current-location permission with a clear explanation and graceful denial behavior;
- a useful Budapest fallback without requiring location permission;
- camera, loading, empty, network-error and retry states;
- adaptive bottom navigation on compact phones and navigation rail/drawer on larger windows according to the UX spec;
- a complete redesign of authentication, discovery, detail, booking, favorites, bookings and profile screens using the new design system;
- light/dark theme and accessible semantics, contrast, touch targets and scalable text.

Use the Google Maps Secrets Gradle Plugin or an equally safe documented mechanism. Do not commit a Maps API key. Add a placeholder/default that permits CI compilation without exposing a real key. Document how a developer supplies and restricts the key.

The runtime app must use the real backend. Fake data is allowed only for previews and tests.

Add or update unit/UI tests for filter state, Map/List switching, selected markers, permission denial, empty/error states and navigation. Run Android tests, lint if configured and `assembleDebug`.

## Phase 5 — administration web

Spawn `web_admin_engineer` after the admin API contract is stable. It may run in parallel with Android when files and contracts no longer conflict.

Create `web-admin/` as a modern TypeScript administration application. Use a mainstream maintainable React-based stack selected and justified by the lead. Follow `docs/UX_DIRECTION.md` and do not imitate generic legacy admin templates.

Required flows:

- administrator login/logout;
- dashboard summary;
- providers: list, search, create and edit;
- events/offers: list, search, filter, create, edit and lifecycle actions;
- Hungarian-local date/time entry with explicit conversion to UTC;
- category, description, image URL, address, coordinates, accessibility, age range, capacity, prices, start/end, booking cutoff and cancellation cutoff;
- clear validation and field-level errors;
- confirmation for destructive/state-changing actions;
- responsive and accessible layouts;
- complete loading, empty, error, unauthorized and retry states.

The web app must call the backend API and must not access PostgreSQL directly. Client-side role checks are only presentation; backend authorization remains authoritative.

Add unit/component tests and at least one browser-level smoke test for login -> create provider if needed -> create event -> publish -> verify event appears through the public API. Add lint, typecheck, test and production build commands.

## Phase 6 — local development, Docker and CI

Update local development so a new developer can run:

- PostgreSQL;
- ASP.NET Core API;
- administration web;
- Android app from Android Studio.

Extend root Docker Compose with the administration web where practical. Avoid hard-coded production URLs and secrets. Document ports, environment files, Google Maps key setup, customer and administrator demo credentials.

Update GitHub Actions so it runs:

- backend restore/build/format/unit/integration tests;
- Android unit tests and debug build;
- web install/lint/typecheck/tests/production build;
- Compose smoke test including API health and administration web availability where reliable.

## Phase 7 — QA, UX review and independent review

Spawn `qa_engineer` after implementation. Verify the real integrated system, not only isolated mocks.

At minimum verify:

- customer map and list display the same eligible events for the same filters;
- expired, unpublished and full events are not bookable;
- panning and `Search this area` return geographically appropriate results;
- denied location permission does not block discovery;
- a customer cannot call admin endpoints;
- an administrator can create and publish an event through the web UI;
- the newly published event appears in Android map/list discovery;
- existing booking, cancellation, favorite and overbooking behavior still works;
- light/dark and compact/expanded layouts have no blocking usability issues;
- no real API keys or credentials are committed.

Then have `product_designer` perform a post-implementation UX review and `reviewer` perform an independent code/security review. Fix every blocking and high-severity finding, rerun affected tests and update documentation.

## Definition of done

The phase is complete only when:

1. Android opens Explore as a working Google map with live backend events.
2. Map and List are easily switchable and share search/filter state.
3. The Android design is visibly and structurally redesigned, not superficially recolored.
4. An administrator can sign in to the web application and create/publish an event.
5. The created event can appear in Android discovery and be reserved by a customer.
6. Backend, Android and web builds/tests pass.
7. Docker/local setup and Google Maps key setup are reproducible from README instructions.
8. CI is green.
9. A draft pull request contains the full change, test evidence, screenshots where possible, known limitations and manual verification steps.

Do not merge to `main`, deploy production infrastructure, purchase Google services, publish an app or commit real secrets without explicit human approval.
