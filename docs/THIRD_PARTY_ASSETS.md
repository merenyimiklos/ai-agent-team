# Third-party and visual asset record

## Phase 3 bundled assets

No proprietary screenshot, logo, illustration, font or animation from another product is included.

| File | Type | Origin | License/status | Runtime fallback |
| --- | --- | --- | --- | --- |
| `android/app/src/main/res/raw/booking_success.json` | Lottie vector animation | Original UgorjBe Phase 3 asset authored directly for this repository | Project-owned source; no external asset | Primary-color circular surface and QR icon |
| `android/app/src/main/res/raw/empty_discovery.json` | Lottie vector animation | Original UgorjBe Phase 3 asset authored directly for this repository | Project-owned source; no external asset | Search/bookmark icon and explanatory text |
| `android/app/src/main/res/raw/brand_loading.json` | Lottie vector animation | Original UgorjBe Phase 3 asset authored directly for this repository | Project-owned source; no external asset | Auto-awesome icon and loading copy |

The JSON files contain vector shapes only and no embedded raster images.

## Icons

The application uses AndroidX Material icons through the existing `material-icons-extended` dependency. They are used as functional interface symbols, not as a copied product icon set.

## Remote images

Offer/provider image URLs are supplied by the UgorjBe backend and administered through the web application. The Android repository does not redistribute those images.

Runtime rules:

- image failure never blocks discovery or booking;
- missing/failed images use original category gradients and Material functional icons;
- a production operator remains responsible for rights, moderation, retention and image-host policy for uploaded URLs.

## Fonts

No font file is bundled. The application uses the Android sans-serif fallback through Compose typography.

## Maps

Google Maps tiles, attribution and SDK assets are provided at runtime under the developer's Google Maps Platform configuration. No map tiles or Google visual assets are committed.

## Review checklist for future assets

Before adding any asset, record:

1. source URL or author;
2. exact license and redistribution permission;
3. whether modification is allowed;
4. whether attribution is required;
5. file size and embedded raster content;
6. accessibility purpose;
7. static/native fallback;
8. confirmation that the asset does not imitate another product's distinctive identity.
