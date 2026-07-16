# UgorjBe mobile design system — Phase 3

## Product expression

The Android customer app is a youthful, image-led local-experience product that remains trustworthy for parents and families. It should feel energetic when inviting discovery and calm when presenting booking-critical information.

The design is original. It does not copy TikTok, Instagram, Munch, Airbnb, Google Maps or the administration web. The administration web is used only as evidence that UgorjBe can support confident typography, warm surfaces and a deliberate brand system.

## Experience principles

1. **Show the experience before explaining it.** Real offer/provider imagery gets priority; category artwork is a deterministic first-class fallback.
2. **One scan, one decision.** Cards expose start time, title, provider, price, discount and remaining capacity without a wall of chips.
3. **Map and list are one task.** Search, filters, loaded results, selection and viewport state remain shared.
4. **Motion explains continuity.** Transitions communicate view changes, selection and success but never delay booking or obscure server updates.
5. **The API remains authoritative.** Attractive optimistic feedback never invents capacity, price, booking success or favorite state.
6. **Failure still looks intentional.** Missing images, Maps key, Lottie parsing, denied location and offline backend each have usable fallback content.

## Tokens

Implementation sources:

- `android/app/src/main/java/hu/ugorjbe/app/ui/theme/Theme.kt`
- `android/app/src/main/java/hu/ugorjbe/app/ui/theme/DesignTokens.kt`

### Color

The primary family uses electric violet rather than the previous mockup pink or a mobility-brand green. Coral signals urgency and discount. Aqua is reserved for positive availability and supporting state.

All UI uses semantic `MaterialTheme.colorScheme` roles. Literal colors are allowed only inside the named brand/category token definitions and map-marker content.

Both light and dark schemes define:

- primary, secondary and tertiary role families;
- five surface-container levels;
- outline and outline-variant roles;
- error container roles;
- scrim behavior.

No state is communicated by color alone. Text, icon, shape or status copy accompanies state color.

### Typography

The app uses the distributable Android sans-serif fallback with deliberate weight and spacing. No font file is bundled.

- display: bold campaign/brand moments only;
- headline: screen and offer hierarchy;
- title: card and section hierarchy;
- body: descriptions and operational information;
- label: metadata, filters and compact controls;
- numeric emphasis: price, booking code and capacity use bold title/display styles.

Critical copy must remain readable at 200% font scale. Fixed-height containers may be used only for controls whose labels remain single-line and accessible.

### Spacing and shapes

Spacing follows the named 2/4/8/12/16/20/24/32/48 dp scale in `UgorjBeSpacing`.

Shape roles:

- small 10–12 dp: small controls and supporting surfaces;
- medium 16–18 dp: fields, buttons and metadata panels;
- large 24–26 dp: cards, sheets and banners;
- hero 32–34 dp: authentication, large account and success surfaces;
- pill: filters, statuses and view switches.

### Elevation

Elevation is used for task hierarchy, not decoration:

- floating search, view switch and selected map preview: 7–12 dp;
- standard content card: 0–4 dp with tonal separation;
- persistent booking/navigation surfaces: 12–14 dp;
- nested information surfaces: tonal color, normally no shadow.

## Core components

### Experience image

`ExperienceImage` uses Coil `AsyncImage` and the shared app image loader. The loader supplies memory/disk behavior through Coil defaults and requests the size produced by Compose constraints.

Behavior:

- valid URL: crop to the component ratio;
- loading: category artwork remains behind the request;
- missing or failed URL: deterministic category gradient and icon;
- list cards avoid subcomposition-heavy image APIs;
- informative images use the offer/provider name as content description.

### Offer card

An offer card contains:

1. 176 dp image area;
2. category label and discount status over the image;
3. two-line maximum title;
4. provider;
5. start time, location and optional distance;
6. original/discounted price;
7. remaining-capacity status.

The whole card is a single accessible action. Internal metadata is not separately clickable.

### Search and filters

Search is a high-elevation rounded surface. It contains a search action and a separately labeled filter action. Active filter count is visible as text/number, not color alone.

Compact filters use a modal bottom sheet. Draft state is isolated from committed state until Apply. Clear restores defaults while preserving the search query.

### Navigation

Compact windows retain all four labeled top-level destinations in a rounded floating dock:

- Explore;
- Bookings;
- Favorites;
- Profile.

Medium/expanded windows use a rail. Map and List remain presentations of Explore and use a contextual floating switch.

### State surfaces

- initial list loading: geometry-matched skeleton cards;
- compact brand/loading moment: small bundled Lottie with native fallback;
- empty discovery/favorites/bookings: concise copy plus Lottie/native fallback;
- stale data: previous content remains with a retry banner;
- no data + failure: full explanatory error surface;
- API contract failure: distinct non-network copy and debug detail;
- missing Maps key: list remains accessible;
- denied location: Budapest fallback remains usable.

## Screen acceptance criteria

### Authentication

- brand-led first impression;
- login/register transition does not reset entered values;
- password visibility and keyboard actions work;
- loading/error state does not change button geometry;
- demo credentials appear only in debug.

### Explore Map/List

- real API results are used;
- custom cluster/item visuals have contrast borders;
- result markers remain while refreshing;
- selected preview is image-led and dismissible;
- Map/List switch preserves filters, selection and loaded results;
- list uses stable keys and matching skeletons;
- attribution and navigation areas remain unobstructed.

### Offer and booking

- image/fallback hero, title, time, location, provider, age, accessibility, pricing and capacity are immediately readable;
- booking CTA stays available without covering content;
- review sheet shows quantity, unit price, total, cancellation and pay-on-arrival disclosure;
- duplicate submit is prevented;
- success animation is finite and booking code stays selectable/copyable;
- expired, closed and capacity-changed states remain server-authoritative.

### Bookings, favorites, provider and profile

- every route uses the same typography, shapes, image and state system;
- empty states are responsive;
- local API data is debug-only;
- logout and cancellation remain explicit actions;
- contact and accessibility information remain readable at large font scale.

## Accessibility

- minimum interactive target: 48 dp;
- meaningful icons have localized labels;
- decorative images/icons have null descriptions;
- selected navigation state is conveyed by icon, indicator and label;
- critical status is conveyed by text;
- light/dark contrast is reviewed over map/image surfaces;
- system-disabled animation leaves final state visible;
- core actions remain usable with Lottie and remote images unavailable.
