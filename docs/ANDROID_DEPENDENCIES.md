# Android dependency decisions — Phase 4

Phase 4 remains Kotlin/Jetpack Compose and deliberately adds **no new runtime UI dependency**. The reference-led polish is implemented with the already verified native stack and custom UgorjBe components.

| Dependency/tool | Version | Purpose | Phase 4 decision |
| --- | ---: | --- | --- |
| Android Gradle Plugin | 9.2.1 | build, lint, packaging, R8 and resource shrinking | retained |
| Kotlin plugins/BOM | 2.3.21 | compiler, Compose compiler and metadata alignment | retained |
| Dagger/Hilt | 2.60.1 | dependency injection | retained |
| Compose BOM | 2026.02.01 | Compose version alignment | retained; custom components use official Compose APIs |
| Maps Compose / Utils | 6.12.0 | map, marker content and clustering | retained; Phase 4 adds original price-marker content without a new map library |
| Coil Compose / network OkHttp | 3.4.0 | cached remote image loading | retained; category fallback redesigned |
| Airbnb Lottie Compose | 6.7.1 | bounded state animation | retained; no new animation assets |
| Baseline Profile plugin | 1.4.1 | profile generation/package integration | retained |
| Benchmark Macro JUnit4 | 1.4.1 | startup and frame measurement | retained; Phase 4 adds critical-journey tests |
| ProfileInstaller | 1.4.1 | profile installation | retained |
| AndroidX Test Ext JUnit | 1.2.1 | instrumented benchmark runner | retained |
| UI Automator | 2.3.0 | real-device benchmark journey automation | retained |

## Phase 4 implementation choices

- custom search/filter chrome uses Compose foundation and Material 3;
- custom navigation uses Compose interaction sources, animation primitives and semantic tags;
- custom map price markers use Maps Compose clustering content slots;
- editorial typography uses Android system serif/sans fallbacks, not a bundled font;
- press feedback uses Compose springs, not an interaction library;
- filter UI uses Material modal bottom sheet;
- no screenshot, design-system, shimmer or marker dependency was added.

## Verified compatibility baseline

The pinned compatibility set remains:

- Kotlin 2.3.21;
- Hilt 2.60.1;
- Coil 3.4.0;
- AGP 9.2.1;
- Java/JVM 17.

Versions remain pinned rather than dynamically floated.

## Libraries deliberately not added

- no Flutter or WebView runtime;
- no second navigation/UI framework;
- no proprietary mobility design system;
- no generic animation collection;
- no shimmer library;
- no social-feed, carousel or autoplay framework;
- no proprietary font;
- no copied marker/icon package;
- no QR renderer until the backend defines a signed/scannable QR-image contract.

## Release constraints

- release uses an HTTPS placeholder endpoint and never inherits emulator cleartext configuration;
- R8/minification and resource shrinking remain enabled;
- Maps keys, API secrets and signing material remain local;
- debug HTTP logging remains disabled outside debug builds;
- benchmark/profile code stays isolated in the Android test module;
- actual benchmark numbers and Baseline Profile generation require a configured emulator/device and must not be inferred from CI compilation.

## Reference basis

- official Android/Compose APIs for touch feedback, semantics, adaptive layout and animation;
- official Maps Compose clustering/content APIs;
- Coil `AsyncImage` for constraint-aware list/detail media;
- official Lottie Compose integration for the existing bounded animations;
- official Android Baseline Profile/Macrobenchmark workflow.
