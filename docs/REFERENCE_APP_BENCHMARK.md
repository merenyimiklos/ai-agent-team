# Phase 4 reference application benchmark

Access date: **2026-07-16**

This benchmark uses only lawfully public, preferably official product and app-store material. No reference screenshot, logo, font, icon, marker, illustration, copy, animation asset or source code is downloaded or committed.

The purpose is to identify reusable product principles, not to recreate another product.

## Sources reviewed

### GreenGo

Official sources:

- https://greengo.com/hu/good-to-know
- https://greengo.com/hu/zones
- https://greengo.com/hu/rental-process
- https://greengo.com/hu/

Observed from the official material:

- the application is the operational centre for locating the nearest vehicle, checking range/charge, reserving, opening and closing a rental;
- map zones and vehicle markers carry operational meaning;
- security and rental state are communicated as part of the core journey;
- the main action changes with the user's current task.

Reusable principle:

- Make the map the primary task canvas and expose only decision-critical data around the selected item.

Original UgorjBe adaptation:

- Price markers replace vehicle markers; a selected event preview shows time, price, remaining places and one clear details action.

Must not be copied:

- GreenGo green identity, exact zone/vehicle marker language, navigation, labels, iconography or rental-state motion.

### wigo

Official app-store sources:

- https://apps.apple.com/hu/app/wigo-carsharing/id6466403312
- https://play.google.com/store/apps/details?id=com.wigo.carsharing

Observed from the official material:

- the product promises flexible use from short to long journeys;
- discounts, credits, vehicle types and current availability are differentiators;
- recent release notes emphasise clearer pricing, differentiated badges and smoother usability.

Reusable principle:

- Show price/benefit differences at the point of selection, not behind an information wall.

Original UgorjBe adaptation:

- Discount and remaining-capacity signals are visible on cards and map selection without creating a dense badge wall.

Must not be copied:

- wigo branding, vehicle badges, product taxonomy, exact price calculator presentation or onboarding screens.

### Munch

Official sources:

- https://hello.munch.eco/hu/hun
- https://hello.munch.eco/en/hun/howitworks

Observed from the official material:

- nearby and currently available offers are the centre of discovery;
- discount magnitude is a primary value signal;
- the journey is presented as a small number of simple steps;
- pickup information and a code complete the customer journey.

Reusable principle:

- Let time, savings, proximity and availability dominate the scan order.

Original UgorjBe adaptation:

- Editorial experience cards show image, start time, title, provider, discount, price and capacity in one scan; the booking code is visually dominant after success.

Must not be copied:

- Munch name, food-rescue language, exact discount treatment, card layout, copy, imagery, illustrations or treasure-hunt identity.

### Free2move / historical SHARE NOW lineage

Official sources:

- https://www.free2move.com/at/en/car-sharing/app/
- https://apps.apple.com/hu/app/free2move-car-share-rental/id514921710

Observed from the official material:

- the dynamic map displays currently available resources;
- the nearest item is surfaced quickly;
- find, reserve and unlock are reduced to a few mobile actions;
- filters and trip duration serve the user's immediate decision.

Reusable principle:

- Keep discovery operational, fast and location-aware, with a strong single primary action.

Original UgorjBe adaptation:

- Map/List share one filter/result state; moving the camera retains current markers until the user explicitly searches the area.

Must not be copied:

- Free2move/SHARE NOW branding, vehicle imagery, map composition, navigation, exact filtering, unlock flow or motion signature.

### `cleengo`

Status: **unresolved and excluded**.

No exact application could be identified from an authoritative source with sufficient confidence. It did not influence Phase 4 decisions.

## Cross-reference comparison

| Dimension | Reusable conclusion | UgorjBe Phase 4 decision |
| --- | --- | --- |
| First impression | Confident brand and immediate task orientation | Forest/cream/coral editorial identity with one clear discovery task |
| Map canvas | Map is operational and mostly unobstructed | Edge-to-edge map with compact floating controls and safe attribution spacing |
| Search/filter | Controls should be compact and directly actionable | One search surface plus a separate filter action with active count |
| Selected item | Decision data belongs near the map | Image-led preview with time, price, capacity and details action |
| Information density | Fewer stronger signals beat chip walls | Category, time, provider, price, discount and capacity only |
| Scanning speed | Numbers need visible hierarchy | Serif editorial title hierarchy plus bold sans numeric values |
| Image use | Images support selection but cannot be required | Cached remote media plus deterministic category artwork |
| Navigation | Primary destinations must remain visible | Four tactile destinations; adaptive rail on larger screens |
| Primary action | One dominant next step | Details/reserve action is visually dominant and server-authoritative |
| Color/contrast | Strong identity needs semantic consistency | Brand tokens map to Material roles; no state is color-only |
| Motion | Motion should confirm state, not entertain continuously | Press feedback, controlled transitions, finite success animation |
| Loading/errors | Failure must remain usable | Geometry-matched skeletons, stale retention, targeted retry and list fallback |
| Accessibility | Product quality includes readable and reachable controls | 48 dp targets, semantics, 200% text review and reduced-motion fallback |

## Adopted design principles

1. The map is a working surface, not a promotional background.
2. The selected item shows the minimum data needed for a confident decision.
3. Price, time and availability are numeric hierarchy anchors.
4. Search and filters stay available without covering the map.
5. Map and List are presentations of one state, not separate destinations.
6. Images enrich the decision but never block it.
7. Motion is tactile, finite and meaningful.
8. Empty, stale, offline and permission-denied states remain intentionally designed.

## Originality boundary

Phase 4 does not reproduce the exact colors, geometry, navigation, screen composition, markers, text, logos, icons, illustrations, fonts or motion signatures of any reference. The resulting system is grounded in UgorjBe's existing administration-web brand language and its own family-activity domain.
