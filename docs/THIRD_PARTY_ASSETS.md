# Third-party and visual asset record — Phase 4

## Asset policy

No proprietary screenshot, logo, illustration, font, icon, marker, copy, animation or source code from GreenGo, wigo, Munch, Free2move, SHARE NOW or another product is included.

Phase 4 uses public reference material only to understand general product principles. No reference asset was downloaded, traced, recolored or recreated.

## Bundled Lottie assets

Phase 4 reuses the three original UgorjBe assets created in Phase 3; no new animation file was added.

| File | Type | Origin | License/status | Runtime fallback |
| --- | --- | --- | --- | --- |
| `android/app/src/main/res/raw/booking_success.json` | Lottie vector | Original UgorjBe repository asset | Project-owned; no external asset | Primary action surface and QR/code icon |
| `android/app/src/main/res/raw/empty_discovery.json` | Lottie vector | Original UgorjBe repository asset | Project-owned; no external asset | Search/bookmark icon and explanatory text |
| `android/app/src/main/res/raw/brand_loading.json` | Lottie vector | Original UgorjBe repository asset | Project-owned; no external asset | UgorjBe brand mark and loading copy |

The files contain vector shapes only and no embedded raster images.

## Phase 4 brand graphics

The following are code-generated Compose graphics rather than external assets:

- asymmetric `UgorjBeBrandMark`;
- forest/coral/sun authentication shapes;
- category fallback gradients;
- forest/coral price markers and cluster counters;
- navigation selected-state surfaces.

They were authored for this repository and do not reproduce another product's logo, vehicle marker or visual signature.

## Icons

The application uses AndroidX Material icons through the existing `material-icons-extended` dependency. They are functional interface symbols. No reference product icon pack is included.

## Typography

No font file is bundled.

- display/headline: Android system serif fallback;
- title/body/label/numeric: Android system sans-serif fallback.

This avoids proprietary-font redistribution and keeps large-text behavior predictable.

## Remote images

Offer/provider image URLs are supplied by the backend and maintained through the administration web. The Android repository does not redistribute the images.

Runtime rules:

- image failure never blocks discovery or booking;
- missing/failed images use original category gradients and Material functional icons;
- the production operator remains responsible for rights, moderation, retention and image-host policy.

## Maps

Google Maps tiles, attribution and SDK-provided assets are rendered at runtime under the developer's Google Maps Platform configuration. No map tile, Google logo or Google screenshot is committed.

The custom price/cluster content is UgorjBe-authored Compose UI. Google attribution must remain visible and unobstructed.

## Reference-source records

Reference URLs and observed principles are recorded in `docs/REFERENCE_APP_BENCHMARK.md`. Those links are evidence only; their assets are not part of the repository.

## Future asset checklist

Before adding any future asset, record:

1. source URL or author;
2. exact license and redistribution permission;
3. whether modification is allowed;
4. attribution requirements;
5. file size and embedded raster content;
6. accessibility purpose;
7. static/native fallback;
8. confirmation that it does not imitate another product's distinctive identity.
