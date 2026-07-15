# UgorjBe Phase 3 — Production Mobile Experience

## Status

This phase replaces the current Android presentation layer with a production-ready, original, high-polish customer experience. It preserves the working product, backend rules, map discovery, administration web and API contract except where a verified defect or a narrowly required presentation field must be fixed.

The administration web is the current quality benchmark for brand coherence. The Android app should feel like the same product family while remaining a native mobile experience rather than a web layout copied into Compose.

## Product goal

Create an Android experience that feels current and desirable to a visually demanding 16–25-year-old audience without excluding parents and families. The app should be image-first, fast to scan, warm, expressive and confidently modern. It may learn from the visual literacy of current social and lifestyle apps—strong imagery, bold hierarchy, fluid motion, tactile controls and immediate feedback—but must not copy TikTok, Instagram, Munch, Airbnb, Google Maps or another product's distinctive screens, branding, assets or interaction signatures.

This is not a palette refresh and not a static mockup. The result must be a maintainable, accessible, testable, performant production implementation backed by the real API.

## Non-negotiable first step

Before visual implementation, reproduce and fix all current Android/API integration defects. In particular, verify the real JSON contract for `GET /api/offers/map` against the Android DTO and add regression coverage for successful deserialization. A `200 OK` response followed by a Moshi/Retrofit parsing failure is a blocking defect. Do not hide contract errors behind generic network messages.

After the fix, prove that seeded and admin-created published offers appear in both Map and List before beginning the redesign.

## Required agent workflow

The lead agent must orchestrate the configured specialists and may not stop at research, documentation, wireframes or scaffolding.

1. `product_designer` audits the current Android app, the successful administration web and the Phase 2 UX specification. It writes the target audience, experience principles and screen inventory.
2. `mobile_experience_designer` owns the production mobile art direction, component system and motion specification in `docs/MOBILE_DESIGN_SYSTEM.md` and `docs/MOTION_SPEC.md`.
3. `solution_architect` reviews dependency choices, state ownership, navigation transitions, image loading, Lottie asset strategy, release configuration and test architecture. Avoid architectural churn unrelated to this phase.
4. `android_engineer` implements the full redesign using the real backend and the approved specifications.
5. `android_performance_engineer` adds or updates performance measurement, Baseline Profiles, release optimization and reports measurable findings.
6. `qa_engineer` verifies functionality, accessibility, visual states, device sizes and regression coverage.
7. `mobile_experience_designer` performs a post-implementation visual and motion review from screenshots/video or a running build and records concrete defects.
8. `reviewer` performs an independent correctness, maintainability, security, dependency, performance and accessibility review.
9. The lead fixes all blocking and high-severity findings, runs every quality gate and creates a draft pull request.

## Experience direction

### Personality

The Android product should feel:

- youthful but not childish;
- energetic but not noisy;
- premium but not exclusive;
- playful in motion, calm in information architecture;
- image-led, with useful text hierarchy;
- warm and human rather than corporate;
- unmistakably UgorjBe and visually related to the administration web.

Avoid generic default Material screens, excessive pastel-on-pastel surfaces, giant empty cards, decorative icons replacing content, dense chip walls and motion that delays tasks.

### Visual system

Create a complete tokenized system rather than scattered literal values:

- original light and dark palettes with semantic roles and accessible contrast;
- display, title, body, label and numeric styles with deliberate hierarchy;
- spacing, grid, shape, border, elevation and scrim tokens;
- category accents that remain legible and do not communicate state by color alone;
- a distinctive but restrained gradient language;
- image aspect ratios and crop rules;
- icon sizing and stroke guidance;
- navigation, card, sheet, button, chip, search and status components;
- compact, medium and expanded window behavior;
- proper edge-to-edge and system inset handling.

Do not download or bundle proprietary fonts. Prefer a high-quality system/Google-font strategy that is legally distributable and does not expose font files as project artifacts. The app must remain usable if downloadable fonts are unavailable.

### Imagery

Use real offer/provider images when valid and a polished category-specific fallback when missing or failed.

Use a maintained Compose image-loading library after verifying its current stable release. Configure:

- memory/disk caching;
- correct request sizing;
- crossfade only where it helps;
- deterministic loading, error and fallback states;
- content descriptions where the image conveys information;
- no subcomposition-heavy image component in performance-critical lazy lists unless justified;
- no unstable public demo URLs as the only way the app looks complete.

The design must still look intentional with all image URLs missing.

## Motion system

Motion must communicate hierarchy, continuity, state and success. It must not exist merely to decorate every interaction.

### Native Compose motion

Use official Compose animation APIs where appropriate:

- shared element/container transitions from offer card or selected map preview into offer detail;
- coordinated navigation enter/exit transitions;
- animated list insertion/removal and reordering;
- `AnimatedContent`/`AnimatedVisibility` for meaningful state changes;
- animated Map/List presentation switch;
- animated active navigation indicator;
- press, selection, favorite and filter-count microinteractions;
- spring/tween specifications defined as named motion tokens;
- predictive-back-compatible navigation where supported.

