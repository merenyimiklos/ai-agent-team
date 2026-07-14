# UgorjBe administration web

Responsive React/TypeScript administration client for the UgorjBe API. Runtime data always comes from the backend; there is no mock catalog or direct database access.

## Local development

Requirements: Node.js 22.12 or newer and the Phase 2 API running at `http://localhost:8081` with development seed enabled.

```powershell
npm ci
npm run dev
```

Open `http://localhost:5173` and use the development administrator account documented in the root README. Override the API origin only when necessary:

```powershell
Copy-Item .env.example .env.local
```

The bearer token is held in React memory only. Refreshing or closing the page intentionally requires a new login.

## Verification

```powershell
npm run lint
npm run typecheck
npm test
npm run build
npm run test:e2e
```

The Playwright flow requires a seeded PostgreSQL-backed API and checks administrator login, provider creation, offer creation, publication, and visibility through the public bounded map endpoint. Optional variables are `E2E_API_BASE_URL`, `E2E_ADMIN_EMAIL`, and `E2E_ADMIN_PASSWORD`. The test creates uniquely named records and does not delete them because the contract intentionally has no delete endpoints.

## Container

The production image builds static assets and serves them with nginx. It accepts `VITE_API_BASE_URL=/api`; the client avoids a duplicated `/api` prefix and nginx proxies API traffic to `api:8080`. The root Compose file owns service orchestration.

All administrator date/time fields are explicitly interpreted in `Europe/Budapest`, reject daylight-saving gaps and overlaps, preview their UTC value, and send only ISO-8601 `Z` instants.
