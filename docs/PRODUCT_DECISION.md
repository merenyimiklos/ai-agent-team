# Product decision: UgorjBe

**Decision date:** 2026-07-14  
**Status:** Accepted for MVP validation  
**Working name:** UgorjBe, pending trademark and domain review

## Decision

Build **UgorjBe**, a Hungarian marketplace for discounted, same-day empty places in nearby family activities and local experiences. The first launch hypothesis targets Budapest and a compact nearby area of Pest County. Families can discover and reserve provider-declared capacity in playhouses, workshops, swimming and movement sessions, sports classes, museum activities, and parent-child programs.

This is the repository's predefined fallback, selected because desk research did not establish a clearly better opportunity. It is a testable product hypothesis, not proof of demand.

## Compared opportunities

Scores are 1–5, where 5 is best. Low-risk scores invert operational and regulatory risk. The weighted total uses two-sided pain 25%, Hungary fit 20%, repeat use 15%, MVP feasibility 15%, differentiation 15%, and low risk 10%.

| Candidate | Pain | Hungary fit | Repeat | Feasibility | Differentiation | Low risk | Weighted total |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| **Same-day family activities** | 4 | 5 | 4 | 4 | 4 | 3 | **82/100** |
| Cancelled beauty/wellness appointments | 5 | 5 | 5 | 2 | 1 | 3 | **75/100** |
| Last-minute sports/group classes | 4 | 4 | 5 | 3 | 2 | 4 | **74/100** |
| Last-minute culture/event tickets | 4 | 4 | 3 | 4 | 1 | 4 | **68/100** |

Beauty and wellness have strong recurring pain, but Salonic already serves more than 7,000 Hungarian professionals and Fresha advertises same-day bookings; a credible entrant would also need calendar integrations. Sports capacity fits the model but competes with memberships and aggregators such as AYCM. Culture is technically straightforward, but Ma este Színház already owns a close last-minute-ticket position and Jegy.hu supports broad family ticket discovery.

Family activity discovery is active but fragmented across directories, editorial sites, venue pages, and conventional ticketing. The research found no prominent Hungarian service centered on provider-declared, capacity-backed, discounted same-day family inventory. That gap is an inference to validate, not a demonstrated market fact.

## Evidence

