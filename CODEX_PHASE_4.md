# UgorjBe Phase 4 — Reference-led Android polish

## Prerequisite

Phase 4 must be implemented on top of the completed Phase 3 branch or pull-request head, not the older Phase 2 `main` state. First verify that Phase 3 exists, builds, uses the real API and has passed its functional, release, accessibility and performance gates. If Phase 3 has not been pushed yet, report the missing prerequisite instead of redesigning the old app.

Preserve all working backend, admin-web, map/list, booking, favorite, release, test, Lottie, accessibility and performance behavior.

## Goal

Bring the native Kotlin/Jetpack Compose customer app to the visual-quality level of polished modern mobility and marketplace apps. GreenGo, wigo, Munch, Free2move and historically relevant SHARE NOW screens are quality references. The exact product intended by the name `cleengo` may be used only after it is identified from an authoritative source.

Match their confidence, clarity and finish, not their implementation framework or identity. Do not migrate to Flutter.

The result must be:

- immediately polished and trustworthy;
- youthful without looking childish;
- bold but uncluttered;
- image-led and fast to scan;
- map-native;
- tactile and smoothly animated;
- coherent with the successful UgorjBe administration web;
- unmistakably original UgorjBe.

## Originality rules

References are evidence, not templates. Never copy or closely reproduce another product's logo, name, exact palette, font combination, icon set, markers, proprietary assets, screenshots, text, signature animation or distinctive whole-screen composition. Do not use exact reference colors or pixel-identical geometry.

Shared principles such as map-first discovery, floating controls, large imagery, concise cards, bottom sheets and one clear primary action are acceptable only after they are redesigned for UgorjBe.

## Agent workflow

1. `reference_ui_researcher` writes `docs/REFERENCE_APP_BENCHMARK.md` from current lawful public sources.
2. `mobile_experience_designer` audits the real Phase 3 build and writes `docs/PHASE4_ART_DIRECTION.md`.
3. `solution_architect` validates state, navigation, dependency, release and test decisions.
4. `android_engineer` implements the complete visual pass.
5. `android_performance_engineer` measures and fixes regressions.
6. `qa_engineer` verifies functionality, accessibility, themes, sizes and failure states.
7. `brand_originality_reviewer` performs an independent similarity/originality review.
8. `reviewer` performs the independent engineering review.
9. The lead fixes every blocking and high-severity finding, then opens a draft PR.

Do not stop at research, a mood board, component previews, screenshots or a partial redesign.

## Benchmark requirements

For every verified reference compare:

- first impression and brand confidence;
- map canvas and floating controls;
- search and filters;
- selected-item card or sheet;
- card density and scanning speed;
- imagery and fallback behavior;
- navigation and primary actions;
- type hierarchy and numeric emphasis;
- color, contrast and dark theme;
- motion and feedback;
- empty, loading, offline, permission and error states;
- accessibility;
- reusable principle, UgorjBe adaptation and element that must not be copied.

Use official websites and official store material where possible. Record URLs and access dates. If `cleengo` remains ambiguous, mark it unresolved instead of inventing a product.

## Required design system

Create and implement an original tokenized system covering:

- semantic light and dark palettes;
- strong headline, body, label and numeric hierarchy;
- spacing, grid, shape, border, elevation and scrim tokens;
- restrained original gradients;
- image ratios and crop rules;
- custom UgorjBe icons, categories, markers and clusters;
- buttons, cards, sheets, search, filters and navigation;
- compact, medium and expanded layouts;
- edge-to-edge and system inset handling.

The main brand family must not simply reuse GreenGo green, wigo blue, Munch green or SHARE NOW teal. Avoid generic Material defaults, excessive pills, chip walls, fake glassmorphism, heavy blur and random gradients.

## Required screen pass

Every customer-facing route must belong to the same new visual generation:

- login and registration;
- Explore Map and selected-event preview;
- Explore List;
- search and filters;
- offer and provider detail;
- reservation review and success;
- booking code or QR;
- active and previous bookings;
- cancellation;
- favorites, populated and empty;
- profile and settings;
- loading, skeleton, empty, stale, offline, image-error, Lottie-error, permission-denied, map-key and retry states.

No route may remain visibly from an older phase.

## Core interaction direction

### Explore Map

- edge-to-edge map canvas;
- compact premium floating search/filter control;
- original readable markers and clusters;
- image-led selected-offer preview or controlled bottom sheet;
- clear Map/List switch that respects attribution and insets;
- results stay visible while refreshing;
- polished stale/offline/permission/empty states;
- no expensive continuous animation during camera movement.

### Explore List

- editorial image-led cards;
- title, start time, price/discount and capacity visible at a glance;
- minimal metadata rather than many chips;
- matching skeletons, stable keys and preserved scroll;
- shared continuity into detail;
- intentional appearance even when all remote images fail.

### Detail and booking

- immersive but readable hero;
- strong time, place, price and availability hierarchy;
- persistent primary booking action;
- compact supporting sections instead of generic stacked cards;
- clear provider and favorite actions;
- safe reservation review and duplicate-submit prevention;
- finite success animation and subtle haptic confirmation;
- accessible, dominant booking code/QR;
- complete closed, expired and capacity-changed states.

## Motion

Keep and refine the Phase 3 motion system:

- quick press and selection feedback;
- controlled shared transition into detail;
- stable animated Map/List switch;
- marker/card synchronization;
- smooth sheet and persistent-action transitions;
- finite success animation;
- meaningful haptics only;
- reduced-motion and disabled-animation behavior.

Do not imitate a reference app's exact easing or signature transition. Motion may not delay booking or obscure server-authoritative updates.

## Native implementation

Remain in Kotlin and Jetpack Compose. A polished cross-platform-like result means custom components, coherent tokens, precise layouts, smooth state transitions and excellent image handling. It does not mean Flutter, WebView, a second navigation framework or a second UI runtime.

## Production constraints

- Preserve unidirectional state and real API usage.
- Add no unrelated business features.
- Keep all Phase 3 release, R8, resource shrinking, Baseline Profile and accessibility gates.
- Use only maintained, license-compatible dependencies when justified.
- Commit no API keys, signing material, licensed fonts or proprietary reference assets.
- The app must remain useful without images, Lottie, location permission, Maps key or backend connectivity.
- Validate dark theme, 200% font scaling and compact/medium/expanded windows.

## Evidence and tests

Capture `docs/screenshots/phase4/` evidence for all key routes, light/dark and compact/expanded states. Write `docs/PHASE4_VISUAL_REVIEW.md` with:

- principles adopted;
- originality decisions;
- Phase 3 versus Phase 4 before/after evidence;
- accessibility results;
- performance comparison;
- remaining limitations;
- confirmation that no proprietary reference assets were included.

Run all Phase 3/backend/web gates plus screenshot/golden tests, Compose UI tests, large-font/theme checks, release build, Baseline Profile verification and benchmarks for startup, list scrolling, Map/List switching and opening detail. Verify with Docker that an admin-created published event appears and can be booked.

## Deliverables

- `docs/REFERENCE_APP_BENCHMARK.md`;
- `docs/PHASE4_ART_DIRECTION.md`;
- updated mobile design/motion/dependency/asset records;
- completed Android visual implementation;
- updated tests, screenshots, benchmarks and README;
- `docs/PHASE4_VISUAL_REVIEW.md`;
- draft pull request against `main` after using the completed Phase 3 base.

## Completion rule

Phase 4 is complete only when the app is functionally equal to or better than Phase 3, visually coherent across every route and state, connected to the real API, accessible, measured, release-buildable, clearly original and independently reviewed with no unresolved blocking or high-severity findings.