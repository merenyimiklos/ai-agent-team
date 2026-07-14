# UgorjBe

UgorjBe is a Hungarian, pay-on-arrival marketplace for discounted same-day places in family activities. The Phase 2 MVP contains a .NET/PostgreSQL API, a native map-first Android customer app, and a TypeScript/React administration web app.

The product is original and does not reuse Munch branding, copy, assets, source, or distinctive screen designs. All seeded data and credentials are development fixtures only.

## What works

Customers can register or sign in, discover currently bookable activities on a synchronized map or list, search and filter, inspect providers and offers, reserve one or more places, display the booking code/QR payload, cancel within the permitted window, and manage offer/provider favorites. The API remains authoritative for expiry, capacity, cancellation, and overbooking prevention.

Administrators can sign in to the responsive web app, view the dashboard, search/create/edit providers, and search/create/edit/publish/unpublish/archive offers. Admin writes are validated and concurrency-protected by the API; customer tokens are forbidden from every admin endpoint.

## Development credentials

| Role | Email | Password |
| --- | --- | --- |
| Customer | `demo@ugorjbe.local` | `UgorjBe123!` |
| Administrator | `admin@ugorjbe.local` | `UgorjBeAdmin123!` |

These credentials are seeded only when development seeding is enabled. Never reuse them outside local development or test environments.

## Repository map

```text
backend/                   .NET 8 modular monolith, EF Core migrations, tests
android/                   Kotlin, Jetpack Compose, Google Maps Compose app
web-admin/                 React, TypeScript, Vite administration app
docs/                      product, UX, architecture, and API decisions
docker-compose.yml         PostgreSQL, API, and administration web stack
.github/workflows/ci.yml   backend, Android, web, and Compose gates
```

See `docs/UX_DIRECTION.md`, `docs/ARCHITECTURE.md`, and `docs/API_CONTRACT.md` for the frozen Phase 2 behavior.

## Fastest local setup: Docker Compose

Prerequisites: Docker Desktop with Compose v2.

Create the ignored local environment file and replace the example database password and JWT key with local development values. The JWT signing key must contain at least 32 UTF-8 bytes.

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

The API migrates and seeds the database at startup. Wait for `http://localhost:8081/health` to report `Healthy`.

| Service | Local URL |
| --- | --- |
| Administration web | `http://localhost:8080` |
| API | `http://localhost:8081` |
| API health | `http://localhost:8081/health` |
| Swagger UI (Development) | `http://localhost:8081/swagger` |
| OpenAPI JSON | `http://localhost:8081/swagger/v1/swagger.json` |
| PostgreSQL | `localhost:5432` |

The administration container proxies `/api` and `/health` to the API, so the browser uses same-origin requests. If 8080 is occupied, set `ADMIN_WEB_PORT` in `.env` to another free host port such as 8082. View logs with `docker compose logs --follow`. Stop without deleting data using `docker compose down`; add `--volumes` only when intentionally resetting the local database.

## Google Maps key for Android

The committed `android/secrets.defaults.properties` contains only a harmless build placeholder so CI can compile. A real key must never be committed.

1. In a Google Cloud project that you control, enable **Maps SDK for Android**. Billing may be required by Google; this repository does not purchase or provision it.
2. Create an API key and restrict its API access to **Maps SDK for Android**.
3. Add an Android application restriction for package `hu.ugorjbe.app` and the SHA-1 certificate fingerprint of each allowed build. Obtain the debug fingerprint with `cd android` then `./gradlew signingReport` (`.\gradlew.bat signingReport` on Windows).
4. Add this ignored line to `android/local.properties` alongside `sdk.dir`:

```properties
MAPS_API_KEY=your_restricted_local_key
```

5. Sync Gradle and run the debug app. Key restrictions can take several minutes to propagate.

Without a valid local key the APK still builds, but Google map tiles do not render. The list view and the rest of the backend-backed customer flow remain usable. Never put the key in source, Gradle scripts, `.env.example`, CI logs, or a pull request.

## Run Android

Prerequisites: Android Studio/JDK 17 and Android SDK platform 36. Start the Compose stack first, then open `android/` and run the `app` debug configuration on a Google APIs emulator. The debug client calls `http://10.0.2.2:8081/`, the emulator alias for the host loopback interface.

