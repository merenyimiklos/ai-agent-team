# Phase 4 screenshot and device-review matrix

This directory intentionally contains a capture plan instead of invented or reference-product screenshots. Capture evidence from the current branch on a configured Google APIs emulator or physical Android device.

## Required compact screenshots

Light and dark where applicable:

1. authentication — login;
2. authentication — registration;
3. Explore Map with multiple price markers and cluster;
4. Explore Map with selected event preview;
5. Explore List populated;
6. filter bottom sheet;
7. offer detail;
8. reservation review;
9. reservation success;
10. booking code/QR payload;
11. active bookings;
12. previous bookings;
13. favorites populated;
14. favorites empty;
15. provider detail;
16. profile/settings;
17. skeleton loading;
18. empty discovery;
19. stale/network retry state;
20. missing Maps key/list fallback;
21. failed remote image/category fallback;
22. location permission denied.

## Required expanded screenshots

- expanded authentication split layout;
- map with forest navigation rail and selected-event side panel;
- list with navigation rail;
- offer detail at expanded width;
- profile at expanded width.

## Accessibility variants

- 200% font scale: authentication, Explore List, offer detail, reservation review, booking code and profile;
- dark theme over actual map tiles and loaded images;
- animations disabled: login/register change, Map/List change, booking success and empty state;
- TalkBack focus evidence for navigation, search/filter, selected preview and booking CTA.

## Capture rules

1. Use seeded/demo data only; do not expose personal information or real tokens.
2. Never show the Maps API key, `local.properties`, JWT or database credentials.
3. Keep Google Maps attribution visible.
4. Record device/emulator model, API level, resolution, theme, orientation and font scale.
5. Capture both successful remote media and forced image-failure fallback.
6. Verify Lottie failure/static fallback separately; the screen must remain understandable.
7. Name files descriptively, for example:
   - `explore-map-selected-light-compact.png`
   - `offer-detail-dark-200pct.png`
   - `authentication-light-expanded.png`
8. Optimize PNG files before committing.
9. Do not commit proprietary reference screenshots or large screen recordings.
10. Add before/after Phase 3 evidence only from builds owned by this repository.

## Local branch

```powershell
git fetch origin
git switch codex/ugorjbe-phase4-reference-polish
git pull origin codex/ugorjbe-phase4-reference-polish
```

Open the repository's `android` directory in Android Studio, select the `app` run configuration and use the existing restricted local Maps key.
