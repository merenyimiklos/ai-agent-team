# UgorjBe Phase 2 UX direction

**Status:** implementation-ready and frozen for Phase 2<br>
**Date:** 2026-07-15<br>
**Scope:** native Android customer experience and responsive administration web<br>
**Default locale:** Hungarian (`hu-HU`); all visible copy remains in localization resources

## 1. Decision

UgorjBe will become a **map-first, place-aware activity finder** with a calm editorial layer rather than a map utility with marketplace controls pasted over it. The primary Android journey remains authenticate → discover → inspect → reserve → booking code. Existing booking, cancellation, favorite, capacity and expiry behavior is preserved; this document changes presentation and interaction, not business rules.

The signed-in customer shell has exactly four top-level destinations:

1. **Felfedezés** (Explore)
2. **Foglalások** (Bookings)
3. **Kedvencek** (Favorites)
4. **Profil** (Profile)

Explore opens in **Map** mode. Map and List are two presentations of one Explore route and one state holder, never separate navigation destinations. Search, filters, result set, selected offer and last searched map area survive switching. The switch is always named with the destination view: `Lista` while on Map and `Térkép` while on List.

The administration web uses the same brand system but a denser, task-oriented composition. It must feel like a current UgorjBe workspace, not a generic legacy dashboard. It uses backend data exclusively.

## 2. Evidence and implications

This direction follows current first-party guidance:

