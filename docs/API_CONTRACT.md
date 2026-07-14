# UgorjBe Phase 2 API contract

**Status:** Frozen for backend, Android, and administration web implementation<br>
**Direct local API:** `http://localhost:8081`<br>
**Android Emulator debug:** `http://10.0.2.2:8081`<br>
**Compose admin same-origin API:** relative `/api` through `http://localhost:8080`<br>
**Media types:** `application/json`; errors use `application/problem+json`

This is the human-readable source of truth for generated OpenAPI and client models. Existing customer paths remain unversioned and compatible except for additive fields and documented lifecycle enum expansion.

## 1. Wire conventions

- JSON is camelCase; clients ignore unknown response fields.
- IDs are UUID strings.
- Every timestamp is an ISO-8601 UTC instant ending in `Z`. Admin writes with a local time or non-UTC offset fail validation.
- Money is a decimal JSON number plus an uppercase ISO-4217 code. It is never a localized string or binary floating point.
- Enum values use the uppercase spellings in this document; integers are rejected.
- Optional response fields are explicit `null` where shown.
- Collection pages are one-based and use UUID ascending as their final tie-breaker.
- List page size defaults to 20 and is 1–50.
- Protected requests use `Authorization: Bearer <accessToken>`.

### Money

```json
{ "amount": 3200.00, "currency": "HUF" }
```

Amount is 0–9999999999.99 with at most two fractional digits. Two prices on one offer must use the same currency.

### Address

```json
{
  "postalCode": "1137",
  "city": "Budapest",
  "street": "Pozsonyi út 12.",
  "countryCode": "HU",
  "latitude": 47.518200,
  "longitude": 19.050400
}
```

Coordinates are WGS84. Public/provider input rules are specified in section 10.

### Page envelope

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "totalCount": 0,
  "totalPages": 0
}
```

Requesting a page beyond the end returns `200` with an empty `items` array and accurate metadata.

### Map bounds and envelope

```json
{
  "items": [],
  "returnedCount": 0,
  "limit": 100,
  "isTruncated": false,
  "bounds": {
    "south": 47.420000,
    "west": 18.920000,
    "north": 47.590000,
    "east": 19.180000
  }
}
```

The map response deliberately has no global `totalCount` and no page cursor. The server reads at most `limit + 1` eligible rows. `isTruncated=true` means more eligible rows exist in these bounds and clients must narrow the area/filters; it does not authorize an unbounded follow-up.

## 2. Error contract

Every non-2xx API response uses RFC 7807 with stable `code` and `traceId`:

```json
{
  "type": "urn:ugorjbe:problem:validation-failed",
  "title": "A kérés érvénytelen.",
  "status": 400,
  "detail": "Egy vagy több mező hibás.",
  "instance": "/api/admin/offers",
  "code": "VALIDATION_FAILED",
  "traceId": "00-7e...-01",
  "errors": {
    "startsAtUtc": ["Az időpontnak UTC Z formátumúnak kell lennie."]
  }
}
```

Field keys are camelCase paths such as `address.latitude`. Clients branch on `code`, never localized prose.

| HTTP | Code | Meaning |
| ---: | --- | --- |
| 400 | `VALIDATION_FAILED` | Body, query, route, format, or field validation failed |
| 401 | `AUTH_REQUIRED` | Token missing, invalid, or expired |
| 401 | `AUTH_INVALID_CREDENTIALS` | Login email/password mismatch |
| 403 | `AUTH_FORBIDDEN` | Valid identity lacks the required role |
| 404 | `OFFER_NOT_FOUND` | Offer absent or intentionally not visible |
| 404 | `PROVIDER_NOT_FOUND` | Provider absent |
| 404 | `BOOKING_NOT_FOUND` | Booking absent or owned by someone else |
| 409 | `AUTH_EMAIL_EXISTS` | Normalized registration email exists |
| 409 | `OFFER_NOT_BOOKABLE` | Offer state, cutoff, start, or expiry rejects booking; response includes `reason` |
| 409 | `INSUFFICIENT_CAPACITY` | Requested places unavailable; includes `availablePlaces` |
| 409 | `CANCELLATION_NOT_ALLOWED` | Cancellation boundary/status rejects cancellation |
| 409 | `OFFER_STATE_TRANSITION_INVALID` | Admin lifecycle action is not allowed from current state |
| 409 | `OFFER_PUBLISH_NOT_READY` | Publish readiness changed/failed; includes field `errors` |
| 409 | `OFFER_UPDATE_CONFLICT` | Update would violate confirmed-booking/capacity invariants |
| 409 | `CONCURRENCY_CONFLICT` | Opaque admin version is stale |
| 503 | `DEPENDENCY_UNAVAILABLE` | Health/dependency failure |
| 500 | `INTERNAL_ERROR` | Unexpected server failure; no internals exposed |

Unsupported media/method/route errors retain the problem shape. `403` is never returned as `VALIDATION_FAILED`.

## 3. Shared public DTOs

### UserDto and AuthResponse

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "email": "demo@ugorjbe.local",
  "displayName": "Demó Család",
  "locale": "hu-HU",
  "role": "customer",
  "createdAtUtc": "2026-07-15T08:00:00Z"
}
```

