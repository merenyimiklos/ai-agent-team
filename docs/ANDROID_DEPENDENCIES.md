# Android dependency decisions

Phase 3 keeps the native Kotlin/Jetpack Compose architecture and adds only dependencies that solve a concrete production requirement.

| Dependency/tool | Version | Purpose | Decision |
| --- | ---: | --- | --- |
| Android Gradle Plugin | 9.2.1 | Android build, lint, packaging, R8 and resource shrinking | Existing modern build baseline retained. |
| Kotlin plugins/BOM | 2.3.21 | Kotlin compiler, Compose compiler and metadata alignment | Selected as the compatible line for AGP 9, Hilt 2.60.1 and Coil 3.4.0. |
| Dagger/Hilt | 2.60.1 | Dependency injection and generated Android components | Upgraded for AGP 9/Kotlin 2.3 compatibility. |
| Compose BOM | 2026.02.01 | Compose version alignment | Existing baseline retained. |
| Maps Compose / Utils | 6.12.0 | Map, marker and clustering UI | Existing map stack retained. |
| Coil Compose | 3.4.0 | Constraint-aware remote image loading | Added. `AsyncImage` is used in cards/details; subcomposition image APIs are avoided in lazy lists. |
| Coil network OkHttp | 3.4.0 | HTTPS image fetching through OkHttp | Added with deterministic category fallback when loading fails. |
| Airbnb Lottie Compose | 6.7.1 | Small bundled vector state animations | Added for booking success, empty and loading states with native fallback. |
| Baseline Profile Gradle plugin | 1.4.1 | Generate/package startup and critical-journey profiles | Added to the app and a dedicated test module. |
| Benchmark Macro JUnit4 | 1.4.1 | Release-like startup/frame measurement | Added only to `baselineprofile`. |
| ProfileInstaller | 1.4.1 | Install bundled profile for local/sideloaded builds | Added to the app. |
| AndroidX Test Ext JUnit | 1.2.1 | Instrumented benchmark runner support | Test-module only. |
| UI Automator | 2.3.0 | Drive profile/benchmark journeys | Test-module only. |

## Compatibility decision

The first implementation attempt exposed two real metadata incompatibilities in CI:

1. Coil 3.5.0 pulled Kotlin 2.4 metadata while the project compiler was older.
2. Kotlin 2.4 then exceeded the metadata version supported by the previous Hilt compiler.

The verified build therefore uses Kotlin 2.3.21, Hilt 2.60.1 and Coil 3.4.0 as one compatible toolchain. Versions are pinned rather than floated.

## Libraries deliberately not added

- no second UI or navigation framework;
- no Flutter or WebView runtime;
- no shimmer dependency: skeleton motion uses Compose primitives;
- no generic animation collection: only three original local Lottie files are bundled;
- no image carousel or social-feed framework;
- no proprietary font package;
- no QR dependency because the current API guarantees a readable booking code and QR payload, not a signed/scannable QR-image contract.

## Release constraints

- release builds use an HTTPS placeholder endpoint and never inherit emulator cleartext configuration;
- R8/minification and resource shrinking are enabled and compile in CI;
- API keys and signing material remain outside source control;
- debug HTTP logging is disabled outside debug builds;
- benchmark/profile code is isolated in an Android test module;
- actual benchmark numbers and profile generation require a configured emulator/device and are not fabricated from compilation-only CI.

## Primary reference basis

- Coil Compose documentation recommends `AsyncImage` for most cases and warns that subcomposition is unsuitable for some performance-critical lazy-list cases.
- Airbnb's official Lottie Android project provides the Compose integration.
- Android's official Baseline Profile documentation defines the test module, plugin, app dependency and generation workflow.
