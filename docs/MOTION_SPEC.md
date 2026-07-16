# UgorjBe motion specification — Phase 3

## Purpose

Motion communicates continuity, hierarchy, state change and confirmed success. It must not decorate every element, hide server-authoritative changes or delay a customer from booking.

Implementation tokens live in `DesignTokens.kt`.

## Timing

- **Instant — 90 ms:** pressed-state and tiny indicator response.
- **Quick — 160 ms:** content exit, icon swap, dismiss.
- **Standard — 260 ms:** view switch, navigation fade/slide, form state change.
- **Expressive — 420 ms:** skeleton pulse and a small number of brand moments.

Preferred easing:

- enter: emphasized deceleration `(0.2, 0, 0, 1)`;
- exit: accelerated `(0.4, 0, 1, 1)`;
- tactile selection: medium-bouncy spring;
- final layout settlement: no-bounce medium-low spring.

## Navigation

- authentication/session gate: crossfade, 260/160 ms;
- top-level destinations: fade, preserving nested state;
- detail routes: 20% horizontal slide plus fade;
- system/predictive Back remains authoritative; transitions do not replace platform back behavior.

## Explore

### Map/List

The same result state transitions between two presentations using a subtle 0.985–1.015 scale and fade. No new network request is caused solely by switching presentation.

### Map

- never animate layout continuously during camera movement;
- cluster activation performs the map camera animation only;
- marker selection changes size/color and reveals the selected preview;
- stale results retain marker positions and show a banner;
- search-area progress stays in the action surface.

### List

- skeletons pulse between 42% and 78% alpha using the Expressive duration;
- stable keys preserve item identity;
- result refresh retains content rather than replacing it with a full-screen spinner.

## Authentication

Login/register uses crossfade and size continuity. Entered values remain in ViewModel state. The primary action keeps a stable 56 dp container while swapping its label for progress.

## Booking

- review sheet follows Material modal-sheet motion;
- the submit button is disabled while the request is in flight;
- confirmed booking triggers one subtle haptic event;
- success Lottie runs once and stops;
- booking code and actions are visible independently of animation progress.

## Favorites and cancellation

Favorite icon state may animate visually but the stored state remains the server result. Failure restores or retains authoritative state and shows an error. Cancellation is preceded by confirmation and uses no celebratory motion.

## Lottie

Bundled animations:

- `booking_success.json`: finite success pulse;
- `empty_discovery.json`: low-priority looping empty-state orbit;
- `brand_loading.json`: small loading/brand pulse.

Rules:

- success never loops;
- looping animations are shown only while their state surface is on screen;
- no embedded raster images;
- native icon/surface fallback always exists;
- animation is not required to identify the state or action;
- when the system animator scale is zero, the final/static state is displayed.

## Reduced motion

`Phase3Lottie` reads the platform animator-duration scale and switches to the final frame when disabled. Compose transitions are deliberately short and low-distance. Manual verification must include:

1. Developer options → Animator duration scale Off;
2. authentication mode change;
3. Map/List change;
4. detail navigation;
5. booking success;
6. empty discovery/favorites.

No critical content may disappear when animation is disabled.

## Performance guardrails

- no blur or continuously animated shadow;
- no Lottie in scrolling cards;
- no subcomposition-based image loader in lazy lists;
- no infinite animation except visible loading/empty state;
- no camera-driven recomposition of the complete list;
- measure startup, list scroll, Map/List switch and detail opening in release-like builds.