`role` is `customer` or `admin`. Registration always returns `customer`.

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresAtUtc": "2026-07-15T12:00:00Z",
  "user": {
    "id": "11111111-1111-1111-1111-111111111111",
    "email": "demo@ugorjbe.local",
    "displayName": "Demó Család",
    "locale": "hu-HU",
    "role": "customer",
    "createdAtUtc": "2026-07-15T08:00:00Z"
  }
}
```

### ProviderSummaryDto and ProviderDetailDto

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "Kerek Erdő Műhely",
  "shortDescription": "Kézműves programok családoknak.",
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "imageUrl": null
}
```

Detail adds `description`, nullable `phone`, `email`, `websiteUrl`, `accessibilityInfo`, and integer `activeOfferCount`. Active count uses the same live/bookable predicate as discovery.

### OfferSummaryDto

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "provider": {
    "id": "22222222-2222-2222-2222-222222222222",
    "name": "Kerek Erdő Műhely",
    "shortDescription": "Kézműves programok családoknak.",
    "address": {
      "postalCode": "1137",
      "city": "Budapest",
      "street": "Pozsonyi út 12.",
      "countryCode": "HU",
      "latitude": 47.518200,
      "longitude": 19.050400
    },
    "imageUrl": null
  },
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "title": "Délutáni agyagozás",
  "category": "WORKSHOP",
  "startsAtUtc": "2026-07-15T14:00:00Z",
  "endsAtUtc": "2026-07-15T15:30:00Z",
  "minChildAge": 5,
  "maxChildAge": 10,
  "originalUnitPrice": { "amount": 4800.00, "currency": "HUF" },
  "discountedUnitPrice": { "amount": 3200.00, "currency": "HUF" },
  "discountPercent": 33,
  "availablePlaces": 4,
  "distanceKm": 2.3,
  "imageUrl": null
}
```

`address` is the event venue; `provider.address` is the provider's registered location. `distanceKm` is nullable and populated only when reference coordinates are supplied.

### OfferDetailDto

Detail contains all summary fields plus:

```json
{
  "description": "Minden eszközt biztosítunk.",
  "bookingCutoffUtc": "2026-07-15T13:30:00Z",
  "cancelUntilUtc": "2026-07-15T12:00:00Z",
  "accompanimentRequired": true,
  "accessibilityInfo": "Babakocsival megközelíthető.",
  "totalCapacity": 10,
  "availablePlaces": 4,
  "isBookable": true,
  "unavailableReason": null,
  "paymentMethod": "PAY_ON_ARRIVAL"
}
```

`unavailableReason` is null or `SOLD_OUT`, `BOOKING_CLOSED`, `STARTED`, `UNPUBLISHED`, or `ARCHIVED`. A never-published draft returns `404`. Previously published offers remain readable by ID when unavailable; booking performs the definitive check.

### BookingDto

```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "status": "CONFIRMED",
  "quantity": 2,
  "unitPrice": { "amount": 3200.00, "currency": "HUF" },
  "totalPrice": { "amount": 6400.00, "currency": "HUF" },
  "paymentMethod": "PAY_ON_ARRIVAL",
  "bookingCode": "UGB-7K3M9Q",
  "qrPayload": "ugorjbe://booking/44444444-4444-4444-4444-444444444444?code=UGB-7K3M9Q",
  "createdAtUtc": "2026-07-15T10:05:00Z",
  "cancelledAtUtc": null,
  "cancellationAllowed": true,
  "cancellationDeadlineUtc": "2026-07-15T12:00:00Z",
  "offer": {
    "id": "33333333-3333-3333-3333-333333333333",
    "title": "Délutáni agyagozás",
    "category": "WORKSHOP",
    "providerId": "22222222-2222-2222-2222-222222222222",
    "providerName": "Kerek Erdő Műhely",
    "startsAtUtc": "2026-07-15T14:00:00Z",
    "endsAtUtc": "2026-07-15T15:30:00Z",
    "address": {
      "postalCode": "1137",
      "city": "Budapest",
      "street": "Pozsonyi út 12.",
      "countryCode": "HU",
      "latitude": 47.518200,
      "longitude": 19.050400
    },
    "imageUrl": null
  }
}
```

Status is `CONFIRMED`, `CANCELLED`, or read-time `COMPLETED`. The QR payload is not an authentication credential.

## 4. Endpoint inventory

| Method | Path | Authorization | Success |
| --- | --- | --- | --- |
| GET | `/health` | Public | 200 |
| POST | `/api/auth/register` | Public | 201 |
| POST | `/api/auth/login` | Public | 200 |
| GET | `/api/auth/me` | Bearer | 200 |
| GET | `/api/offers` | Public | 200 page |
| GET | `/api/offers/map` | Public | 200 map envelope |
| GET | `/api/offers/{offerId}` | Public | 200 |
| GET | `/api/providers/{providerId}` | Public | 200 |
| POST | `/api/bookings` | Bearer | 201 |
| GET | `/api/bookings` | Bearer | 200 page |
| GET | `/api/bookings/{bookingId}` | Bearer/owner | 200 |
| POST | `/api/bookings/{bookingId}/cancel` | Bearer/owner | 200 |
| GET/PUT/DELETE | `/api/favorites/...` | Bearer | 200/204 |
| GET | `/api/admin/dashboard` | Admin | 200 |
| GET/POST | `/api/admin/providers` | Admin | 200/201 |
| GET/PUT | `/api/admin/providers/{providerId}` | Admin | 200 |
| GET/POST | `/api/admin/offers` | Admin | 200/201 |
| GET/PUT | `/api/admin/offers/{offerId}` | Admin | 200 |
| POST | `/api/admin/offers/{offerId}/publish` | Admin | 200 |
| POST | `/api/admin/offers/{offerId}/unpublish` | Admin | 200 |
| POST | `/api/admin/offers/{offerId}/archive` | Admin | 200 |

## 5. Authentication and health

`POST /api/auth/register` accepts:

```json
{
  "email": "szulo@example.hu",
  "password": "Biztonsagos1",
  "displayName": "Kiss Anna"
}
```

- email: trimmed valid address, maximum 254, normalized case-insensitively;
- password: 8–128 with uppercase, lowercase, and digit;
- displayName: trimmed 2–80.

Registration cannot accept a role. `POST /api/auth/login` accepts required email/password and returns AuthResponse. Any credential mismatch is `401 AUTH_INVALID_CREDENTIALS`. `GET /api/auth/me` returns UserDto.

`GET /health` checks PostgreSQL and returns `{"status":"Healthy"}` or `503 DEPENDENCY_UNAVAILABLE`.

## 6. Public discovery

### Shared discovery query

The following fields apply to `GET /api/offers` and `GET /api/offers/map`:

| Query | Default | Rule |
| --- | --- | --- |
| `q` | null | trimmed, max 100; title, description, provider name, case-insensitive |
| `providerId` | null | UUID exact match |
| `category` | null | repeated enum, OR within categories |
| `childAge` | null | integer 0–18 within inclusive offer range |
| `startsFromUtc` | now | inclusive UTC lower start bound |
| `startsToUtc` | end of current Budapest day | exclusive UTC upper start bound, after lower |
| `minPrice` | null | discounted unit price, non-negative |
| `maxPrice` | null | non-negative and >= minPrice |
| `minAvailablePlaces` | 1 | integer 1–10 |
| `latitude` | null | reference latitude, -90..90; paired with longitude |
| `longitude` | null | reference longitude, -180..180; paired with latitude |
| `maxDistanceKm` | null | >0 and <=100; requires reference pair |
| `sort` | `START_TIME` | `START_TIME`, `PRICE`, `DISTANCE`, `DISCOUNT` |
| `direction` | `ASC` | `ASC` or `DESC` |

`DISTANCE` requires reference coordinates. Filters combine with AND; repeated categories use OR. Discovery returns only `PUBLISHED` offers where booking cutoff and start are future and available places meet the requested minimum. Final ordering is UUID ascending after the chosen sort.

### GET /api/offers

Adds `page`/`pageSize` and optional `south`, `west`, `north`, `east`. Bounds must be all absent or all present. Present bounds use the map validation below and filter on offer address. Returns Page<OfferSummaryDto>.

### GET /api/offers/map

Requires:

| Query | Rule |
| --- | --- |
| `south` | -90..90 |
| `north` | -90..90 and > south |
| `west` | -180..180 |
| `east` | -180..180 and > west |
| `limit` | default 100; integer 1–200 |

Latitude span must be <=2 degrees; longitude span <=3 degrees. Anti-meridian boxes are not supported. Inclusion is `latitude >= south && latitude < north && longitude >= west && longitude < east`. Returns MapOfferEnvelope of OfferSummaryDto.

Invalid/missing/unbounded boxes return `400 VALIDATION_FAILED` with errors on the relevant bound. `isTruncated` is based on one extra deterministic row.

### Detail

`GET /api/offers/{id}` optionally accepts paired reference latitude/longitude. `GET /api/providers/{id}` returns ProviderDetailDto. Unknown or never-public resources return the corresponding `404`.

## 7. Existing bookings and favorites

`POST /api/bookings` accepts `{"offerId":"uuid","quantity":2}`, quantity 1–10. The API locks the offer row, revalidates state/cutoff/start/capacity, snapshots authoritative price and display data, and returns `201` plus `Location` and BookingDto. Errors include `OFFER_NOT_BOOKABLE` with `reason` and `INSUFFICIENT_CAPACITY` with the locked `availablePlaces`.

`GET /api/bookings` accepts `scope=ACTIVE|PREVIOUS|ALL` plus page fields. Active means confirmed and not ended. Previous includes cancelled and derived completed. Order is createdAt descending, then ID ascending.

Cancellation remains owner-only, allowed when persisted confirmed, `now <= cancellationDeadlineUtc`, and `now < startsAtUtc`. It restores capacity in the same row-lock transaction. Repeating a successful cancellation returns the same logical cancelled booking and never restores twice.

Favorite offer/provider PUT and DELETE remain idempotent. Favorite pages retain unavailable saved offers. Another user's booking remains indistinguishable from absent.

## 8. Administrator DTOs

### AdminProviderSummaryDto

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "Kerek Erdő Műhely",
  "shortDescription": "Kézműves programok családoknak.",
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "imageUrl": null,
  "activeOfferCount": 2,
  "totalOfferCount": 5,
  "updatedAtUtc": "2026-07-15T09:00:00Z",
  "version": "734"
}
```

