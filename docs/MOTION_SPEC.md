# UgorjBe motion specification — Phase 4

## Purpose

Motion communicates touch, hierarchy, continuity and confirmed state. It must not imitate a reference product, decorate every element, hide server-authoritative changes or delay booking.

Implementation tokens live in `android/app/src/main/java/hu/ugorjbe/app/ui/theme/DesignTokens.kt`.

## Timing

- **Instant — 90 ms:** tiny indicator or immediate pressed response.
- **Quick — 150 ms:** content exit, dismiss and icon replacement.
- **Standard — 240 ms:** navigation fade, Map/List change and authentication mode change.
- **Expressive — 380 ms:** skeleton pulse and a small number of bounded brand moments.

Easing:

- enter: `(0.16, 1, 0.3, 1)` emphasized deceleration;
- exit: `(0.4, 0, 1, 1)` accelerated exit;
- touch: medium spring with low bounce;
- layout settlement: no-bounce medium-low spring.

## Navigation

- authentication/session gate: Standard fade in, Quick fade out;
- top-level destinations: short fade while Navigation Compose preserves state;
- detail/provider routes: shallow horizontal movement plus fade;
- system and predictive Back remain authoritative;
- the custom navigation dock scales to 94% only while pressed, then settles by spring.

The navigation treatment is original UgorjBe motion and does not reproduce another product's tab animation.

## Explore Map/List

The same `ExploreUiState` moves between Map and List with a subtle 0.99–1.01 scale and fade. Switching presentation alone must not trigger a network request.

### Map

- camera movement never drives a decorative animation loop;
- normal price markers are static forest pills;
- selection changes the marker to coral, increases its size and reveals the event preview;
- clusters animate only through the Google Maps camera action after activation;
- current markers stay visible while a refresh is in flight;
- search-area progress is contained inside its action;
- selected-event preview uses platform layout motion only;
- closing the preview is immediate and does not move the camera.

### List

- card press uses a subtle 0.985 scale spring;
- skeletons pulse between 42% and 78% alpha using the Expressive duration;
- stable keys preserve item identity;
- refresh keeps current content and adds progress/stale feedback rather than replacing the screen;
- no Lottie or infinite animation is rendered inside a scrolling card.

## Authentication

Login and registration use crossfade and size continuity. Entered values remain in ViewModel state. The primary action keeps a stable 56 dp container while its content changes to progress.

The brand canvas contains only static abstract shapes. It does not autoplay decorative motion.

## Booking

- review uses Material modal-sheet motion;
- submit remains disabled while the request is in flight;
- confirmed booking emits one subtle haptic event;
- success Lottie runs once and stops;
- booking code and actions remain visible independently of animation progress;
- cancellation uses confirmation and no celebratory motion;
- capacity, expiry and server errors replace decoration with direct recovery information.

## Lottie

Bundled animations are unchanged from Phase 3:

- `booking_success.json`: finite success pulse;
- `empty_discovery.json`: low-priority looping empty-state orbit;
- `brand_loading.json`: compact loading pulse.

Rules:

- success never loops;
- looping animations exist only while their state surface is visible;
- no embedded raster images;
- native fallback always exists;
- animation is not required to identify the state or action;
- when platform animator scale is zero, the final/static state is shown.

## Reduced motion

`Phase3Lottie` checks the platform animator-duration scale. Compose movement is short and low-distance. Manual verification must include:

1. Developer options → Animator duration scale Off;
2. authentication mode change;
3. navigation dock selection;
4. Map/List change;
5. marker selection and preview dismissal;
6. detail navigation;
7. booking success;
8. loading and empty states.

No critical content may disappear or become unreachable with animations disabled.

## Haptics

Haptics are meaningful and rare:

- booking confirmation: one confirmation event;
- no haptic on scrolling, map camera motion, passive loading or every navigation tap;
- no repeated haptic while a request is in flight.

## Performance guardrails

- no blur or continuously animated shadow;
- no camera-driven recomposition of the complete result list;
- no continuously animated marker while the camera moves;
- no Lottie in lazy-list cards;
- no subcomposition image loading in performance-critical lists;
- no infinite animation except visible loading/empty state;
- selected marker content remains compact;
- measure startup, list scroll, Map/List switch and offer-detail opening in release-like builds.

## Benchmark journeys

`android/baselineprofile/.../CriticalJourneyBenchmark.kt` defines:

- `exploreListScroll`;
- `mapListSwitch`;
- `offerDetailOpening`.

The benchmark uses the real local API and the development demo customer. Numeric claims require an actual configured emulator/device, API level, build variant and compilation mode; compilation-only CI is not a performance measurement.
