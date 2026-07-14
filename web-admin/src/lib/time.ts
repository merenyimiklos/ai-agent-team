import { DateTime } from 'luxon'

export const BUDAPEST_ZONE = 'Europe/Budapest'
const LOCAL_FORMAT = "yyyy-MM-dd'T'HH:mm"

export type LocalTimeResult =
  | { ok: true; utc: string; preview: string }
  | { ok: false; reason: string }

export function budapestLocalToUtc(value: string): LocalTimeResult {
  if (!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(value)) {
    return { ok: false, reason: 'Adj meg teljes dátumot és időt.' }
  }

  const parsed = DateTime.fromFormat(value, LOCAL_FORMAT, {
    zone: BUDAPEST_ZONE,
    setZone: true,
    locale: 'hu-HU',
  })
  if (!parsed.isValid || parsed.toFormat(LOCAL_FORMAT) !== value) {
    return { ok: false, reason: 'Ez a helyi idő nem létezik az óraátállítás miatt.' }
  }

  const possible = parsed.getPossibleOffsets()
  if (possible.length !== 1) {
    return { ok: false, reason: 'Ez a helyi idő kétszer fordul elő az óraátállítás miatt. Válassz másik időpontot.' }
  }

  const utc = possible[0]?.toUTC().toISO({ suppressMilliseconds: true })
  if (!utc) return { ok: false, reason: 'Az időpont nem alakítható UTC-re.' }
  return {
    ok: true,
    utc,
    preview: `${value.replace('T', ' ')} (${BUDAPEST_ZONE}) → ${utc}`,
  }
}

export function utcToBudapestLocal(value: string | null | undefined) {
  if (!value) return ''
  const parsed = DateTime.fromISO(value, { setZone: true }).setZone(BUDAPEST_ZONE)
  return parsed.isValid ? parsed.toFormat(LOCAL_FORMAT) : ''
}

export function formatBudapest(value: string) {
  return DateTime.fromISO(value, { setZone: true })
    .setZone(BUDAPEST_ZONE)
    .setLocale('hu-HU')
    .toLocaleString(DateTime.DATETIME_MED)
}

export function formatHuf(amount: number, currency: string) {
  return new Intl.NumberFormat('hu-HU', { style: 'currency', currency, maximumFractionDigits: 2 }).format(amount)
}