AdminProviderDetailDto adds `description` and nullable `phone`, `email`, `websiteUrl`, and `accessibilityInfo`.

### Provider write request

```json
{
  "name": "Kerek Erdő Műhely",
  "shortDescription": "Kézműves programok családoknak.",
  "description": "Családi foglalkozások Újlipótvárosban.",
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "phone": "+3615550100",
  "email": "hello@kerekerdo.example",
  "websiteUrl": "https://example.invalid/kerekerdo",
  "accessibilityInfo": "Babakocsival megközelíthető.",
  "imageUrl": null
}
```

Update has the same fields plus required `version`.

### AdminOfferSummaryDto

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "providerId": "22222222-2222-2222-2222-222222222222",
  "providerName": "Kerek Erdő Műhely",
  "title": "Délutáni agyagozás",
  "category": "WORKSHOP",
  "status": "PUBLISHED",
  "startsAtUtc": "2026-07-15T14:00:00Z",
  "endsAtUtc": "2026-07-15T15:30:00Z",
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "discountedUnitPrice": { "amount": 3200.00, "currency": "HUF" },
  "totalCapacity": 10,
  "reservedQuantity": 6,
  "availablePlaces": 4,
  "updatedAtUtc": "2026-07-15T09:00:00Z",
  "version": "811"
}
```

### AdminOfferDetailDto and write request

Detail contains every write field below plus `id`, providerName, `status`, `reservedQuantity`, `availablePlaces`, `publishedAtUtc`, `archivedAtUtc`, `createdAtUtc`, `updatedAtUtc`, and `version`.

```json
{
  "providerId": "22222222-2222-2222-2222-222222222222",
  "title": "Délutáni agyagozás",
  "description": "Minden eszközt biztosítunk.",
  "category": "WORKSHOP",
  "address": {
    "postalCode": "1137",
    "city": "Budapest",
    "street": "Pozsonyi út 12.",
    "countryCode": "HU",
    "latitude": 47.518200,
    "longitude": 19.050400
  },
  "startsAtUtc": "2026-07-15T14:00:00Z",
  "endsAtUtc": "2026-07-15T15:30:00Z",
  "bookingCutoffUtc": "2026-07-15T13:30:00Z",
  "cancelUntilUtc": "2026-07-15T12:00:00Z",
  "minChildAge": 5,
  "maxChildAge": 10,
  "accompanimentRequired": true,
  "accessibilityInfo": "Babakocsival megközelíthető.",
  "originalUnitPrice": { "amount": 4800.00, "currency": "HUF" },
  "discountedUnitPrice": { "amount": 3200.00, "currency": "HUF" },
  "totalCapacity": 10,
  "imageUrl": null
}
```

Create uses that body and returns a `DRAFT`. Update has the same fields plus required `version`.

### AdminDashboardDto

```json
{
  "providerCount": 4,
  "draftOfferCount": 1,
  "publishedOfferCount": 6,
  "unpublishedOfferCount": 1,
  "archivedOfferCount": 2,
  "startingWithin24HoursCount": 4,
  "nextOffers": []
}
```

`nextOffers` contains at most five AdminOfferSummaryDto values that are `PUBLISHED` and start in the future, ordered by start then ID. Starting-soon uses injected UTC now through now + 24 hours.

## 9. Administrator endpoints and lifecycle

Every endpoint in this section requires `role=admin`.

### Dashboard

`GET /api/admin/dashboard` returns AdminDashboardDto. Counts include all stored providers/offers; starting-soon/next use only published future offers.

### Providers

`GET /api/admin/providers` query:

- `q`: optional trimmed max 100, name/description/city;
- `page` and `pageSize`;
- order: name ascending, ID ascending.

`POST /api/admin/providers` accepts ProviderCreateRequest, returns `201`, Location, and detail. `GET /api/admin/providers/{id}` returns detail. `PUT` accepts ProviderUpdateRequest and returns updated detail. There is no delete.

### Offers

`GET /api/admin/offers` query:

| Query | Rule |
| --- | --- |
| `q` | optional max 100, title/description/provider |
| `providerId` | optional UUID |
| `category` | optional repeated enum |
| `status` | optional repeated `DRAFT/PUBLISHED/UNPUBLISHED/ARCHIVED` |
| `startsFromUtc` / `startsToUtc` | optional UTC bounds; paired ordering |
| `page` / `pageSize` | standard |

Order is startsAtUtc ascending then ID. `POST` creates and returns a `DRAFT` with `201` and Location. `GET /{id}` returns any admin-visible state. `PUT` replaces editable fields and returns detail.

Lifecycle requests have body:

```json
{ "version": "811" }
```

Allowed transitions:

- publish: `DRAFT -> PUBLISHED` and `UNPUBLISHED -> PUBLISHED`;
- unpublish: `PUBLISHED -> UNPUBLISHED`;
- archive: `DRAFT|PUBLISHED|UNPUBLISHED -> ARCHIVED`;
- `ARCHIVED` has no outgoing transition and cannot be updated.

Repeated lifecycle calls are not silently idempotent; if the current state is not an allowed source, return `409 OFFER_STATE_TRANSITION_INVALID` with the current state in `currentStatus`.

Publish revalidates that provider exists, all fields are valid, `bookingCutoffUtc > now`, `startsAtUtc > now`, and `availablePlaces > 0`. Failure is `409 OFFER_PUBLISH_NOT_READY` with field errors. Unpublish/archive does not cancel bookings or decrement reserved quantity.

## 10. Exact administrator validation

Whitespace is trimmed before length checks. Required strings cannot become empty.

Provider:

- name 2–120; shortDescription 2–240; description 10–2000;
- postalCode 1–16; city 2–100; street 2–200;
- countryCode exactly two uppercase ASCII letters;
- latitude -90..90; longitude -180..180;
- phone nullable max 40;
- email nullable valid max 254;
- websiteUrl nullable absolute HTTP/HTTPS max 500;
- accessibilityInfo nullable max 500;
- imageUrl nullable absolute HTTP/HTTPS max 1000.

Offer:

- existing providerId;
- title 2–160; description 10–3000;
- category one of `PLAYHOUSE`, `WORKSHOP`, `MOVEMENT`, `SWIMMING`, `SPORT`, `MUSEUM`, `PARENT_CHILD`;
- event address follows provider address rules;
- all four timestamps are required UTC `Z` instants;
- endsAtUtc > startsAtUtc;
- bookingCutoffUtc <= startsAtUtc and cancelUntilUtc <= startsAtUtc;
- ages are integers with `0 <= minChildAge <= maxChildAge <= 18`;
- both money values follow Money rules, same currency, discounted <= original;
- totalCapacity integer 1–10000;
- accessibilityInfo/imageUrl use provider limits.

Update:

- `version` is required and must match;
- reservedQuantity is never accepted from the client;
- totalCapacity cannot be below reservedQuantity;
- when a confirmed booking exists, providerId, address, startsAtUtc, endsAtUtc, cancelUntilUtc, and currency must be unchanged; otherwise `409 OFFER_UPDATE_CONFLICT` with `conflictingFields`;
- any database constraint failure is mapped to a stable validation/conflict problem, not a raw exception.

## 11. Time-zone contract for the web

Admin forms label and interpret values in `Europe/Budapest`, but send only UTC `Z` strings. The web must:

- round-trip local input to detect nonexistent spring-forward times;
- reject local values with zero or multiple possible instants, including autumn overlap;
- preview the selected UTC instant;
- convert UTC responses back through `Europe/Budapest` rather than the browser zone.

Example: unambiguous Budapest `2026-07-15 16:00` (UTC+02:00) is sent as `2026-07-15T14:00:00Z`. The API does not accept `2026-07-15T16:00` or `2026-07-15T16:00:00+02:00` for admin writes.

## 12. CORS, OpenAPI, and client failure mapping

Development CORS allows exactly configured `http://localhost:5173`, no credentials, Authorization/Content-Type headers, and GET/POST/PUT/DELETE/OPTIONS. Compose admin uses relative same-origin requests and nginx proxying, so it needs no cross-origin exception.