Never animate layout continuously while the map camera is moving. Avoid unnecessary recomposition and expensive blur/shadow effects on scrolling/map-heavy surfaces.

### Lottie

Integrate the maintained Airbnb Lottie Compose library using a current stable version verified at implementation time.

Use small, purposeful Lottie animations for at least:

1. booking/reservation success;
2. a polished empty discovery or empty favorites state;
3. one lightweight loading/brand moment, only where it does not block content.

Requirements:

- bundle only original or clearly permissively licensed assets;
- record origin, author and license in `docs/THIRD_PARTY_ASSETS.md`;
- do not fetch arbitrary LottieFiles assets without checking redistribution rights;
- keep animation files small and avoid embedded raster images when possible;
- provide a static or native Compose fallback if parsing/rendering fails;
- do not loop success animations forever;
- stop/pause work when off-screen;
- respect system animation/reduced-motion expectations and provide a low-motion path;
- animations must never be required to understand or complete an action.

### Haptics and sound

Use subtle platform haptics only for meaningful events such as confirmed favorite state or successful reservation. Do not add sound. Do not vibrate on routine navigation or every tap.

## Required screen redesign

Redesign and implement all of the following. No route may remain as an obviously older visual system.

### Authentication

- original brand-led sign-in/register composition;
- strong first impression without blocking sign-in;
- animated but fast transition between login and registration;
- password visibility, autofill, keyboard actions and field-level validation;
- development demo credentials clearly separated from production UI;
- loading and API errors without layout jumps.

### Explore Map

- map remains the primary canvas;
- polished floating search/filter treatment with active state and count;
- distinctive custom marker/cluster visuals that remain readable;
- tactile selected-offer preview with real imagery/fallback;
- modern Map/List control that does not obstruct attribution or content;
- refined stale, loading, empty, map-key, permission and network states;
- smooth but controlled camera/selection transitions;
- current results must remain visible during refresh.

### Explore List

- visually strong editorial cards or a justified compact grid/list pattern;
- immediate time, title, image, price/discount and remaining-capacity hierarchy;
- skeleton loading rather than a blank global spinner;
- animated filter/search result changes without destabilizing scroll;
- shared continuity into detail;
- efficient lazy rendering and stable item keys.

### Offer detail

- immersive image/header treatment with readable system bars;
- clear title, provider, time, location, age, accessibility and pricing hierarchy;
- sticky or persistent primary booking action without covering content;
- favorite feedback and provider navigation;
- shared transition from originating card/preview where supported;
- graceful missing/failed image state;
- complete bookable, closed, capacity-changed and expired states.

### Reservation and success

- confident quantity and price review;
- prevent accidental duplicate submission;
- visible server-authoritative errors;
- polished booking success with finite Lottie animation;
- booking code/QR payload remains readable, selectable/copyable and accessible;
- clear next actions.

### Bookings

- modern active/previous presentation;
- strong upcoming booking card with date/time prominence;
- empty and error states using the new system;
- detail/cancellation flow with confirmation and success feedback.

### Favorites

- offers/providers switch remains clear;
- image-led saved cards;
- responsive empty state with Lottie/static fallback;
- optimistic-looking animation is allowed only when final state remains server-authoritative and failures restore the prior state visibly.

### Profile

- coherent account header;
- settings grouped into modern sections;
- theme preference if implemented must support System/Light/Dark and persist safely;
- local development API information must be debug-only;
- clear logout action and confirmation where appropriate.

### Filters and sheets

- compact devices use a refined modal bottom sheet;
- larger windows use an appropriate side sheet/dialog/pane;
- applied state, clear/reset and result action are obvious;
- input controls remain accessible at large font sizes;
- sheet transitions follow motion tokens and do not trap focus.

## Navigation

Preserve the four top-level destinations: Explore, Bookings, Favorites and Profile. Do not hide them in a hamburger menu on compact devices.

The compact navigation may become a distinctive floating rounded dock or other original bottom treatment if it:

- keeps all four destinations visible and labeled appropriately;
- provides at least 48 dp touch targets;
- handles gesture/navigation insets;
- does not cover map attribution, actions or scroll content;
- has a clear selected state beyond color;
- adapts to rail/drawer/panes on larger windows.

Map and List remain two presentations of Explore, not separate global destinations.

## Dependencies

Add dependencies only when they solve a clear requirement and are maintained, license-compatible and compatible with the current Android/Compose toolchain.

Expected evaluation set:

- Airbnb Lottie Compose for bundled vector animations;
- Coil 3 Compose or another justified maintained image loader;
- official Compose animation/shared transition APIs;
- AndroidX adaptive/navigation components already used or justified;
- AndroidX Macrobenchmark/Baseline Profile tooling for production performance.

Do not add a large UI framework, a second navigation framework, an unmaintained shimmer package, a generic animation collection or a dependency solely to reproduce a one-line Compose effect. Record every new dependency and reason in `docs/ANDROID_DEPENDENCIES.md`.

## Production readiness

### Architecture and code quality

