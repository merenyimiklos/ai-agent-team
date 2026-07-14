# UgorjBe MVP

UgorjBe is a Hungarian demo marketplace for discounted, same-day empty places in family activities. It combines an ASP.NET Core/PostgreSQL API with a native Kotlin and Jetpack Compose Android app. The MVP is pay-on-arrival and intentionally has no provider app, real payment integration, or production deployment.

## Demo journey

1. Sign in with `demo@ugorjbe.local` / `UgorjBe123!` or register a customer.
2. Browse and filter bookable Budapest offers.
3. Open an offer and its provider details.
4. Reserve one or more places.
5. Show the returned booking code or QR payload.
6. View active/previous bookings, cancel before the cutoff, and manage favorite offers/providers.

All seeded providers, offers, and credentials are development fixtures. Do not reuse them in production.

## Repository map

```text
backend/                   .NET 8 modular-monolith solution and tests
  src/UgorjBe.Api/         HTTP API, middleware, health, Swagger, Dockerfile
  src/UgorjBe.Application/ use cases, DTOs, validation, ports
  src/UgorjBe.Domain/      domain entities, rules, and enums
  src/UgorjBe.Infrastructure/ EF Core, PostgreSQL, JWT, migrations, seed
  tests/                   unit and PostgreSQL integration tests
android/                   Kotlin/Compose single-activity Android application
docs/                      product decision, architecture, and API contract
docker-compose.yml         local API and PostgreSQL stack
.github/workflows/ci.yml   PostgreSQL-backed backend and Android CI gates
.env.example               local configuration template without real secrets
```

The authoritative design and wire behavior are documented in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/API_CONTRACT.md](docs/API_CONTRACT.md).

## Prerequisites

- Docker Desktop with Docker Compose for the shortest full-stack path.
- .NET 8 SDK for direct backend development.
- Android Studio with JDK 17+ and an Android SDK matching the app's `compileSdk`.

No production secrets are stored in the repository. Local Docker values are development-only. Copy `.env.example` to `.env` before Compose startup and replace the JWT key if the example file instructs you to do so.

## Start PostgreSQL and the API

From the repository root:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

On macOS/Linux:

```bash
cp .env.example .env
docker compose up --build
```

The API applies the committed migration and idempotent development seed when configured by Compose. The seed keeps booking-linked history intact and appends a deterministic rolling batch when the previous demo schedule has expired, so a retained database volume remains useful. Wait until the health endpoint reports healthy:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/health`
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI JSON: `http://localhost:8080/swagger/v1/swagger.json`

Stop the stack with `docker compose down`. Add `-v` only when you intentionally want to delete the local database and reseed it.

## Run the backend directly

Provide a reachable PostgreSQL connection string and a development-only JWT signing key of at least 32 bytes:

```powershell
$env:ConnectionStrings__Default = 'Host=localhost;Port=5432;Database=ugorjbe;Username=ugorjbe;Password=local-password'
$env:Jwt__SigningKey = 'replace-with-at-least-32-development-bytes'
$env:SeedData__Enabled = 'true'
dotnet run --project backend/src/UgorjBe.Api/UgorjBe.Api.csproj --urls http://localhost:8080
```

The direct process uses the same URLs and seeded login as Compose.

## Build and test the backend

```powershell
dotnet restore backend/UgorjBe.sln
dotnet build backend/UgorjBe.sln --no-restore
dotnet test backend/tests/UgorjBe.UnitTests/UgorjBe.UnitTests.csproj --no-build
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj --no-build
```

PostgreSQL integration tests require an explicitly supplied disposable test database. The test fixture migrates and resets that database, so never point it at data you need to keep:

```powershell
$env:UGORJBE_TEST_CONNECTION = 'Host=localhost;Port=5432;Database=ugorjbe_test;Username=ugorjbe;Password=local-password'
dotnet test backend/tests/UgorjBe.IntegrationTests/UgorjBe.IntegrationTests.csproj --no-build
```

Without `UGORJBE_TEST_CONNECTION`, only the database-independent API/OpenAPI contract tests run and the PostgreSQL cases report as skipped. The PostgreSQL suite covers authentication edge cases, expiry, quantity validation, competing reservations, exact capacity restoration, cancellation deadlines, customer ownership, active/previous booking scopes, offer/provider favorites, representative catalog filters/sorting, and rolling seed behavior. The same suite runs against a PostgreSQL 16 service in GitHub Actions.

## Build and run Android

1. Start the API on host port `8080`.
2. Open `android/` in Android Studio, let Gradle sync, and select the `app` debug configuration.
3. Run on a standard Android Emulator.

The debug app calls `http://10.0.2.2:8080/`, the emulator alias for the host loopback interface. Its local debug network configuration permits cleartext only for this development endpoint. Discovery initially requests the next 24 hours, so the rolling evening seed remains visible, with narrower 3- and 6-hour filters available.

Command-line checks from the repository root:

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

On macOS/Linux use `./gradlew`. The debug APK is produced under `android/app/build/outputs/apk/debug/`.

## API behavior worth knowing

- Public endpoints expose offers and providers; bookings, favorites, and `/api/auth/me` require a bearer token.
- Times are stored and transmitted in UTC. Default same-day discovery uses `Europe/Budapest` to determine the day boundary.
- Money is decimal plus an explicit ISO currency code; seeded prices use `HUF`.
- The API is authoritative for expiry, cancellation, and capacity.
- Booking creation and cancellation lock the PostgreSQL offer row inside a transaction, preventing two customers from taking the same final place.
- API failures use RFC 7807 problem JSON with stable application error codes so Android can render useful recovery states.

## Configuration and security

- Never commit `.env`, Android `local.properties`, signing files, access tokens, real database passwords, or production JWT keys.
- Swagger and seed data are development features.
- The MVP stores no child names or birth dates.
- The booking QR payload is a display/check-in reference, not an authentication credential.
- Providers remain responsible for activity safety, qualifications, fiscal obligations, and any NTAK reporting.

## Current product limitations

- Provider inventory is seeded; there is no provider or admin application.
- Payment and commission settlement are represented as pay-on-arrival/manual reconciliation.
- No refresh token, email verification, password reset, notifications, reviews, waitlist, or offline catalog cache.
- The product thesis still requires provider occupancy interviews, a compact-area liquidity pilot, customer conversion testing, and Hungarian legal review before production use.
- `UgorjBe` is a working name pending trademark and domain clearance.
