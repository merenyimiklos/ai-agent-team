# UgorjBe MVP API contract

**Status:** Frozen for backend and Android implementation  
**Base URL (Android Emulator debug):** `http://10.0.2.2:8080`  
**Media types:** `application/json`; errors use `application/problem+json`  
**Authentication:** `Authorization: Bearer <accessToken>` where required

This document is the human-readable OpenAPI source of truth. The backend's generated Swagger document must match it. Endpoint paths are unversioned for the MVP. Breaking changes require coordinated backend, Android, contract-test, and documentation updates.

## 1. Wire conventions

- JSON names are camelCase and unknown response fields should be ignored by clients.
- IDs are UUID strings.
- Timestamps are ISO 8601 UTC strings ending in `Z`.
- Money amounts are decimal JSON numbers, never localized strings or binary floating-point calculations.
- Enum values are the uppercase spellings shown here.
- Request bodies reject unknown enum values and malformed UUIDs/timestamps.
- Optional absent values are JSON `null` when the field is present. Request fields not marked optional are required.
- Protected endpoints return `401` for a missing/invalid/expired bearer token.
- `GET` collection endpoints use one-based pagination and return stable results with UUID as the final ascending tie-breaker.

### `Money`

```json
{
  "amount": 3200.00,
  "currency": "HUF"
}
```

`currency` is an uppercase ISO 4217 code. One booking must use one currency.

### `Address`

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

`countryCode` is ISO 3166-1 alpha-2. Coordinates are WGS84.

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

`page` defaults to 1; `pageSize` defaults to 20 and must be 1-50. Requesting a page beyond the end returns `200` with empty `items` and accurate metadata.

## 2. Error contract

Every API failure uses RFC 7807 plus a stable application code:

```json
{
  "type": "urn:ugorjbe:problem:insufficient-capacity",
  "title": "Nincs elegendő szabad hely.",
  "status": 409,
  "detail": "A kért 3 helyből jelenleg 2 foglalható.",
  "instance": "/api/bookings",
  "code": "INSUFFICIENT_CAPACITY",
  "traceId": "00-7e...-01",
  "availablePlaces": 2
}
```

Validation adds field errors:

```json
{
  "type": "urn:ugorjbe:problem:validation-failed",
  "title": "A kérés érvénytelen.",
  "status": 400,
  "detail": "Egy vagy több mező hibás.",
  "instance": "/api/auth/register",
  "code": "VALIDATION_FAILED",
  "traceId": "00-7e...-01",
  "errors": {
    "email": ["Érvényes e-mail-cím szükséges."],
    "password": ["A jelszó legalább 8 karakter legyen."]
  }
}
```

Clients branch on `code`, not localized `title` or `detail`. Required codes are:

| HTTP | Code | Used when |
| ---: | --- | --- |
| 400 | `VALIDATION_FAILED` | Body/query/route validation fails |
| 401 | `AUTH_REQUIRED` | Token missing, invalid, or expired |
| 401 | `AUTH_INVALID_CREDENTIALS` | Login email/password does not match |
| 409 | `AUTH_EMAIL_EXISTS` | Normalized registration email already exists |
| 404 | `OFFER_NOT_FOUND` | Offer does not exist or is not visible |
| 409 | `OFFER_NOT_BOOKABLE` | Withdrawn, started, or booking cutoff reached |
| 409 | `INSUFFICIENT_CAPACITY` | Fewer places remain than requested; includes `availablePlaces` |
| 404 | `PROVIDER_NOT_FOUND` | Provider does not exist |
| 404 | `BOOKING_NOT_FOUND` | Booking absent or owned by another user |
| 409 | `CANCELLATION_NOT_ALLOWED` | Booking started/completed or cancellation deadline passed |
| 503 | `DEPENDENCY_UNAVAILABLE` | Health/dependency failure |
| 500 | `INTERNAL_ERROR` | Unexpected server failure with no internal details exposed |

