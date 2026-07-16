# AI Agent Team Working Agreements

## Mission

Build UgorjBe: an ASP.NET Core/PostgreSQL backend, a secure React administration web application and a native Kotlin/Jetpack Compose customer application for discounted, time-limited empty places in local family activities.

The MVP, map/admin expansion and Phase 3 production mobile foundation are complete. `CODEX_PHASE_4.md` is the active specification on this branch. Phase 4 raises the Android customer experience to mature mobility/marketplace product quality without copying another product or changing authoritative business behavior.

## Active Phase 4 workflow

The engineering lead must:

1. preserve the tested Phase 3 head as the implementation base;
2. research only lawfully public, preferably official reference material;
3. separate reusable design principles from distinctive elements that must not be copied;
4. define an original UgorjBe editorial-mobility direction;
5. polish every customer-facing Android route using the real API;
6. retain map/list state, booking, cancellation, favorites and failure behavior;
7. extend accessibility, visual, contract and performance verification;
8. perform explicit originality, QA, performance and engineering reviews;
9. fix all blocking and high-severity findings;
10. report exact evidence and device-dependent limitations in a draft pull request.

Do not stop at research, documentation, previews or partial screens.

## Product direction

- Immediately polished, confident and trustworthy.
- Youthful without becoming childish.
- Bold, image-led and fast to scan without visual clutter.
- Map-native, tactile and smoothly animated.
- Visually coherent with the administration web's forest, cream, coral and warm editorial language.
- Clearly and originally UgorjBe.
- No Flutter, WebView, second UI runtime, social feed, stories, reels, autoplay or dark patterns.

Reference quality may inform hierarchy, task clarity, scanning speed and feedback. Exact reference colors, geometry, layouts, navigation, copy, icons, markers, assets or motion signatures must not be copied.

The name `cleengo` remains excluded until an exact application is identified from an authoritative source.

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
- save favorite offers or providers;
- recover from network, contract, image, Maps, permission and stale-data failures.

Phase 4 changes presentation quality, not these business rules.

## Technical baseline

### Backend and web

- .NET 8 ASP.NET Core, PostgreSQL, EF Core, JWT and backend-enforced roles remain authoritative.
- Overbooking protection, expiry, pricing and cancellation rules must not be weakened.
- The existing React administration web remains green and communicates only through authorized APIs.
- Docker Compose and Playwright smoke tests remain required.

### Android

- Kotlin and Jetpack Compose only.
- Original Material 3 light/dark token system.
- Google Maps Compose with bounded queries, custom original price markers and clustering.
- Single activity, Navigation Compose, ViewModel, Coroutines/Flow, Hilt and Retrofit/OkHttp.
- Coil image loading with deterministic category fallback.
- Original bounded Lottie animations with native/static fallback.
- Four visible top-level destinations on compact screens and an adaptive rail on larger screens.
- Map and List are two presentations of one Explore state.
- Stable lazy-list keys and preserved scroll/state behavior.
- TalkBack semantics, 48 dp targets, contrast, 200% font scale and reduced-motion behavior.
- Macrobenchmark/Baseline Profile support for startup, list scrolling, Map/List switching and detail opening.
- Release R8/minification, resource shrinking and HTTPS-only release configuration.
- Maps keys, signing material and production secrets remain local and uncommitted.

## Design rules

- The map is an operational canvas, not a decorative background.
- A selected event exposes time, price, remaining places and one clear action.
- List cards show title, provider, time, price, discount and capacity in one scan.
- Avoid chip walls and unnecessary metadata.
- Motion explains continuity and tactile response but never delays booking.
- No expensive continuous animation while the camera is moving.
- The app remains intentional when all images fail or Maps is unavailable.
- All visible Hungarian copy is resource-backed.
- No proprietary screenshots, logos, fonts, illustrations, icons, markers, animation assets or source code may be committed.

## Quality gates

Before Phase 4 can be declared complete:

- backend restore/build/format/unit/PostgreSQL integration tests pass;
- web lint/typecheck/tests/build and Playwright smoke pass;
- Android contract/unit/Compose UI tests compile and pass where supported;
- debug, androidTest and release Android builds pass;
- R8/minification and resource shrinking are verified;
- benchmark and Baseline Profile modules compile;
- connected tests and numeric benchmarks are run on an available device or explicitly documented as device-dependent;
- an admin-created published offer appears in Android Map and List and can be booked;
- booking, cancellation, favorites and recovery behavior remain intact;
- light/dark, compact/expanded, 200% text, TalkBack and reduced-motion behavior are reviewed;
- reference benchmark, art direction, design system, motion, dependency, asset, visual review and screenshot records exist;
- originality review confirms that no reference product was cloned;
- no unresolved blocking or high-severity functional, visual, accessibility, originality, security, performance or release finding remains.

## Git rules

- Never push implementation directly to `main`.
- Use focused commits on `codex/ugorjbe-phase4-reference-polish`.
- Keep pull request #4 draft until the quality gates are complete.
- Do not merge or deploy without human approval.
- Do not rewrite unrelated backend or administration-web implementation.
- New dependencies and assets require purpose, source and license records.

## Definition of done

Phase 4 is done only when a fresh developer can build debug and release variants, use a local restricted Maps key, sign in, discover real offers on the polished Map/List experience, complete booking/favorite/cancellation flows, observe safe fallbacks, run documented tests and device benchmarks, review originality evidence and inspect a green draft pull request with no unresolved blocking defect.
