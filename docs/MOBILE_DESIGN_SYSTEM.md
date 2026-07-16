# UgorjBe mobile design system — Phase 4

## Product expression

Phase 4 is an original **editorial mobility** system: the operational clarity of a mature map-based product combined with the warmth and visual hierarchy of a local experience marketplace.

It must feel:

- confident and immediately understandable;
- youthful without becoming childish;
- image-led and fast to scan;
- calm and trustworthy during booking;
- visually related to the administration web;
- recognisably UgorjBe rather than a clone of a reference app.

## Experience principles

1. **The map is a working surface.** Search, filters, price markers and selected-event information support a concrete decision.
2. **One scan, one decision.** Time, title, provider, price, discount and capacity dominate cards.
3. **Map and List are one Explore state.** Results, query, filters, selected offer and viewport remain shared.
4. **Images enrich but never gate the journey.** Every image surface has deterministic category artwork.
5. **Motion communicates continuity and touch.** It never delays booking or hides server-authoritative updates.
6. **Failure remains intentional.** Missing Maps key, denied location, failed image, stale response, offline backend and Lottie failure all retain a usable route.
7. **The API remains authoritative.** Capacity, price, expiry, favorite state and booking result are never invented by the UI.

## Implementation sources

- `android/app/src/main/java/hu/ugorjbe/app/ui/theme/Theme.kt`
- `android/app/src/main/java/hu/ugorjbe/app/ui/theme/DesignTokens.kt`
- `android/app/src/main/java/hu/ugorjbe/app/ui/Components.kt`
- `android/app/src/main/java/hu/ugorjbe/app/ui/ExperienceMedia.kt`
- `android/app/src/main/java/hu/ugorjbe/app/ui/Phase4DiscoveryScreen.kt`

## Color

Core brand tokens:

- Forest `#173F35`: trusted operational controls and navigation;
- Forest Deep `#0C2B24`: hero depth and dark brand surfaces;
- Coral `#EB705B`: primary action, selected marker and discount emphasis;
- Sun `#F4BD58`: scarce warm positive accent;
- Cream `#F6F2E9`: application background;
- Paper `#FFFDF7`: card and elevated content surface;
- Sage `#DCE9D9`: calm selected/supporting state.

All screen code should prefer semantic `MaterialTheme.colorScheme` roles. Literal colors are limited to named brand/category artwork and map-marker content.

No state is conveyed by color alone. Selection, capacity, error and status also use text, icon or shape.

## Typography

The app uses only distributable Android system typefaces; no font file is committed.

- display/headline: Android serif fallback for editorial warmth;
- title/body/label: Android sans-serif fallback for operational readability;
- price, capacity and booking code: heavy sans hierarchy;
- critical copy must remain readable at 200% font scale.

Serif is not used for metadata, controls or dense numeric information.

## Spacing and shape

Spacing uses the named 2/4/8/12/16/20/24/32/48 dp scale.

Shape roles:

- 10 dp: compact supporting controls;
- 16 dp: fields and primary controls;
- 22 dp: cards, panels and sheets;
- 28 dp: hero and navigation surfaces;
- pill: real badges, filters, markers and presentation switches only.

The Phase 4 brand mark uses a deliberately asymmetric rounded silhouette and the letter `U`; it is original to UgorjBe and not based on a reference logo.

## Elevation

Elevation represents task hierarchy:

- floating search, selected map preview and view switch: 7–10 dp;
- content card: 2–4 dp plus outline separation;
- persistent navigation and booking surfaces: 10–14 dp;
- nested information surfaces: tonal fill, usually no shadow.

No continuously animated shadow or blur is allowed.

## Core components

### Brand mark

`UgorjBeBrandMark` is a compact asymmetric coral/forest mark. It appears in authentication, expanded navigation and fallback surfaces. It is not used as decorative wallpaper.

### Experience image

`ExperienceImage` uses Coil `AsyncImage` and keeps category artwork rendered behind the request.

Behavior:

