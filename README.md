# UgorjBe

UgorjBe is a Hungarian, pay-on-arrival marketplace for discounted same-day places in family activities. It contains a .NET/PostgreSQL API, a responsive TypeScript/React administration web application and a native Kotlin/Jetpack Compose Android customer app.

Phase 3 replaces the Android mockup-style presentation with an original production mobile foundation: tokenized light/dark themes, image-led discovery, category fallbacks, purposeful Lottie states, animated navigation, adaptive layouts, contract tests, R8/resource shrinking and Baseline Profile tooling.

The API remains authoritative for expiry, capacity, price, cancellation and overbooking protection. No Munch, TikTok, Instagram, Airbnb, Google Maps or other product branding, assets, copy or distinctive screen design is reused.

## What works

Customers can register or sign in, discover currently bookable activities on a synchronized map or list, search and filter, inspect providers and offers, reserve one or more places, display/copy the booking code and QR payload, cancel within the permitted window, and manage offer/provider favorites.

Administrators can sign in to the web app, view the dashboard, manage providers, and create/edit/publish/unpublish/archive offers. Customer tokens are rejected by administrator endpoints.

## Development credentials

| Role | Email | Password |
| --- | --- | --- |
| Customer | `demo@ugorjbe.local` | `UgorjBe123!` |
| Administrator | `admin@ugorjbe.local` | `UgorjBeAdmin123!` |

These credentials are development fixtures only.

## Repository map

```text
backend/                   .NET 8 modular monolith, EF Core migrations, tests
android/app/               Native Compose customer application
android/baselineprofile/   Macrobenchmark and Baseline Profile test module
web-admin/                 React, TypeScript, Vite administration app
docs/                      Product, API, design, motion, dependency and test records
docker-compose.yml         PostgreSQL, API and administration web stack
.github/workflows/ci.yml   backend, Android, web and integrated Compose gates
```

Important Phase 3 documents:

- `CODEX_PHASE_3.md`
- `docs/MOBILE_DESIGN_SYSTEM.md`
- `docs/MOTION_SPEC.md`
- `docs/ANDROID_DEPENDENCIES.md`
- `docs/THIRD_PARTY_ASSETS.md`
- `docs/PHASE3_TEST_REPORT.md`

## Start the local stack

Prerequisite: Docker Desktop with Compose v2.

```powershell
Copy-Item .env.example .env
docker compose up --build --detach
docker compose ps
```

macOS/Linux:

```bash
cp .env.example .env
docker compose up --build --detach
docker compose ps
```

The API migrates and seeds PostgreSQL at startup.

| Service | Local URL |
| --- | --- |
| Administration web | `http://localhost:8080` |
| API | `http://localhost:8081` |
| Health | `http://localhost:8081/health` |
| Swagger | `http://localhost:8081/swagger` |
| OpenAPI JSON | `http://localhost:8081/swagger/v1/swagger.json` |
| PostgreSQL | `localhost:5432` |

When host port 8080 is occupied, set `ADMIN_WEB_PORT=8082` or another free port in the ignored root `.env`. The web container still proxies `/api` internally to `api:8080`; do not change that internal port.

Stop without deleting data:

```powershell
docker compose down
```

Use `docker compose down --volumes` only for an intentional database reset.

## Google Maps key

The committed `android/secrets.defaults.properties` contains only a build placeholder. A real key must never be committed.

1. Enable **Maps SDK for Android** in a Google Cloud project.
2. Create a key restricted to **Maps SDK for Android**.
3. Add an Android application restriction for package `hu.ugorjbe.app` and the allowed certificate SHA-1.
4. Obtain the debug SHA-1:

```powershell
Set-Location android
.\gradlew.bat signingReport
```

5. Add the key to ignored `android/local.properties` beside `sdk.dir`:

```properties
MAPS_API_KEY=your_restricted_local_key
```

Without a valid key the APK still builds. The map-unavailable state links the customer to the fully functional backend-backed list view.

## Run Android

Prerequisites:

- Android Studio and JDK 17;
- Android SDK platform 36;
- a Google APIs/Google Play emulator or device;
- the Docker stack running.

The debug build calls the host API through the emulator alias:

```text
http://10.0.2.2:8081/
```

The release build deliberately uses an HTTPS placeholder endpoint:

```text
https://api.ugorjbe.invalid/
```

A real production endpoint must be supplied through a reviewed release configuration before distribution. Release does not permit cleartext traffic, and demo credentials/local backend information remain debug-only.

### Android verification