The app requests approximate location only after the user invokes the location action. Denial leaves Budapest discovery available. Explore opens map-first; Map and List share filters and selection, while the server supplies bounded live results.

Command-line checks:

```powershell
Set-Location android
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest --no-daemon
```

macOS/Linux: use `./gradlew`. The debug APK is written below `android/app/build/outputs/apk/debug/`.

## Run the API directly

Prerequisites: .NET 8 SDK and a PostgreSQL 16-compatible database.

```powershell
$env:ConnectionStrings__Default = 'Host=localhost;Port=5432;Database=ugorjbe;Username=ugorjbe;Password=local-password'
$env:Jwt__SigningKey = 'replace-with-at-least-32-development-bytes'
$env:DatabaseInitialization__Enabled = 'true'
$env:SeedData__Enabled = 'true'
dotnet run --project backend/src/UgorjBe.Api/UgorjBe.Api.csproj --urls http://localhost:8081
```

Development CORS allows only `http://localhost:5173`. Times are stored/transmitted in UTC; administrator date-time input is interpreted strictly as Europe/Budapest local time.

Backend gates:

```powershell
dotnet restore backend/UgorjBe.sln --locked-mode
dotnet build backend/UgorjBe.sln -c Release --no-restore
dotnet format backend/UgorjBe.sln --verify-no-changes --no-restore
dotnet test backend/tests/UgorjBe.UnitTests/UgorjBe.UnitTests.csproj -c Release --no-build
$env:UGORJBE_TEST_CONNECTION = 'Host=localhost;Port=5432;Database=ugorjbe_test;Username=ugorjbe;Password=local-password;Include Error Detail=true'
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj -c Release --no-build
```

The PostgreSQL test fixture migrates and resets the supplied database. Never point `UGORJBE_TEST_CONNECTION` at data you need to keep.

## Run the administration web directly

Prerequisites: Node.js 22.14 or newer. Start the API on 8081 first.

```powershell
Set-Location web-admin
Copy-Item .env.example .env.local
npm ci
npm run dev
```

Open `http://localhost:5173` and use the administrator credential above. The token is held in memory only, so a browser refresh intentionally returns to sign-in.

Web gates:

```powershell
Set-Location web-admin
npm ci
npm run lint
npm run typecheck
npm test
npm run build
```

The browser-level smoke test requires the integrated stack and creates uniquely named development records:

```powershell
Set-Location web-admin
npx playwright install chromium
$env:E2E_BASE_URL = 'http://localhost:8080'
$env:E2E_NO_WEBSERVER = '1'
npm run test:e2e
```

## Manual end-to-end verification

1. Start Compose and verify API health and the administration login page.
2. Sign in to the admin web, create or select a provider, create a future offer with Budapest coordinates, and publish it.
3. Confirm `GET /api/offers/map` and `GET /api/offers` expose the published offer inside matching bounds/filters.
4. Start Android with a restricted Maps key, sign in as the demo customer, and confirm the same offer appears in both Explore views.
5. Open it, reserve a place, verify the booking code, favorite it, then cancel before the cutoff.
6. Attempt an admin endpoint with the customer token and confirm HTTP 403.
7. Check compact/expanded and light/dark Android layouts, location denial fallback, map camera movement, and **Search this area**.

## Security and operating notes

- Never commit `.env`, `android/local.properties`, Maps keys, access tokens, signing files, production database passwords, or production JWT keys.
- Swagger and seed data are Development features.
- PostgreSQL row locks and transactions serialize reservation/cancellation capacity changes; administration rules prevent edits that invalidate confirmed bookings.
- Money is decimal with an explicit ISO currency code. API timestamps are ISO-8601 UTC values.
- Booking QR content is a check-in reference, not an authentication credential.

## Known MVP limitations

- Payment, refunds, settlement, notifications, password reset, email verification, reviews, waitlists, and production identity administration are out of scope.
- There is no provider self-service app; providers are managed by development administrators.
- Map use requires a developer-supplied Google Maps key and a Google APIs emulator/device.
- Admin sessions are deliberately memory-only and do not survive a page refresh; refresh tokens are not implemented.
- Image URLs reference remote provider imagery; there is no media upload pipeline.
- This is a local-development MVP, not a production deployment. Hungarian legal, tax, privacy, activity-safety, trademark, and Google Maps commercial-term reviews remain required before any launch.
