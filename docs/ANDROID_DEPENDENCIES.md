# Android dependency decisions

Phase 3 keeps the existing Kotlin/Jetpack Compose architecture and adds only libraries that solve a concrete production requirement.

| Dependency | Version | Purpose | Decision |
| --- | ---: | --- | --- |
| Compose BOM | 2026.02.01 | Compose version alignment | Existing baseline retained. |
| Maps Compose / Utils | 6.12.0 | Map, marker and clustering UI | Existing supported map stack retained. |
| Coil Compose | 3.5.0 | Constraint-aware remote image loading | Added. `AsyncImage` is used in cards/details; subcomposition image APIs are avoided in lazy lists. |
| Coil network OkHttp | 3.5.0 | HTTPS image fetching through OkHttp | Added. Uses Coil memory/disk behavior and a deterministic category fallback. |
| Airbnb Lottie Compose | 6.7.1 | Small bundled vector state animations | Added for booking success, empty and loading states with native fallback. |
| Baseline Profile Gradle plugin | 1.4.1 | Generate/package startup and critical-journey profiles | Added to app and dedicated test module. |
| Benchmark Macro JUnit4 | 1.4.1 | Release-like startup/frame measurement | Added only to the `baselineprofile` test module. |
| ProfileInstaller | 1.4.1 | Install bundled profile for local/sideloaded builds | Added to the app. |
| AndroidX Test Ext JUnit | 1.2.1 | Instrumented benchmark runner support | Test-module only. |
| UI Automator | 2.3.0 | Drive benchmark journeys | Test-module only. |

## Libraries deliberately not added

- no second UI or navigation framework;
- no Flutter or WebView runtime;
- no shimmer library: the skeleton pulse uses Compose animation primitives;
- no generic animation collection: only three original local Lottie files are bundled;
- no image carousel or social-feed framework;
- no proprietary font package;
- no QR library because the API contract currently guarantees a readable booking code and QR payload, not a signed/scannable QR image contract.

## Release constraints

- release builds use an HTTPS placeholder endpoint and do not inherit emulator cleartext configuration;
- R8/minification and resource shrinking are enabled;
- API keys and signing material remain outside source control;
- debug HTTP logging is disabled automatically outside debug builds;
- third-party versions must be reviewed before a future release upgrade rather than floated dynamically.

## Primary references used for the decision

- Coil Compose documentation recommends `AsyncImage` for most cases and warns that subcomposition can be unsuitable for performance-critical lazy lists.
- Airbnb's official Lottie Android repository publishes the Compose integration and release history.
- Android's official Baseline Profile documentation defines the test module, plugin, `baselineProfile(project(...))`, ProfileInstaller and generation workflow.