OpenAPI in Development declares all DTOs, enum strings, bearer security, `AdminOnly` operations, problem extensions, nullable fields, query repetition, and every success/error status above.

Client mapping:

- Android `401` clears customer token and returns to auth; login credential error remains inline.
- Admin web `401` clears in-memory session/query cache and returns to login; `403` shows unauthorized.
- `INSUFFICIENT_CAPACITY` updates the authoritative limit.
- `OFFER_NOT_BOOKABLE` refreshes detail and renders `reason`.
- `CONCURRENCY_CONFLICT` forces admin detail refresh and explicit re-entry; clients never automatically overwrite.
- `500`, `503`, connectivity, and timeouts are retryable; validation and domain conflicts are not automatically retried.

## 13. Contract acceptance

Automated acceptance demonstrates:

1. registration/login/me preserve customer behavior and expose role;
2. map bounds are mandatory, validated, geographically correct, capped, and report truncation;
3. map and list share every filter and eligibility rule;
4. expired, unpublished, archived, full, and cutoff offers are absent from discovery and unbookable;
5. customer/invalid tokens receive `403`/`401` for admin APIs;
6. admin can create/update a provider and create/publish an offer;
7. a published admin-created offer appears through public map/list discovery;
8. invalid inputs and lifecycle transitions produce exact problem codes/fields;
9. capacity cannot be set below reserved quantity and protected event fields cannot move under confirmed bookings;
10. two final-place bookings still produce exactly one `201` and one `409 INSUFFICIENT_CAPACITY`;
11. cancellation and favorites retain original ownership/idempotency behavior;
12. DST gap/overlap inputs are blocked in web conversion and only UTC reaches the API;
13. generated OpenAPI matches this endpoint, authorization, enum, and error contract.