Favorite target misses use `OFFER_NOT_FOUND` or `PROVIDER_NOT_FOUND`. Unsupported content type may use the framework status but must retain the same problem shape and `VALIDATION_FAILED` code.

## 3. DTOs

### `UserDto`

```json
{
  "id": "11111111-1111-1111-1111-111111111111",
  "email": "demo@ugorjbe.local",
  "displayName": "Demó Család",
  "locale": "hu-HU",
  "createdAtUtc": "2026-07-14T08:00:00Z"
}
```

### `AuthResponse`

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresAtUtc": "2026-07-14T12:00:00Z",
  "user": { "id": "11111111-1111-1111-1111-111111111111", "email": "demo@ugorjbe.local", "displayName": "Demó Család", "locale": "hu-HU", "createdAtUtc": "2026-07-14T08:00:00Z" }
}
```

### `ProviderSummaryDto`

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "Kerek Erdő Műhely",
  "shortDescription": "Kézműves programok családoknak.",
  "address": { "postalCode": "1137", "city": "Budapest", "street": "Pozsonyi út 12.", "countryCode": "HU", "latitude": 47.518200, "longitude": 19.050400 },
  "imageUrl": null
}
```

### `ProviderDetailDto`

All summary fields plus:

```json
{
  "id": "22222222-2222-2222-2222-222222222222",
  "name": "Kerek Erdő Műhely",
  "shortDescription": "Kézműves programok családoknak.",
  "description": "Részletes, szolgáltató által megadott bemutatkozás.",
  "address": { "postalCode": "1137", "city": "Budapest", "street": "Pozsonyi út 12.", "countryCode": "HU", "latitude": 47.518200, "longitude": 19.050400 },
  "phone": "+3615550100",
  "email": "hello@kerekerdo.example",
  "websiteUrl": "https://example.invalid/kerekerdo",
  "accessibilityInfo": "Babakocsival megközelíthető.",
  "imageUrl": null,
  "activeOfferCount": 2
}
```

Contact and accessibility fields are nullable. `activeOfferCount` uses the same live/bookable rules as the default offer list.

### `OfferSummaryDto`

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "provider": {
    "id": "22222222-2222-2222-2222-222222222222",
    "name": "Kerek Erdő Műhely",
    "shortDescription": "Kézműves programok családoknak.",
    "address": { "postalCode": "1137", "city": "Budapest", "street": "Pozsonyi út 12.", "countryCode": "HU", "latitude": 47.518200, "longitude": 19.050400 },
    "imageUrl": null
  },
  "title": "Délutáni agyagozás",
  "category": "WORKSHOP",
  "startsAtUtc": "2026-07-14T14:00:00Z",
  "endsAtUtc": "2026-07-14T15:30:00Z",
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

`distanceKm` is nullable and is populated only when the list request supplies coordinates. `discountPercent` is the whole number nearest to `(original-discounted)/original*100`, or zero when original price is zero.

### `OfferDetailDto`

All summary fields plus detail and booking rules:

```json
{
  "id": "33333333-3333-3333-3333-333333333333",
  "provider": { "id": "22222222-2222-2222-2222-222222222222", "name": "Kerek Erdő Műhely", "shortDescription": "Kézműves programok családoknak.", "address": { "postalCode": "1137", "city": "Budapest", "street": "Pozsonyi út 12.", "countryCode": "HU", "latitude": 47.518200, "longitude": 19.050400 }, "imageUrl": null },
  "title": "Délutáni agyagozás",
  "description": "Minden eszközt biztosítunk; kérjük, érkezzetek tíz perccel korábban.",
  "category": "WORKSHOP",
  "startsAtUtc": "2026-07-14T14:00:00Z",
  "endsAtUtc": "2026-07-14T15:30:00Z",
  "bookingCutoffUtc": "2026-07-14T13:30:00Z",
  "cancelUntilUtc": "2026-07-14T12:00:00Z",
  "minChildAge": 5,
  "maxChildAge": 10,
  "accompanimentRequired": true,
  "accessibilityInfo": "Babakocsival megközelíthető.",
  "originalUnitPrice": { "amount": 4800.00, "currency": "HUF" },
  "discountedUnitPrice": { "amount": 3200.00, "currency": "HUF" },
  "discountPercent": 33,
  "totalCapacity": 10,
  "availablePlaces": 4,
  "isBookable": true,
  "unavailableReason": null,
  "paymentMethod": "PAY_ON_ARRIVAL",
  "distanceKm": null,
  "imageUrl": null
}
```

