# Phase 3 test report

Branch: `chatgpt/phase3-production-mobile`

## Verified in GitHub Actions

### Backend — PASS

```bash
dotnet restore backend/UgorjBe.sln --locked-mode
dotnet build backend/UgorjBe.sln -c Release --no-restore
dotnet format backend/UgorjBe.sln --verify-no-changes --no-restore
dotnet test backend/tests/UgorjBe.UnitTests/UgorjBe.UnitTests.csproj -c Release --no-build
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj -c Release --no-build
```

PostgreSQL 16 integration coverage includes authentication, authorization, catalog/map filtering, expiry, capacity, concurrent overbooking protection, cancellation, favorites and rolling seed behavior.

### Administration web — PASS

```bash
npm ci
npm run lint
npm run typecheck
npm test
npm run build
```

### Docker and browser integration — PASS

The integrated job starts PostgreSQL, API and administration web, verifies API/web/proxy health and runs the Chromium Playwright administrator smoke journey.

### Android — PASS

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease :baselineprofile:assemble --no-daemon
```

Verified:

- Android local unit tests;
- representative map JSON deserialization;
- malformed API contract versus network failure mapping;
- lint;
- debug APK;
- Android test APK;
- release APK;
- R8/minification and resource shrinking path;
- benchmark/Baseline Profile test-module compilation.

## Defects fixed

1. Android and backend map-envelope DTOs disagreed.
2. JSON parsing failures were incorrectly shown as network failures.
3. Late-night Budapest seed initialization could create expired offers.
4. Kotlin, Hilt and Coil metadata versions were incompatible.
5. Benchmark module JVM targets and API usage were inconsistent.

## Device-dependent checks still required

Hosted CI did not execute or fabricate:

- connected Android UI tests;
- real Google Maps tile/key rendering;
- screenshots for all routes/themes/window sizes;
- hands-on TalkBack and 200% font-scale review;
- animation-disabled manual review;
- Baseline Profile generation on a device;
- numeric startup/frame/jank measurements.

Run locally on a configured Google APIs emulator or device:

```powershell
Set-Location android
.\gradlew.bat connectedDebugAndroidTest --no-daemon
.\gradlew.bat :app:generateBaselineProfile --no-daemon
.\gradlew.bat :baselineprofile:connectedCheck --no-daemon
```

Record device, API level, build variant and compilation mode before publishing benchmark numbers.

## Manual acceptance

1. Start Docker and verify API health.
2. Add a restricted Maps SDK key to `android/local.properties`.
3. Publish a future Budapest event through the administration web.
4. Confirm it appears in Android Map and List.
5. Verify search/filter state survives Map/List switching.
6. Complete detail → review → reserve → success → booking-code flow.
7. Verify booking cancellation and favorites.
8. Repeat with Maps key missing, images failing, location denied and backend offline.
9. Review light/dark, compact/expanded, rotation, 200% text, TalkBack and disabled animations.

## Assessment

Code, tests, lint, debug/release builds, R8/resource shrinking, benchmark-module compilation, web gates and Docker/Playwright integration are verified. Interactive screenshots, connected tests and numeric performance measurements remain explicit local-device acceptance work.