- preserve unidirectional state and existing repositories/ViewModels unless a measured problem requires change;
- split oversized screen files into cohesive components without creating a component-per-line architecture;
- no business logic in composables;
- no mock repository as normal runtime source;
- all strings localized in resources;
- previews use explicit fixtures and never leak into runtime;
- avoid hardcoded colors, dimensions and animation durations outside tokens;
- use stable keys and immutable models where appropriate;
- surface parse/contract errors distinctly in debug logging and tests.

### Accessibility

Verify:

- TalkBack labels, roles, headings, state descriptions and traversal order;
- minimum 48 dp interactive targets;
- contrast in light/dark themes and over images/maps;
- no information conveyed only by color or animation;
- font scaling through at least 200% without clipped critical actions;
- landscape, keyboard and switch-access-relevant focus behavior;
- reduced/system-disabled animation path;
- content remains usable when Lottie or network images fail.

### Performance

Add a `baselineprofile` or equivalent benchmark module if absent and measure release-like builds.

Create benchmark journeys for at least:

- cold startup to first useful authenticated/unauthenticated screen;
- login or demo sign-in journey as practical;
- Explore list scroll;
- Map/List switch;
- open offer detail;
- reservation review/success path using a controlled test backend or fixture mode isolated to benchmark/test builds.

Generate and commit a Baseline Profile for critical journeys. Report startup and frame/jank metrics with device/emulator details. Do not claim a numeric improvement without before/after measurements. Ensure Lottie, image loading, gradients and transitions do not introduce visible jank on the agreed reference emulator/device.

### Release build

- `release` build must compile with R8/minification enabled;
- enable resource shrinking where safe and fix keep rules rather than disabling optimization globally;
- no debug API URL, demo credential banner, logging body or secret in release output;
- no cleartext traffic in release;
- verify Maps key handling remains safe;
- produce a signed-local or unsigned release artifact as appropriate without committing signing material;
- document release configuration and known production placeholders.

## Tests and visual evidence

Add or update:

- DTO/contract deserialization tests using real representative backend JSON;
- ViewModel tests for all major states and stale/error recovery;
- Compose UI tests for navigation, search/filter, Map/List, reservation and favorites;
- animation tests with clocks controlled where meaningful;
- screenshot/golden tests using a currently maintained approach selected by the architect for key screens in light/dark and at least compact/expanded sizes;
- accessibility checks/semantics assertions for critical routes;
- Macrobenchmark and Baseline Profile generation;
- a manual test checklist for map gestures, real images, Lottie fallback and reduced animation.

Capture review artifacts under `docs/screenshots/phase3/` and, where practical, a short screen recording or animated evidence outside the Git repository if file size would be excessive. At minimum capture:

- authentication;
- Explore Map with markers and selected preview;
- Explore List;
- offer detail;
- reservation success;
- active bookings;
- favorites empty and populated;
- profile;
- light and dark themes;
- compact and expanded layouts.

## Quality gates

Before completion, all existing backend and web gates must remain green. At minimum run and report:

```powershell
# Backend regression
dotnet restore backend/UgorjBe.sln --locked-mode
dotnet build backend/UgorjBe.sln -c Release --no-restore
dotnet test backend/tests/UgorjBe.UnitTests/UgorjBe.UnitTests.csproj -c Release --no-build
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj -c Release --no-build

# Android
Set-Location android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --no-daemon
.\gradlew.bat assembleRelease --no-daemon
# Run connected UI tests, screenshot tests and benchmark/profile tasks on a configured emulator/device.
```

Also verify manually against the running Docker stack:

1. customer sign-in;
2. map and list show real published offers;
3. search and filters preserve state;
4. offer detail opens with image/fallback and correct data;
5. reservation succeeds and Lottie completes without blocking;
6. booking appears and cancellation still works;
7. favorites add/remove recovers from server failure;
8. dark mode, large fonts and rotation/resizing preserve tasks;
9. Maps key missing and backend offline states remain useful;
10. admin-created published offer appears on Android.

## Out of scope

- redesigning the administration web unless a tiny brand-token correction is needed;
- new marketplace business features;
- payments, chat, social feeds, stories, reels or addictive engagement mechanics;
- copying another application;
- production deployment;
- purchasing assets, fonts, APIs or services;
- committing credentials, signing keys or Maps API keys.

## Deliverables

- `docs/MOBILE_DESIGN_SYSTEM.md`;
- `docs/MOTION_SPEC.md`;
- `docs/ANDROID_DEPENDENCIES.md`;
- `docs/THIRD_PARTY_ASSETS.md`;
- `docs/PHASE3_TEST_REPORT.md`;
- updated Android implementation and tests;
- Baseline Profile/benchmark setup and measured report;
- updated README with exact setup/build/release/test instructions;
- review screenshots;
- a draft pull request against `main`.

## Completion rule

Phase 3 is complete only when the redesigned app is demonstrably connected to the real API, all primary customer flows work, every major screen belongs to the same production visual system, motion and Lottie degrade safely, release and debug builds pass, performance is measured, accessibility is checked, and an independent reviewer has no unresolved blocking or high-severity findings.
