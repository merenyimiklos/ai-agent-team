import { describe, expect, it } from 'vitest'
import { budapestLocalToUtc, utcToBudapestLocal } from '../src/lib/time'

describe('Budapest time conversion', () => {
  it('converts an unambiguous summer wall time to UTC Z', () => {
    expect(budapestLocalToUtc('2026-07-15T16:00')).toEqual({
      ok: true,
      utc: '2026-07-15T14:00:00Z',
      preview: '2026-07-15 16:00 (Europe/Budapest) → 2026-07-15T14:00:00Z',
    })
  })

  it('rejects the spring DST gap', () => {
    const result = budapestLocalToUtc('2026-03-29T02:30')
    expect(result.ok).toBe(false)
    if (!result.ok) expect(result.reason).toMatch(/nem létezik/)
  })

  it('rejects the autumn DST overlap', () => {
    const result = budapestLocalToUtc('2026-10-25T02:30')
    expect(result.ok).toBe(false)
    if (!result.ok) expect(result.reason).toMatch(/kétszer/)
  })

  it('always displays API instants in Budapest, not browser time', () => {
    expect(utcToBudapestLocal('2026-01-15T14:00:00Z')).toBe('2026-01-15T15:00')
  })
})