- Hungary had approximately 1.4 million children under 15 in 2024, and 34% of families had a child under 15 at the 2022 census ([KSH](https://www.ksh.hu/infografika/2024/gyermeknap_2024_en.pdf)).
- Gyereknapok reports more than 1,290 verified programs and venues, while Minimatiné has curated family programs for roughly a decade ([Gyereknapok](https://gyereknapok.hu/), [Minimatiné](https://minimatine.hu/rolunk/)). These sources show supply breadth, not unused capacity.
- Seventy-nine percent of Hungarian internet users bought goods or services online in 2024 ([Eurostat](https://ec.europa.eu/eurostat/web/products-eurostat-news/w/ddn-20250220-3)).
- Beauty has direct booking incumbents ([Salonic](https://partner.salonic.hu/), [Fresha](https://www.fresha.com/lp/en/bt/beauty-salons/in/hungary)); sports has a broad membership substitute reporting 700 facilities in 130 towns ([AYCM](https://www.aycm.hu/kiajanlo.php?nev=AYCM_2026_M_3XL)); and culture has an exact last-minute incumbent ([Ma este Színház](https://www.maesteszinhaz.hu/)).
- Dated leisure services are generally excluded from the standard EU withdrawal right, while clear pre-contract terms remain necessary ([Directive 2011/83/EU, Article 16(l)](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=celex%3A32011L0083), [Hungarian regulation 45/2014](https://njt.jog.gov.hu/jogszabaly/2014-45-20-22)). Final customer terms still require Hungarian legal review.
- Some attractions have NTAK registration and reporting obligations. UgorjBe must not imply that it replaces provider compliance systems ([NTAK attraction guidance](https://info.ntak.hu/attrakcio)).

## Target users and jobs

The primary customer is a parent, grandparent, or caregiver of a child approximately 2–10 years old who unexpectedly has free time today and wants a nearby, age-appropriate, affordable activity without calling multiple venues.

> When we unexpectedly have free time today, help me find a suitable nearby activity, confirm that places really remain, and reserve without calling around.

The primary provider is an independent operator with scheduled staff and fixed or semi-fixed session capacity whose marginal cost for one more participant is lower than the value of an empty place.

> When today's session will run below capacity, help me sell remaining places without permanently lowering my standard price or adding substantial administration.

## Value proposition and product rules

For families: trusted, age-appropriate things to do nearby today, discounted and immediately reservable.

For providers: convert otherwise empty same-day places into incremental revenue and potential repeat customers with controlled, time-limited inventory.

Every offer represents explicit expiring capacity and includes a start time, expiry or booking cutoff, remaining quantity, visible discount, immediate capacity-backed reservation, and booking code or QR payload. Providers control quantity, price, discount, cutoff, and cancellation rules. The MVP uses pay-on-arrival.

## Monetization hypothesis

- Free provider onboarding during launch.
- Test a 12% commission on redeemed bookings, paid by the provider.
- Providers retain pricing control; test discounts around 20–40%.
- No consumer booking fee initially.
- Commission settlement is mocked or manually reconciled in the MVP.

Featured placement or subscriptions are future hypotheses and must not compromise marketplace relevance or trust.

## MVP scope

- Hungarian-localized registration, login, and current-user profile.
- Browse, search, filter, and sort nearby offers valid today.
- Filters for category, child age, start time, price, availability, and distance.
- Offer details with age range, accessibility, accompaniment rules, address, schedule, original and discounted price, ISO currency, remaining capacity, and cancellation cutoff.
- Provider details.
- Reserve one or more places with transactional overbooking prevention and rejection of expired or insufficient-capacity requests.
- Human-readable booking code and QR payload.
- Active and previous bookings, permitted cancellation, and favorite offers or providers.
- Deterministic demo data, pay-on-arrival, and complete loading, empty, error, expired, and retry states.

The recommended concierge pilot is 8–12 providers across playhouse, workshop, movement/swimming, and museum or parent-child categories.

## Non-goals

- A provider application or full administration portal.
- Online payment, refunds, wallets, or SZÉP-card processing.
- Medical appointments, childcare placement, or unsupervised child handoff.
- Dynamic pricing, reviews, chat, social features, waitlists, loyalty, or subscriptions.
- National rollout before local marketplace liquidity is proven.
- Replacing provider fiscal, ticketing, safety, insurance, qualification, or NTAK responsibilities.
- Production deployment or reuse of Munch branding, copy, assets, colors, screenshots, or distinctive screens.

## Risky assumptions and validation gates

1. **Providers have recurring unused capacity.** Interview at least 15 target providers and collect six weeks of capacity data. Pass if the median suitable provider has at least 15% unused capacity in two or more sessions per week.
2. **A compact area can sustain liquidity.** Pilot with 8–12 providers. Pass if the area supports at least ten relevant live offers per day without stale or fabricated inventory.
3. **Families book spontaneously.** Test a Hungarian landing page and manual booking flow. Pass if at least 10% of qualified detail-page visitors reserve and most bookings occur within six hours of start.
4. **Pay-on-arrival has acceptable no-shows.** Add confirmations, reminders, cutoffs, and redemption codes. Pass if at least 85% of reserved seats are redeemed or cancelled before cutoff.
5. **Both sides retain economically.** Test the 12% commission. Pass if providers republish without subsidy, at least 25% of booking households return within 60 days, and support cost does not consume the commission.

## Operational guardrails

- Do not collect child names, birth dates, or other child personal data in the MVP; use quantity and non-identifying age bands only.
- Location permission is optional and manual location entry remains available.
- Identify the provider responsible for the activity and show age, accompaniment, accessibility, cancellation, and no-show rules before reservation.
- Providers remain responsible for service delivery, safety, insurance, qualifications, fiscal compliance, and regulatory reporting.
- Obtain Hungarian legal review before production use of customer terms.