- Android recommends adapting components rather than merely stretching them and identifies `NavigationSuiteScaffold` as the standard way to switch between navigation bar and rail. Window size is dynamic, so navigation and panes must react without losing state ([Android adaptive apps](https://developer.android.com/develop/adaptive-apps/guides/get-started-with-adaptive-apps), [window size classes](https://developer.android.com/develop/adaptive-apps/guides/use-window-size-classes)).
- The adaptive list-detail pattern presents one pane on small windows and related panes side by side on large windows. This supports a map plus selected-offer pane, and list plus detail pane, without inventing tablet-only navigation ([Android list-detail guidance](https://developer.android.com/develop/adaptive-apps/guides/list-detail)).
- Material 3 treats color roles, type scale and motion as a coordinated system rather than isolated decoration. The token baseline below uses those semantic concepts while retaining an original UgorjBe expression ([Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3), [color roles](https://m3.material.io/styles/color/roles), [type-scale tokens](https://m3.material.io/styles/typography/type-scale-tokens), [motion](https://m3.material.io/styles/motion/overview)).
- Maps Compose exposes declarative map, camera, marker, property and UI-setting state. The Android Maps utility library provides marker clustering specifically to preserve readability as density rises ([Maps Compose](https://developers.google.com/maps/documentation/android-sdk/maps-compose), [marker clustering](https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering)).
- Android permission guidance says to ask in context, allow cancellation, degrade gracefully after denial and not block the app. Approximate location is sufficient for nearby discovery and precise/background location is unnecessary ([runtime permissions](https://developer.android.com/training/permissions/requesting), [Maps location guidance](https://developers.google.com/maps/documentation/android-sdk/location)).
- Compose semantics provide names, roles, states, actions and predictable testing hooks. Android recommends at least 48 × 48 dp touch targets ([Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility), [semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics), [accessible Android apps](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views)).
- The administration web targets WCAG 2.2 AA, including contrast, keyboard access, visible focus, reflow, error identification and minimum pointer target spacing ([WCAG 2.2](https://www.w3.org/TR/WCAG22/)).

These sources do not prescribe an UgorjBe-specific map layout. The interaction model below is an original product decision made for short-lived family activity inventory.

## 3. Audit of the current Android MVP

Keep:

- the four existing top-level destinations and authentication gate;
- the localized Hungarian journey and explicit loading/empty/error/retry branches;
- authoritative server responses for booking and cancellation;
- offer/provider favorites and active/previous bookings;
- the warm, human tone and pay-on-arrival disclosure.

Replace or extend:

- current Explore is list-only and must become map-first;
- the fixed bottom `NavigationBar` must become window-adaptive;
- filters currently live in a modal dialog with no shared map viewport state;
- cards use large decorative placeholders and uniform vertical stacks; introduce real image treatment, stronger information hierarchy and compact preview variants;
- detail and secondary screens lack persistent actions and large-window compositions;
- the theme defines partial light/dark colors but no explicit type, shape, spacing, elevation, icon or motion system;
- location permission, camera movement, selection, clustering and stale-result states do not exist.

The redesign is structural: edge-to-edge map canvas, layered controls, shared Map/List state, adaptive navigation, responsive panes, sticky task actions, a complete token system and purpose-specific cards. A palette swap alone does not meet this direction.

## 4. Customer information architecture and adaptive shell

### Compact windows

At width class **Compact** (normally below 600 dp):

- show a four-item labeled bottom navigation bar;
- show one content pane at a time;
- reserve system-inset-aware space for bottom navigation and the Explore view switch;
- detail routes replace the content pane and use predictive/system back to return with Explore state intact;
- never put a top-level destination in a hamburger drawer.

### Medium windows

At width class **Medium** (normally 600–839 dp):

- use a labeled or compact navigation rail chosen by `NavigationSuiteScaffold` recommendations and posture;
- Explore may show the map/list primary pane with a 320–360 dp selected-offer supporting pane when space and posture permit;
- otherwise retain one pane and the same selection card used on compact.

### Expanded and larger windows

At width class **Expanded** and above (normally 840 dp+):

- use a navigation rail; a permanent drawer is acceptable only if all four destinations remain immediately visible and content width remains useful;
- cap reading/form content to 1200 dp and place related content in panes rather than stretching it;
- Explore Map uses a flexible map pane and a 360–420 dp preview/detail pane;
- Explore List uses a 420–480 dp results pane and a flexible detail pane when an offer is selected;
- Bookings and Favorites use list-detail composition where helpful;
- hinge/fold occlusion is treated as a gutter, never covered by essential controls.

Use `currentWindowAdaptiveInfo()` and adaptive navigation components. Keep destination, Explore mode, filter, selection and last successful viewport state in a `ViewModel`/saved state so resizing, folding and process recreation do not reset the task.

## 5. Explore: one model, two presentations

### Shared Explore state

One `ExploreUiState` is the single source of truth for both Map and List. It contains at minimum:

- `presentation`: `MAP` or `LIST`;
- committed search query and filter object;
- filter draft while a sheet/dialog is open;
- last successfully searched bounding box and camera zoom;
- current visible camera bounding box and zoom;
- loaded offer summaries and paging/limit metadata;
- `selectedOfferId` and selected preview data;
- initial/loading/refreshing/empty/stale-error states;
- permission state and whether a rationale/settings action is appropriate;
- user location when granted, otherwise the Budapest fallback;
- `searchThisAreaVisible` and request generation ID to discard late responses.

The runtime source is the backend repository. Preview/test fixtures are not a normal runtime source.

### Top controls

In both views:

- place a rounded search field at the top safe area, 16 dp from compact edges;
- placeholder: `Program vagy szolgáltató keresése`;
- show a separate filter button with an accessible label and an active-filter count badge;
- submit keyboard search, search icon tap or a 400 ms debounced text change; cancel obsolete requests;
- keep the query and applied filters unchanged when toggling views or opening details;
- use a modal bottom sheet on Compact and a side sheet/dialog on larger windows for filters;
- provide `Szűrők törlése`, `Mégsem` and `Találatok mutatása` actions and show the expected/returned count when available.

Filters retain existing category, child age, start window, maximum price, minimum places and sort choices. Distance sorting is enabled only with a location/center coordinate and explains that dependency in text. Applied filters render as removable chips below search in List and in a horizontally scrollable row over Map. Never rely on chip color alone to convey selection.

### Map presentation

- Initial camera: Budapest center at approximately `47.4979, 19.0402`, zoom 12, unless a previously saved camera exists.
- Draw the map edge to edge beneath translucent/surface controls; honor map attribution and logo safe areas.
- Add only backend-returned, currently discoverable offers for the searched viewport.
- Default marker: primary mulberry pin with a light/dark high-contrast border. Selected marker: larger teal pin, check-shaped inner glyph and elevated halo. Sold-out/unpublished/expired offers are not map markers.
- Cluster when marker overlap/density warrants it using the Maps utility algorithm. Cluster markers display a count, have a spoken label such as `12 program ezen a területen`, and zoom/recenter on activation.
- Marker activation sets `selectedOfferId`, visually selects the marker and opens the selected-offer preview. It does not navigate immediately.
- Tapping map background clears selection but not results or filters.
- On Compact, preview is a dismissible bottom card above the Map/List switch and navigation bar. It shows image/placeholder, title, provider, local start time, age, discounted price, remaining places and `Részletek`; whole-card activation opens details.
- On Expanded, selection opens/updates the supporting pane. Keep the map camera and result markers visible.
- A locate control sits below the search/filter chrome and is never hidden behind the preview. Its content description reflects state: `Saját helyzetem megkeresése`, `Saját helyzetem`, or `Helyhozzáférés beállítása`.

### `Keresés ezen a területen`

Do not issue catalog requests continuously while the camera moves.

1. After a successful request, store its bounding box and zoom as the searched area.
2. During user camera movement, keep current markers and preview visible.
3. On camera idle, show `Keresés ezen a területen` when either the center moved by at least 15% of the visible width/height, zoom changed by at least 0.5, or the new/old viewport intersection-over-union is below 0.8.
4. Programmatic camera movement caused by initial fallback, selected marker, cluster activation or locate does not show the action until that movement is idle; locate may automatically search once after settling.
5. Activating the action queries the exact visible bounds, disables the button and shows an inline progress indicator. On success it replaces results, records the new area and clears a selection that no longer exists. On failure it keeps stale markers, marks them as stale through a non-blocking banner and offers `Újrapróbálás`.
6. Changing search text or committed filters immediately queries the current visible bounds and establishes a new baseline; it does not require an extra area-search tap.

The button is centered below applied-filter chips, has a minimum 48 dp height and must not cover Google map controls or selected content.

### List presentation

- List renders the **same loaded result set** and applied viewport/search/filter state as Map. Toggling never silently starts an unbounded or different query.
- If the user panned but did not activate area search, List continues to show the last searched results and an inline `A térkép elmozdult – keresés az új területen` action.
- Compact cards use a 112 dp image/placeholder, start-time prominence, price, discount, age, distance when available and remaining places. Avoid more than three metadata chips; secondary facts become text rows.
- Keep `selectedOfferId` as the highlighted/first-visible card where sensible. Selecting a list card updates selection before opening detail so Back returns to the same item.
- Paginate List according to the API contract. Map uses its documented bounded marker limit. If the map result is truncated, show `Nagy terület – közelíts a pontosabb találatokhoz`; never imply all offers are shown.

### Explore states

| State | Map | List |
| --- | --- | --- |
| Initial load | map canvas + centered low-obstruction progress panel | skeleton cards, not an indefinite blank spinner |
| Refresh | retain markers, progress in search-area/action chrome | retain content, top progress indicator |
| Empty | Budapest/map remains usable; centered surface says no result and offers clear filters/recenter | illustrated text state with clear filters/recenter |
| Network error, no data | full explanatory surface over map with retry; List remains available | full error state with retry |
| Network error, stale data | retain markers, persistent dismissible banner | retain list, banner with retry |
| Permission denied | map centered on Budapest; short snackbar/inline hint only | results remain available; distance may use map center |
| Map unavailable/misconfigured | explicit map error with `Lista megnyitása`; never crash or show an endless blank tile | fully usable real backend list |

## 6. Location permission model

Location is optional and is not requested on app launch or sign-in.

1. The user taps the locate control or enables a nearby/distance feature.
2. Show a short in-context rationale: approximate location is used once to center nearby activities; the app remains usable without it. Actions: `Folytatás` and `Most nem`.
3. Request foreground coarse location. Fine location may be requested together only if required by the chosen API, but the experience must accept approximate access. Never request background location.
4. If granted, center once, show the My Location layer and pass the coordinate only to relevant discovery requests.
5. If denied, keep Budapest fallback and all Map/List functionality. Do not repeatedly prompt. A later locate tap may explain the limited feature; permanent denial offers an explicit `Beállítások megnyitása` action without blocking.

Do not persist raw location history. Store at most the last camera/viewport needed for continuity. Permission and location strings must say what changes and what continues to work.

## 7. Customer screen specifications

### Authentication

- Compact: warm brand field on top, focused form surface below; Expanded: 40/60 brand-and-benefit pane plus form pane, with the form width capped at 480 dp.
- Login/register remain one stateful route with a clear mode switch, password visibility label, correct keyboard/autofill hints and inline field errors.
- Primary action is full width and stable during progress; preserve the label next to a small progress indicator to avoid layout shift.
- Demo credentials may be shown only in development/demo builds and must be copyable, never prefilled as if they were personal data.
- Authentication/server errors appear next to the form summary and move accessibility focus to the summary; field errors are also associated with fields.

### Offer detail and provider

- Use a real remote image when valid, with a category-specific two-tone placeholder and descriptive alt text only when the image adds information.
- Compact detail: edge-to-edge 4:3 hero, overlaid Back/Favorite controls, then category, title, provider, Budapest-local schedule, age, accessibility, address, availability and price in that order.
- A persistent bottom action surface shows discounted total/unit price and `Foglalok`; respect navigation/system insets. If unbookable, replace action with the authoritative reason.
- Quantity selection occurs in an accessible confirmation sheet/dialog before the booking request. Show quantity, unit price, total, pay-on-arrival and cancellation deadline. Do not infer success until the API returns a booking.
- Expanded detail uses hero/content and reservation summary panes; provider opens in a related pane or route with back continuity.
- Provider detail leads with identity/address/accessibility and current live offers; favorite action always has a visible or spoken state.

### Booking success and bookings

- Success has a concise confirmation heading, activity and start time, human-readable booking code, QR visual generated from the returned payload, `Kód másolása`, `Foglalásaim` and `Vissza a felfedezéshez`.
- Treat the QR as visual redundancy: the booking code and full check-in instruction remain available to screen readers. Do not announce QR payload punctuation by default.
- Bookings keeps `Aktív` / `Korábbi` as a segmented/tab control with status counts when available.
- Active card hierarchy: date/time, title, provider/address, quantity/price, booking code, cancellation status/action. Previous cards visually reduce emphasis but maintain readable contrast.
- Cancellation uses a confirmation dialog naming the booking and consequence. Keep the card visible while cancelling; on success announce completion and move it to Previous. Domain rejection refreshes the card and explains why.
- Empty Active offers `Programok felfedezése`; empty Previous is informational. Errors offer retry without losing the selected tab.

### Favorites

- Keep two subviews, `Programok` and `Szolgáltatók`, within Favorites—not global destinations.
- Offer cards retain unavailable saved items with a clear text status and allow details/removal. Provider cards show location and number of active offers.
- Optimistic icon feedback may be used, but rollback and announce on backend error. Never remove an item invisibly before failure is known.

### Profile

- Show initials/avatar tile, display name, email and locale; group app preferences separately from account actions.
- Expose theme choice `Rendszer / Világos / Sötét` if implemented; default to system. Location access shows current permission status and opens the in-context flow/settings.
- Logout is a lower-emphasis outlined action with confirmation only if local unsaved state exists. Logout clears the token and returns to auth.

## 8. Original visual identity: “city picnic”

The identity combines city precision with family warmth: deep mulberry for confident actions, cool teal for place/selection, saffron for urgency/discount and parchment surfaces. Layouts use asymmetric image crops, quiet whitespace, strong local-time typography and a small “trail” motif (paired dot and short line) in empty illustrations. Do not use another marketplace's logo, palette, pin shape, card proportions, copy or map chrome.

### Color tokens

Use semantic roles in code; never consume raw hex values from screens. The values below are the Phase 2 baseline and must be checked with automated/manual contrast tooling after rendering.

| Role | Light | Dark | Intended use |
| --- | --- | --- | --- |
| `primary` | `#76264B` | `#FFAFD0` | primary action, default marker |
| `onPrimary` | `#FFFFFF` | `#45102B` | content on primary |
| `primaryContainer` | `#FFD8E7` | `#5D173A` | selected controls, soft brand field |
| `onPrimaryContainer` | `#351022` | `#FFD8E7` | content on primary container |
| `secondary` | `#176A63` | `#8BD7CF` | location, selection, positive accents |
| `onSecondary` | `#FFFFFF` | `#003733` | content on secondary |
| `secondaryContainer` | `#A6F2E9` | `#004F49` | selected preview, accessibility accents |
| `onSecondaryContainer` | `#00201D` | `#A6F2E9` | content on secondary container |
| `tertiary` | `#805600` | `#F7C765` | discount/attention, never sole warning signal |
| `onTertiary` | `#FFFFFF` | `#432C00` | content on tertiary |
| `tertiaryContainer` | `#FFDEA0` | `#604000` | discount badge, informational highlight |
| `onTertiaryContainer` | `#281900` | `#FFDEA0` | content on tertiary container |
| `background` | `#FFF8F6` | `#181114` | app background |
| `onBackground` | `#21191C` | `#EEDFE3` | primary text |
| `surface` | `#FFF8F6` | `#181114` | main surfaces |
| `surfaceContainerLow` | `#FFF0F3` | `#22191D` | cards/fields |
| `surfaceContainer` | `#F9E9EE` | `#291F23` | navigation/sheets |
| `surfaceContainerHigh` | `#F2E1E7` | `#342A2E` | raised/selected surfaces |
| `onSurface` | `#21191C` | `#EEDFE3` | primary surface content |
| `onSurfaceVariant` | `#51434A` | `#D5C2C9` | secondary text |
| `outline` | `#84747B` | `#9E8D94` | boundaries |
| `outlineVariant` | `#D7C2CA` | `#51434A` | subtle dividers |
| `error` | `#BA1A1A` | `#FFB4AB` | error/destructive |
| `onError` | `#FFFFFF` | `#690005` | content on error |
| `errorContainer` | `#FFDAD6` | `#93000A` | error surfaces |
| `onErrorContainer` | `#410002` | `#FFDAD6` | content on error container |
| `scrim` | `#000000` at 40% | `#000000` at 60% | modal background |

Map overlays use opaque/sufficiently blurred `surface` at 94% rather than translucent low-contrast text. Verify marker visibility against light and dark satellite/road map styles with an outline/halo. State is always also conveyed by size, glyph, label or preview—not hue alone.

### Typography

Use the platform/system sans family for dependable Hungarian glyphs and fast loading. Use tabular figures for prices, times, capacities and booking codes where supported.

| Token | Size / line | Weight | Use |
| --- | --- | --- | --- |
| `displaySmall` | 36 / 44 sp | 700 | auth brand, success code on roomy screens |
| `headlineLarge` | 32 / 40 sp | 700 | large-window page heading |
| `headlineMedium` | 28 / 36 sp | 700 | compact page/offer title |
| `headlineSmall` | 24 / 32 sp | 650 | section/price emphasis |
| `titleLarge` | 22 / 28 sp | 650 | card/detail section title |
| `titleMedium` | 16 / 24 sp | 650 | list card title, controls |
| `bodyLarge` | 16 / 24 sp | 400 | primary reading text |
| `bodyMedium` | 14 / 20 sp | 400 | metadata/supporting copy |
| `labelLarge` | 14 / 20 sp | 650 | buttons/chips/navigation |
| `labelMedium` | 12 / 16 sp | 650 | compact badges only |

Do not use body text below 14 sp or essential labels below 12 sp. Allow natural wrapping; never reduce font size to fit translated text.

### Spacing, shape and elevation

- Spacing scale: `space-1` 4, `space-2` 8, `space-3` 12, `space-4` 16, `space-5` 20, `space-6` 24, `space-8` 32, `space-10` 40, `space-12` 48 and `space-16` 64 dp/CSS px.
- Compact content margin: 16 dp; medium: 24 dp; expanded: 32 dp. Dense admin table cells may use 12 px vertical and 16 px horizontal padding.
- Shape scale: `xs` 8, `sm` 12, `md` 16, `lg` 24, `xl` 32 dp; `pill` 50%. Inputs use 14, cards 20, dialogs 24, bottom sheets 28 dp top corners. Maps/images may use one squared edge when aligned to a screen/pane to create editorial asymmetry.
- Elevation: level 0 (base), 1 (cards/navigation, 1 dp), 2 (search/filter, 3 dp), 3 (selected preview/sheet, 6 dp), 4 (modal/FAB, 10 dp). Dark mode uses surface tone plus restrained shadow; do not rely on shadow alone for boundaries.

### Icons and imagery

- Use one Material icon family consistently: outlined when inactive, filled when selected. Standard meanings: Explore/compass, ticket/bookings, bookmark/favorites, person/profile, list, map, filter, locate, back and close.
- Every icon-only action has a localized content description/tooltip. Decorative icons have no semantics. Pair unfamiliar or consequential icons with text.
- Remote imagery uses `ContentScale.Crop`, stable aspect ratios, loading/error placeholders and no essential text baked into images. Category placeholders combine a two-tone field with a simple original icon/trail motif.
- Do not create a proprietary-lookalike map pin. UgorjBe's pin is a rounded teardrop with a small centered trail dot; selected state adds a teal halo and check glyph.

### Motion

- Durations: immediate feedback 100 ms; small state/selection 180 ms; content enter/exit 260 ms; pane/sheet 360 ms; never exceed 500 ms for task UI.
- Default easing: Material standard emphasized deceleration for enter and emphasized acceleration for exit; use spring only for selected marker/card emphasis with no more than one subtle overshoot.
- Map camera movement follows Maps SDK behavior and is not decorated with extra parallax. Toggle crossfades shared chrome and fades/slides content 180–260 ms; it does not reset camera or scroll state.
- Loading skeleton shimmer is optional and must stop when not visible. Never animate price, time or remaining-place values in a way that delays reading.
- Respect Android animator duration scale and web `prefers-reduced-motion`. At zero/reduced motion, replace movement with instant state change or a short opacity transition and disable pulsing, shimmer and overshoot.

## 9. Administration web direction

### Responsive shell

- Desktop (≥1024 px): 240 px branded side navigation with `Áttekintés`, `Szolgáltatók`, `Programok`; top bar shows environment, signed-in admin and logout. Main content max width 1440 px.
- Tablet (768–1023 px): compact 80 px icon-plus-tooltip rail or collapsible labeled navigation.
- Mobile (<768 px): top app bar and modal navigation drawer. This exception is for the task-oriented admin web, not the customer app's four primary destinations.
- Use the same semantic tokens, with denser spacing and less decorative imagery. Keep a narrow mulberry “trail” beside active navigation instead of copying common blue/gray admin templates.

### Authentication and dashboard

- Login is a focused card with brand context, email/password, visibility control, field errors and an error summary. Expired/unauthorized sessions return to login with `A munkamenet lejárt` and preserve only the intended destination, never form secrets.
- Dashboard shows useful API-backed counts: providers, draft/published/unpublished/archived events and events starting soon. Each metric links to the filtered management view.
- Add a compact `Következő programok` table and a prominent `Új program` action. Loading, empty, failed and unauthorized states are explicit.

### Providers

- List has search, result count and `Új szolgáltató`; desktop uses a real semantic table, mobile uses equivalent labeled cards.
- Row/card: name, city/address, active event count if provided, last updated and Edit.
- Create/edit form sections: identity, description/contact, address/coordinates, accessibility and imagery. Do not hide required coordinates in an “advanced” accordion.
- Use address, latitude and longitude labels together; explain WGS84 and valid ranges. Validation appears by field and in a summary linking focus to invalid fields.

### Events/offers

- List controls: search, provider, category, lifecycle status and start-date window. Status uses text plus icon, not color alone.
- Status vocabulary follows the API contract. UI actions are explicit verbs: `Közzététel`, `Közzététel visszavonása`, `Archiválás`; every state-changing action names the event and resulting visibility in a confirmation dialog.
- Create/edit uses a multi-section single page, not a fragile stepper: basics; provider/location; audience/accessibility; schedule; capacity/pricing; publishing summary. A sticky action bar offers Save draft/Save and the permitted lifecycle action.
- Hungarian local datetime inputs display a persistent `Europe/Budapest` label and preview the converted UTC instant. During DST ambiguity/nonexistence, block submission with an explicit corrective message rather than silently choosing an offset.
- Show original and discounted price side by side, currency fixed/selected explicitly, and calculate discount as read-only assistance. Capacity changes show reserved quantity; never allow total capacity below reserved quantity.
- Before publish, show a readiness summary with missing/invalid requirements. Backend validation remains authoritative and RFC 7807 field errors map back to controls.

### Web behavior and accessibility

- Use native form elements/ARIA patterns from the chosen React component layer; semantic tables remain keyboard navigable and responsive cards retain labels.
- Every request has idle/loading/success/empty/error/unauthorized states. Disable duplicate submissions while retaining the action label. On mutation success, show a polite live-region confirmation; on failure retain user-entered values.
- Destructive/lifecycle confirmation returns focus to the trigger when dismissed. Toasts never contain the only error or required action.
- Support keyboard-only operation, visible 2 px focus ring with at least 3:1 contrast, skip link, landmarks, logical heading order, browser zoom/reflow at 200%, and minimum 44 × 44 CSS px primary control targets (at least WCAG's 24 px minimum with spacing for dense table affordances).
- Dates are displayed as Hungarian local values with timezone; API payloads remain ISO-8601 UTC. Do not localize decimal serialization.

## 10. Accessibility acceptance criteria

The following are release criteria, not optional polish:

- Android interactive targets are at least 48 × 48 dp; web primary targets are 44 × 44 px and all targets meet WCAG 2.2 target-size/spacing requirements.
- Normal text contrast is at least 4.5:1; large text and essential UI graphics/focus indicators at least 3:1. Test token pairs and rendered overlays on both map themes.
- All actions have a programmatic name matching the visible label. Toggle, favorite, marker, permission and lifecycle states have `stateDescription`/ARIA state.
- TalkBack traversal reaches search → filters → area search → selected preview → view switch → primary navigation in a stable order; decorative map content does not flood focus.
- The List alternative exposes the same discoverable offers without requiring map gestures or location permission.
- Map zoom/pan is not the sole way to find results; area search, recenter, search, filters and List are operable with assistive technology and keyboard/D-pad where supported.
- Content survives Android font scale 2.0 and web zoom 200% without clipped actions, overlapping navigation or horizontal page scrolling. Use scrolling rather than fixed-height text containers.
- Loading/result count, permission outcome, favorite changes, booking success/cancellation and admin save/lifecycle outcomes are announced in polite live regions. Do not repeatedly announce camera movement.
- Errors identify what happened and the recovery action. Field errors are associated with inputs; focus moves to the error summary only after submit.
- Information is never encoded by color alone. Statuses include text/icon; price includes labels; selected marker has size/glyph/preview changes.
- Test light/dark, portrait/landscape, Compact/Medium/Expanded, TalkBack, keyboard/D-pad, reduced motion, denied/permanently denied location and map-key/network failure.

## 11. Implementation and UX acceptance checklist

### Android

- Explore first render is a Google Map backed by live API results, with a safe List fallback.
- Map/List share query, filters, loaded offers, area and selection; switching does not refetch merely because the view changed.
- Marker selection, clustering, preview, details and booking work with stable state and back navigation.
- `Keresés ezen a területen` appears only after meaningful user camera movement and handles stale/error responses.
- Location is requested only in context; denial never blocks discovery.
- Four destinations use adaptive bar/rail behavior and no compact hamburger drawer.
- Auth, details, provider, booking success, bookings, favorites and profile use the new token system and the state specifications above.
- Light/dark and all loading/empty/error/retry/unavailable states are implemented and tested.

### Administration web

- An administrator can sign in, navigate dashboard/providers/programs, create/edit a provider, create/edit an event and perform allowed lifecycle actions using the backend.
- Forms cover all contracted fields, preserve data after failure, display backend field errors and explicitly convert Budapest-local input to UTC.
- Responsive desktop/tablet/mobile layouts and keyboard/screen-reader behavior meet the criteria above.
- Customer/unauthorized sessions never get a misleading usable admin UI; the backend remains the authority.

### Product review scenarios

1. Deny location, discover Budapest offers in Map and List, filter by age, pan, search the new area, select a marker and reserve.
2. Rotate/resize during marker selection and during a filter sheet; state and intended action survive.
3. Increase font scale/zoom, use TalkBack or keyboard, complete login, favorite, booking and cancellation without gesture-only controls.
4. As admin, create a provider and event using a Budapest-local DST-sensitive date, correct validation, publish it and verify it appears in customer discovery.
5. Simulate map-key failure and API failure independently; List/retry paths remain clear and no mock offers appear.

## 12. Known design risks and mitigations

- **Map chrome can become crowded on small phones.** Limit persistent top chrome to search/filter/chips, make selection dismissible, and test 320 dp width plus large text.
- **Map and List can drift semantically.** One state holder and one loaded result set are mandatory; viewport is part of the shared query.
- **Rapid camera/filter requests can race.** Use camera-idle gating, request IDs/cancellation and render only the latest committed query.
- **Pins can fail contrast on arbitrary basemaps.** Use a high-contrast outline/halo, test both themes and always offer List.
- **Permission prompts can damage trust.** Ask only from locate/distance intent, accept approximate access and preserve Budapest fallback after denial.
- **Admin forms are long and error-prone.** Use visible sections, sticky actions, readiness summary, field-linked errors and persisted draft values; do not hide required data.
- **Timezone conversion around daylight-saving transitions is ambiguous.** Name `Europe/Budapest`, validate nonexistent/duplicate local times, and preview the UTC result before save.
- **Remote images may be missing or hostile to legibility.** Enforce stable aspect ratios and original category placeholders; do not overlay essential text without a contrast scrim.