```powershell
Set-Location android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --no-daemon
.\gradlew.bat assembleRelease --no-daemon
```

Artifacts:

```text
android/app/build/outputs/apk/debug/
android/app/build/outputs/apk/release/
```

Run connected UI tests on a configured emulator/device:

```powershell
.\gradlew.bat connectedDebugAndroidTest --no-daemon
```

The Android tests include representative map-envelope JSON deserialization and ensure malformed API contracts are not incorrectly shown as connection failures.

## Images and animation

- Coil loads offer/provider images with memory/disk behavior and correct Compose sizing.
- Every image location has a deterministic category-specific gradient/icon fallback.
- No image is required for discovery, detail or booking to work.
- Three original local Lottie files cover booking success, an empty state and a lightweight loading moment.
- Lottie parsing/rendering failure falls back to native Compose content.
- Success animation is finite; looping animations are limited to visible loading/empty surfaces.
- System-disabled animation produces a static/final state.

Asset provenance is recorded in `docs/THIRD_PARTY_ASSETS.md`.

## Baseline Profile and performance

The `android/baselineprofile` module owns release-like startup and critical-journey measurement.

Generate a profile on a configured emulator/device:

```powershell
Set-Location android
.\gradlew.bat :app:generateBaselineProfile --no-daemon
```

Run the benchmark module tasks available for the selected managed/connected device:

```powershell
.\gradlew.bat :baselineprofile:connectedCheck --no-daemon
```

Measurements must record device, API level, build variant, compilation mode and exact command. Do not claim a before/after improvement without both measurements. See `docs/PHASE3_TEST_REPORT.md`.

## Backend gates

```powershell
dotnet restore backend/UgorjBe.sln --locked-mode
dotnet build backend/UgorjBe.sln -c Release --no-restore
dotnet format backend/UgorjBe.sln --verify-no-changes --no-restore
dotnet test backend/tests/UgorjBe.UnitTests/UgorjBe.UnitTests.csproj -c Release --no-build
$env:UGORJBE_TEST_CONNECTION = 'Host=localhost;Port=5432;Database=ugorjbe_test;Username=ugorjbe;Password=local-password;Include Error Detail=true'
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj -c Release --no-build
```

The test database is reset by the integration fixture. Never point it at valuable data.

## Administration web gates

```powershell
Set-Location web-admin
npm ci
npm run lint
npm run typecheck
npm test
npm run build
```

Integrated browser smoke test with the Docker stack running:

```powershell
npx playwright install chromium
$env:E2E_BASE_URL = 'http://localhost:8080'
$env:E2E_NO_WEBSERVER = '1'
npm run test:e2e
```

## Manual end-to-end checklist

1. Start Compose and verify API health and administrator login.
2. Create/select a provider and publish a future Budapest offer with valid coordinates.
3. Confirm the offer appears in `GET /api/offers/map` inside matching bounds.
4. Run Android with a restricted Maps key and sign in as the demo customer.
5. Verify the offer appears in both Map and List without a second query caused only by switching views.
6. Open detail, change quantity, review total and reserve once.
7. Verify finite success animation, copyable booking code and readable QR payload.
8. Confirm the booking appears, then cancel before the deadline.
9. Add/remove offer and provider favorites and verify server-error recovery.
10. Verify map-key missing, image failure, location denial and backend-offline fallbacks.
11. Review light/dark, compact/expanded, landscape, 200% font scale, TalkBack and animation-disabled behavior.
12. Call an administrator endpoint with a customer token and confirm HTTP 403.

## Security and release notes

- Never commit `.env`, `android/local.properties`, Maps keys, access tokens, signing files, database passwords or production JWT keys.
- Swagger and seed data are development features.
- PostgreSQL row locks/transactions serialize capacity changes.
- Money is decimal plus an ISO currency code; timestamps are ISO-8601 UTC.
- Booking QR payload is a check-in reference, not an authentication credential.
- Release uses R8/minification and resource shrinking; add precise keep rules rather than disabling optimization.
- The repository does not contain a production signing configuration or endpoint.

## Current limitations

- Payment, refunds, settlement, notifications, password reset, email verification, reviews and waitlists are out of scope.
- There is no provider self-service app or media-upload pipeline.
- Admin sessions are memory-only and do not survive refresh.
- Connected screenshots, TalkBack review and benchmark numbers require a configured local device/emulator; GitHub's standard Linux CI validates compilation but cannot substitute for those physical/interactive checks.
- Legal, tax, privacy, activity-safety, trademark and Google Maps commercial-term review remain required before launch.
