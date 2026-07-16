# Phase 4 visual and originality review

## Review basis

Phase 4 was compared against the tested Phase 3 head commit:

```text
4fb5cd52fbf199ae3d3ac899f9a192099301ea41
```

The review is source- and build-based. A hosted GitHub runner does not provide a graphical Google APIs emulator, so device screenshots, hands-on TalkBack review and numeric benchmark results remain explicit local acceptance work rather than fabricated evidence.

## Phase 3 → Phase 4 changes

### Brand confidence

Phase 3:

- electric-violet identity;
- uniformly rounded presentation;
- modern but visually separate from the administration web.

Phase 4:

- forest/cream/coral/sun/sage language aligned with the existing UgorjBe web brand;
- original asymmetric brand mark;
- editorial serif headline layer with operational sans-serif metadata;
- reduced generic pill/roundness usage;
- stronger distinction between promotional discovery and calm booking information.

### Authentication

Phase 3:

- animated gradient hero and standard form card.

Phase 4:

- static forest brand canvas with original abstract shapes;
- clear product benefits: today, nearby and value;
- segmented login/registration control;
- stable form geometry and test tags for device journeys;
- compact and expanded layouts preserved.

### Explore Map

Phase 3:

- icon/category markers and a selected preview;
- floating search and filters;
- functional Map/List switch.

Phase 4:

- original forest price markers and coral selected marker;
- explicit cluster count treatment;
- compact search surface with active-filter count;
- selected event exposes image, time, price, remaining places and one action;
- result count and search-this-area remain separate from the query field;
- expanded side panel provides the same decision hierarchy;
- markers remain while refresh/stale feedback appears.

### Explore List

Phase 3:

- image-led cards and skeletons.

Phase 4:

- forest editorial header;
- stronger title/provider/time/price/capacity hierarchy;
- 188 dp media area;
- reduced chip density;
- visible directional action;
- cohesive fallback artwork when every image fails.

### Navigation

Phase 3:

- floating four-destination dock and adaptive rail.

Phase 4:

- custom tactile dock with selected icon container, label and background;
- forest expanded rail with UgorjBe mark;
- all four destinations remain visible;
- Map/List remains contextual inside Explore rather than becoming a fifth destination.

### Detail, booking and secondary routes

The tested Phase 3 information architecture is intentionally preserved to avoid functional regression. Phase 4 theme, typography, media fallback, navigation and semantic surfaces apply to:

- offer detail;
- provider detail;
- reservation review;
- reservation success and booking code;
- active/previous bookings;
- cancellation;
- favorite offers/providers;
- profile/settings;
- loading, empty and error states.

No route is left with the Phase 3 violet palette or prior typography generation.

## Accessibility review

Source-level checks:

- four top-level destinations retain labels and content descriptions;
- interactive controls target at least 48 dp through Material controls or explicit sizing;
- status/selection/capacity use text and shape in addition to color;
- decorative icons/images use null descriptions;
- search, filters, navigation and presentation switch have stable test/semantic identifiers;
- real image failure retains category artwork and readable content;
- missing Maps key provides a List action;
- reduced-motion Lottie behavior remains intact;
- serif typography is limited to large headings, not dense operational copy.

Device checks still required:

- TalkBack traversal order;
- switch access and hardware keyboard focus;
- 200% font scale on every route;
- light/dark contrast over actual map tiles and remote images;
- system animation scale Off;
- compact, landscape and expanded layouts.

## Performance review

Preserved guardrails:

- stable lazy-list keys;
- Coil `AsyncImage` without subcomposition in list cards;
- no Lottie in scrolling cards;
- no decorative animation driven by map camera movement;
- static marker content while camera moves;
- stale results retained instead of replacing all content;
- release R8/resource shrinking and Baseline Profile support unchanged.

Phase 4 benchmark journeys added:

- startup;
- Explore list scroll;
- Map/List switch;
- offer-detail opening.

Compilation validates benchmark code; numeric results require a configured device/emulator and real local API.

## Originality review

### Confirmed original UgorjBe decisions

- forest/cream/coral palette derives from the existing administration web, not a mobility reference palette;
- price marker geometry and colors are UgorjBe-authored Compose surfaces;
- asymmetric `U` brand mark is repository-authored;
- category fallback gradients are repository-authored;
- navigation dock composition is custom and keeps the pre-existing four-destination product architecture;
- authentication shapes and copy are original;
- existing Lottie files are original UgorjBe assets.

### Explicitly rejected reference elements

- GreenGo green brand/zone/vehicle marker language;
- wigo vehicle badges, pricing calculator and onboarding composition;
- Munch branding, discount treatment, food-rescue language, cards and illustrations;
- Free2move/SHARE NOW vehicle map, unlock flow and navigation composition;
- any proprietary screenshot, logo, font, illustration, icon, text or motion signature.

### `cleengo`

The name remains unresolved and did not influence implementation.

## Findings

Blocking/high findings discovered and resolved during source review:

1. Unsupported Compose `animateColorAsState` import path — corrected.
2. Unsupported spring stiffness constant — corrected to a supported API.
3. Root semantics did not expose all test tags to UI Automator — added at `MainActivity` level.
4. Device performance prompt required more than startup — critical journey benchmarks added.

No unresolved source-level blocking or high-severity originality issue is known. Device-dependent visual/accessibility/performance acceptance remains required before merge.

## Screenshot status

No screenshot has been fabricated or copied from a reference application. The exact Phase 4 capture matrix is in `docs/screenshots/phase4/README.md`.
