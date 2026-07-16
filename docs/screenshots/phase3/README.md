# Phase 3 screenshot evidence

This directory intentionally contains a capture checklist rather than invented screenshots. The connected GitHub Actions environment compiled Android test artifacts but did not run a graphical Google APIs emulator.

Capture the following from the current branch on a configured emulator/device:

- authentication: login and registration;
- Explore Map with markers;
- selected map offer preview;
- Explore List populated;
- filter bottom sheet;
- offer detail;
- reservation review;
- booking success and booking code;
- active and previous bookings;
- favorites populated and empty;
- provider detail;
- profile;
- initial skeleton loading;
- empty discovery;
- network/contract error;
- missing Maps key fallback;
- light and dark themes;
- compact phone and expanded/tablet layouts;
- 200% font scale.

## Capture rules

1. Use only seeded/demo data; do not expose personal information or real tokens.
2. Do not include the Google Maps API key or local properties in screenshots.
3. Record emulator/device model, API level, resolution, font scale and theme.
4. Keep Google Maps attribution visible.
5. Capture both image-loaded and forced image-failure fallback states.
6. Verify that Lottie failure still leaves readable native fallback content.
7. Store optimized PNG files here using descriptive names, for example `explore-map-light-compact.png`.
8. Do not commit large screen recordings; link them from the pull request when needed.
