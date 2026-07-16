# AI Agent Team Working Agreements

## Mission

Build UgorjBe: an ASP.NET Core/PostgreSQL backend, a secure React administration web application and a native Kotlin/Jetpack Compose customer application for discounted, time-limited empty places in local family activities.

The booking MVP and Phase 2 map/admin expansion are complete. `CODEX_PHASE_3.md` is the active specification on this branch. Phase 3 replaces the Android presentation layer with a production-ready visual, motion, image, accessibility, performance and release foundation while preserving authoritative business behavior.

## Active Phase 3 workflow

The engineering lead must:

1. reproduce and fix Android/API integration defects before visual work;
2. lock representative JSON contracts with regression tests;
3. define the production mobile design and motion systems;
4. validate dependency, navigation, image, Lottie, release and test decisions;
5. implement the complete Android redesign using the real API;
6. add release optimization, Macrobenchmark and Baseline Profile support;
7. verify functionality, accessibility, themes, sizes and failure states;
8. perform an independent engineering/design review;
9. fix all blocking and high-severity findings;
10. report exact commands, results, evidence and limitations in the draft PR.

Do not stop at research, documentation, previews, scaffolding or a debug-only build.

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

Phase 3 changes presentation and production quality, not these business rules.

## Administrator journey

The existing web application remains functional. An administrator can sign in, manage providers, create/edit/publish/unpublish/archive offers and enter Budapest-local times that the API converts to UTC.

The web client must use authorized APIs and never access PostgreSQL directly. Phase 3 must not redesign the administration web except for a narrowly required compatibility correction.

## Technical baseline

### Backend

- .NET 8 ASP.NET Core Web API.
- PostgreSQL and Entity Framework Core migrations.
- JWT authentication and backend-enforced roles.
- OpenAPI, Docker Compose, development seed and automated tests.
- Database-backed overbooking protection.
- Efficient bounded geographic queries.
- Representative API/Android JSON contract tests.

### Android

- Kotlin and Jetpack Compose; no Flutter or WebView UI.
- Original tokenized Material 3 light/dark identity.
- Google Maps Compose and clustering.
- Single activity, Navigation Compose, ViewModel, Coroutines/Flow, Hilt and Retrofit/OkHttp.
- Maintained cached image loading with deterministic category fallback.
- Small purposeful original Lottie animations with native/static fallback.
- Compose motion for hierarchy, continuity, state and success.
- Clear loading, skeleton, empty, stale, contract-error, network-error, permission-denied, map-unavailable, image-error and retry states.
- Compact, medium and expanded layouts.
- TalkBack semantics, 48 dp targets, contrast, 200% font scale and reduced-motion behavior.
- Macrobenchmark/Baseline Profile and release-like measurement.
- Release R8/minification and safe resource shrinking.
- Debug emulator backend access; no debug API URL or cleartext traffic in release.
- Maps keys and signing material remain local and uncommitted.

### Administration web

- Existing TypeScript/React implementation remains green.
- Role-aware API integration only.
- Lint, typecheck, tests, production build and browser smoke remain required.

## Design rules

- Youthful but not childish; expressive but not noisy; premium but approachable.
- Image-led and fast to scan while still trustworthy for parents and families.
- Original UgorjBe identity; do not copy TikTok, Instagram, Munch, Airbnb, Google Maps or another product's distinctive UI, assets, copy or motion signature.
- Keep all four compact top-level destinations visible.
- Treat Map and List as two presentations of Explore with shared state.
- Motion must never delay booking or hide server-authoritative updates.
- Lottie is purposeful, licensed/original, bounded and optional to comprehension.
- The app remains intentional when every image fails, Maps is unavailable, location is denied or the backend is offline.
- No feeds, stories, reels, autoplay media, addictive mechanics or dark patterns.
- Hungarian is the demo locale and all visible copy is resource-backed.

## Quality gates

Before Phase 3 can be declared complete:

- backend restore/build/format/unit/integration tests pass;
- web lint/typecheck/tests/build and browser smoke pass;
- Android contract/unit/UI/accessibility tests pass;
- debug and release Android builds pass;
- R8/minification and resource shrinking are verified;
- Docker Compose starts backend, database and admin web;
- seeded and admin-created published offers appear in Android Map and List;
- discovery -> detail -> reserve -> success -> booking code -> cancellation works;
- favorites and error recovery work;
- light/dark, compact/expanded, large text and reduced-motion behavior are reviewed;
- Lottie, image, Maps and network failures degrade safely;
- Baseline Profile generation and benchmark commands are documented and measured on an available device/emulator;
- design, motion, dependency, asset and test reports exist;
- no secrets, API keys, signing files, proprietary fonts or unlicensed assets are committed;
- no unresolved blocking or high-severity review finding remains.

## Git rules

- Never push implementation directly to `main`.
- Use focused commits on `chatgpt/phase3-production-mobile`.
- Keep the pull request draft until quality gates are complete.
- Do not merge or deploy without human approval.
- Do not rewrite unrelated backend or web files.
- Dependency and asset additions require purpose, source and license records.

## Definition of done

Phase 3 is done only when a fresh developer can start the stack, supply a local Maps key, build debug and release variants, sign in, discover real offers on Map/List, complete booking/favorite/cancellation flows, observe the production visual/motion system and safe fallbacks, run documented tests/benchmarks and review a draft pull request containing evidence and no unresolved blocking defect.