- valid URL: cropped to the component constraints;
- loading: category artwork stays visible;
- missing/failed URL: deterministic category gradient and Material functional icon;
- no remote image is required for discovery, detail or booking;
- list cards avoid subcomposition-heavy image APIs.

### Offer card

Phase 4 offer cards contain:

1. 188 dp image/fallback area;
2. forest category and coral discount status;
3. start time over the image;
4. provider and maximum two-line title;
5. city and optional distance;
6. original and discounted price;
7. remaining capacity and one directional action symbol.

The whole card is one accessible action. Internal metadata is not separately clickable.

### Search and filters

Search is a compact floating surface with:

- one leading search symbol;
- editable query;
- optional clear action;
- a separate forest filter control;
- a visible active-filter count.

Filters use a modal bottom sheet. Draft state is isolated until Apply. Clear restores defaults while preserving the current search query.

### Map markers

- normal offer: forest price pill with white contrast border;
- selected offer: coral price pill with increased size/elevation;
- cluster: forest circular count marker;
- marker content uses compact price text, not a copied vehicle/brand symbol;
- no decorative animation occurs while the camera moves.

### Selected map preview

Compact layouts use an image-led preview with provider, title, time, remaining places, price, dismiss and one details action. Expanded layouts use a controlled side panel.

### Navigation

Compact windows retain all four labeled top-level destinations in a tactile floating dock:

- Explore;
- Bookings;
- Favorites;
- Profile.

Selected state combines background, icon container, icon and label. Medium/expanded layouts use a forest rail. Map and List remain two presentations inside Explore.

## State surfaces

- initial list loading: geometry-matched card skeletons;
- map refresh: compact progress surface while existing markers remain;
- empty discovery/favorites/bookings: concise copy plus original Lottie/native fallback;
- stale results: existing content plus retry banner;
- contract failure: distinct non-network message;
- missing Maps key: complete List fallback;
- denied location: Budapest remains usable;
- failed image: category artwork;
- failed Lottie: native icon/surface fallback.

## Screen acceptance criteria

### Authentication

- brand-led first impression in forest/cream/coral;
- login/register values survive mode transition;
- password visibility and IME actions work;
- primary button geometry remains stable while loading;
- demo credentials remain debug-only;
- compact and expanded layouts remain usable at 200% text.

### Explore Map

- real API results appear as original price markers/clusters;
- map remains edge-to-edge and Google attribution is unobstructed;
- search/filter chrome does not dominate the canvas;
- selected preview is image-led, dismissible and accessible;
- current markers remain visible during refresh;
- stale, empty, permission and key-error states remain actionable;
- Map/List switch preserves state and does not trigger a request by itself.

### Explore List

- editorial forest header creates product identity;
- card scan order is image → time → title → provider → price/capacity;
- chip walls are avoided;
- stable keys preserve item identity;
- scroll state remains owned by Compose/navigation state;
- skeleton geometry matches the final card;
- the list remains deliberate when all images fail.

### Offer, provider and booking

- hero image/fallback, title, time, location, provider, pricing and capacity are immediately readable;
- the booking CTA remains persistent without obscuring content;
- review shows quantity, unit price, total, cancellation and pay-on-arrival disclosure;
- duplicate submission is disabled while in flight;
- capacity/expiry errors remain server-authoritative;
- success Lottie is finite and the booking code remains visible without animation;
- provider contact/accessibility information remains readable at large text sizes.

### Bookings, favorites and profile

- all routes inherit the Phase 4 theme, media fallback, typography and navigation;
- populated and empty states use the same visual generation;
- cancellation and logout remain explicit;
- local backend details remain debug-only.

## Accessibility

- minimum interactive target: 48 dp;
- meaningful icons use localized labels;
- decorative imagery/icons use null descriptions;
- selected navigation state is conveyed by indicator, icon and label;
- status and capacity use readable text;
- light/dark contrast is reviewed over image and map surfaces;
- system-disabled animation leaves final/static content visible;
- core actions remain usable when images, Maps and Lottie are unavailable.