`unavailableReason` is nullable or one of `SOLD_OUT`, `BOOKING_CLOSED`, `STARTED`, `WITHDRAWN`. Detail remains readable for an expired/sold-out seeded offer; an unknown or non-public offer is `404`.

### `BookingDto`

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
  "createdAtUtc": "2026-07-14T10:05:00Z",
  "cancelledAtUtc": null,
  "cancellationAllowed": true,
  "cancellationDeadlineUtc": "2026-07-14T12:00:00Z",
  "offer": {
    "id": "33333333-3333-3333-3333-333333333333",
    "title": "Délutáni agyagozás",
    "category": "WORKSHOP",
    "providerId": "22222222-2222-2222-2222-222222222222",
    "providerName": "Kerek Erdő Műhely",
    "startsAtUtc": "2026-07-14T14:00:00Z",
    "endsAtUtc": "2026-07-14T15:30:00Z",
    "address": { "postalCode": "1137", "city": "Budapest", "street": "Pozsonyi út 12.", "countryCode": "HU", "latitude": 47.518200, "longitude": 19.050400 },
    "imageUrl": null
  }
}
```

`status` is `CONFIRMED`, `CANCELLED`, or derived `COMPLETED`. `cancellationAllowed` is authoritative at response time. The code is unique and case-insensitive; the QR payload is not a bearer credential.

## 4. Endpoint summary

| Method | Path | Auth | Success |
| --- | --- | --- | --- |
| GET | `/health` | Public | 200 |
| POST | `/api/auth/register` | Public | 201 |
| POST | `/api/auth/login` | Public | 200 |
| GET | `/api/auth/me` | Bearer | 200 |
| GET | `/api/offers` | Public | 200 |
| GET | `/api/offers/{offerId}` | Public | 200 |
| GET | `/api/providers/{providerId}` | Public | 200 |
| POST | `/api/bookings` | Bearer | 201 |
| GET | `/api/bookings` | Bearer | 200 |
| GET | `/api/bookings/{bookingId}` | Bearer | 200 |
| POST | `/api/bookings/{bookingId}/cancel` | Bearer | 200 |
| GET | `/api/favorites/offers` | Bearer | 200 |
| PUT | `/api/favorites/offers/{offerId}` | Bearer | 204 |
| DELETE | `/api/favorites/offers/{offerId}` | Bearer | 204 |
| GET | `/api/favorites/providers` | Bearer | 200 |
| PUT | `/api/favorites/providers/{providerId}` | Bearer | 204 |
| DELETE | `/api/favorites/providers/{providerId}` | Bearer | 204 |

## 5. Health

### `GET /health`

Checks API process and PostgreSQL connectivity.

`200 OK`:

```json
{ "status": "Healthy" }
```

Dependency failure returns `503 DEPENDENCY_UNAVAILABLE` in the standard problem shape.

## 6. Authentication

### `POST /api/auth/register`

Request:

```json
{
  "email": "szulo@example.hu",
  "password": "Biztonsagos1",
  "displayName": "Kiss Anna"
}
```

- `email`: valid format, maximum 254 characters, trimmed and normalized case-insensitively.
- `password`: 8-128 characters with uppercase, lowercase, and digit.
- `displayName`: trimmed, 2-80 characters.

Returns `201 Created` and `AuthResponse`. Errors: `400 VALIDATION_FAILED`, `409 AUTH_EMAIL_EXISTS`.

### `POST /api/auth/login`

Request:

```json
{ "email": "demo@ugorjbe.local", "password": "UgorjBe123!" }
```

Returns `200 OK` and `AuthResponse`. Malformed input is `400`; any email/password mismatch is `401 AUTH_INVALID_CREDENTIALS`.

### `GET /api/auth/me`

Returns `200 OK` and `UserDto` for the bearer subject, or `401 AUTH_REQUIRED`.

## 7. Catalog

### `GET /api/offers`

Returns a page of published, bookable offers with at least the requested availability.

| Query | Type/default | Rule |
| --- | --- | --- |
| `q` | string/null | Trimmed, max 100; case-insensitive title, description, and provider-name match |
| `providerId` | UUID/null | Exact provider |
| `category` | repeated enum/null | OR across valid category values |
| `childAge` | integer/null | 0-18 and inside the offer's inclusive age range |
| `startsFromUtc` | instant/now | Inclusive lower start-time bound |
| `startsToUtc` | instant/end of current Budapest day | Exclusive upper start-time bound; must exceed lower bound |
| `minPrice` | decimal/null | Discounted unit price, non-negative |
| `maxPrice` | decimal/null | Discounted unit price, non-negative and >= `minPrice` |
| `minAvailablePlaces` | integer/1 | 1-10 |
| `latitude` | decimal/null | -90..90; must appear with longitude |
| `longitude` | decimal/null | -180..180; must appear with latitude |
| `maxDistanceKm` | decimal/null | >0 and <=100; requires both coordinates |
| `sort` | enum/`START_TIME` | `START_TIME`, `PRICE`, `DISTANCE`, `DISCOUNT`; distance requires coordinates |
| `direction` | enum/`ASC` | `ASC` or `DESC` |
| `page` | integer/1 | >=1 |
| `pageSize` | integer/20 | 1-50 |

Repeated categories are encoded as `?category=WORKSHOP&category=MUSEUM`. When both time bounds are omitted, the API computes now through the end of the current `Europe/Budapest` day and transmits results in UTC. When only one is supplied, the missing bound uses its default. All sorts use offer UUID ascending as the last tie-breaker.

Returns `200` page envelope of `OfferSummaryDto`; errors are `400 VALIDATION_FAILED`.

### `GET /api/offers/{offerId}`

Optional query coordinates `latitude` and `longitude` follow the pair/range rules above and populate `distanceKm`.

Returns `200 OfferDetailDto` or `404 OFFER_NOT_FOUND`. A known public offer may be returned with `isBookable=false`; create-booking performs the definitive check.

### `GET /api/providers/{providerId}`

Returns `200 ProviderDetailDto` or `404 PROVIDER_NOT_FOUND`. Active offers are fetched with `GET /api/offers?providerId=...` so provider payloads stay bounded.

## 8. Bookings

All endpoints require a bearer token and operate only on the current user's bookings.

### `POST /api/bookings`

Request:

```json
{
  "offerId": "33333333-3333-3333-3333-333333333333",
  "quantity": 2
}
```

`quantity` is 1-10. The API ignores any client price/capacity assumptions and snapshots the authoritative offer price. The PostgreSQL row-lock transaction described in `ARCHITECTURE.md` is mandatory.

Returns `201 Created`, `Location: /api/bookings/{id}`, and `BookingDto`.

Errors: `400 VALIDATION_FAILED`, `404 OFFER_NOT_FOUND`, `409 OFFER_NOT_BOOKABLE`, or `409 INSUFFICIENT_CAPACITY`. `INSUFFICIENT_CAPACITY` includes the non-negative `availablePlaces` extension observed under lock.

### `GET /api/bookings`

| Query | Type/default | Rule |
| --- | --- | --- |
| `scope` | enum/`ACTIVE` | `ACTIVE`, `PREVIOUS`, or `ALL` |
| `page` | integer/1 | >=1 |
| `pageSize` | integer/20 | 1-50 |

- `ACTIVE`: persisted confirmed and `endsAtUtc > now`.
- `PREVIOUS`: cancelled, or persisted confirmed with `endsAtUtc <= now` (returned as `COMPLETED`).
- `ALL`: both.

Sort order is `createdAtUtc DESC`, then UUID ascending. Returns a page envelope of `BookingDto`.

### `GET /api/bookings/{bookingId}`

Returns `200 BookingDto` or `404 BOOKING_NOT_FOUND`. Another user's ID is deliberately indistinguishable from an absent ID.

### `POST /api/bookings/{bookingId}/cancel`

No request body. Cancellation is allowed for a confirmed booking when `now <= cancellationDeadlineUtc` and `now < startsAtUtc`. It restores capacity in the same row-lock transaction.

Returns `200 BookingDto` with `status=CANCELLED`. Repeating cancellation of that cancelled booking returns the same logical result with `200` and does not change capacity again. Errors: `404 BOOKING_NOT_FOUND`, `409 CANCELLATION_NOT_ALLOWED`.

## 9. Favorites

All endpoints require a bearer token. Favorite creation/removal is idempotent.

### `GET /api/favorites/offers`

Accepts `page` and `pageSize`. Returns a page envelope of `OfferSummaryDto`, ordered by favorite creation time descending then offer UUID ascending. Unlike discovery, saved offers remain in this list when sold out or expired; their summary availability may be zero. Android opens detail to display the authoritative unavailable reason.

### `PUT /api/favorites/offers/{offerId}`

Adds the existing offer to the current user's favorites. Returns `204 No Content` whether newly added or already present. Unknown/non-public target: `404 OFFER_NOT_FOUND`.

### `DELETE /api/favorites/offers/{offerId}`

Removes the favorite. Returns `204 No Content` whether present or absent. This operation does not require the target offer still to be live, but an unknown UUID with no favorite is also an idempotent `204` to allow stale local cleanup.

### `GET /api/favorites/providers`

Accepts `page` and `pageSize`. Returns a page envelope of `ProviderSummaryDto`, ordered by favorite creation time descending then provider UUID ascending.

### `PUT /api/favorites/providers/{providerId}`

Adds the existing provider. Returns `204`; repeated add is `204`. Unknown provider: `404 PROVIDER_NOT_FOUND`.

### `DELETE /api/favorites/providers/{providerId}`

Removes the favorite. Returns `204` whether present or absent. An unknown UUID with no favorite also returns `204` for stale local cleanup.

## 10. OpenAPI and Android mapping requirements

Swagger UI and the OpenAPI JSON are enabled in Development. The generated specification must declare bearer auth, request validation, success schemas, the common problem schema, enums, and nullable fields.

Android mapping is fixed as follows:

- HTTP `401` clears the stored token and returns to authentication; login's `AUTH_INVALID_CREDENTIALS` remains an inline login error.
- `INSUFFICIENT_CAPACITY` updates the quantity limit from `availablePlaces` and prompts the user rather than claiming success.
- `OFFER_NOT_BOOKABLE` refreshes offer detail and renders the returned unavailability state.
- `CANCELLATION_NOT_ALLOWED` refreshes booking detail.
- `500`, `503`, connectivity, and timeout failures are retryable UI errors; validation and domain conflicts are not auto-retried.
- A successful booking is rendered only from the returned `BookingDto`; QR content uses its exact `qrPayload`.

## 11. Contract acceptance examples

The implementation is contract-complete when automated tests demonstrate:

1. registration returns a usable bearer token and `/api/auth/me` identity;
2. default offer discovery returns only current Budapest-day bookable inventory;
3. filters combine with AND, while repeated categories combine with OR;
4. an expired/cutoff offer returns `OFFER_NOT_BOOKABLE` on reservation;
5. two requests for the last place yield one `201` and one `409 INSUFFICIENT_CAPACITY`;
6. cancellation before the boundary restores capacity exactly once; after the boundary it returns `CANCELLATION_NOT_ALLOWED`;
7. another user cannot see or cancel a booking;
8. favorite PUT/DELETE operations are idempotent and favorite lists retain unavailable saved items;
9. Android can execute login -> browse -> detail -> reserve -> booking code using `10.0.2.2:8080`.
