import { describe, expect, it } from 'vitest'
import { offerFormSchema, providerFormSchema } from '../src/lib/schemas'

const validOffer = {
  providerId: '22222222-2222-4222-8222-222222222222', title: 'Délutáni agyagozás', description: 'Minden eszközt biztosítunk a programhoz.', category: 'WORKSHOP',
  address: { postalCode: '1137', city: 'Budapest', street: 'Pozsonyi út 12.', countryCode: 'HU', latitude: 47.5182, longitude: 19.0504 },
  startsAtLocal: '2026-07-20T16:00', endsAtLocal: '2026-07-20T17:30', bookingCutoffLocal: '2026-07-20T15:30', cancelUntilLocal: '2026-07-20T14:00',
  minChildAge: 5, maxChildAge: 10, accompanimentRequired: true, accessibilityInfo: '', originalAmount: 4800,
  discountedAmount: 3200, currency: 'HUF', totalCapacity: 10, imageUrl: '',
}

describe('admin validation', () => {
  it('accepts a complete offer without reserved quantity', () => {
    const result = offerFormSchema.safeParse(validOffer)
    expect(result.success).toBe(true)
    expect(Object.hasOwn(validOffer, 'reservedQuantity')).toBe(false)
  })

  it('links price and age ordering errors to fields', () => {
    const result = offerFormSchema.safeParse({ ...validOffer, discountedAmount: 5000, minChildAge: 12, maxChildAge: 5 })
    expect(result.success).toBe(false)
    if (!result.success) expect(result.error.issues.map((issue) => issue.path.join('.'))).toEqual(expect.arrayContaining(['discountedAmount', 'maxChildAge']))
  })

  it('rejects non-http provider URLs and invalid coordinates', () => {
    const result = providerFormSchema.safeParse({
      name: 'Partner', shortDescription: 'Családi helyszín', description: 'Hosszabb, érvényes bemutatkozó szöveg.',
      address: { postalCode: '1011', city: 'Budapest', street: 'Fő utca 1.', countryCode: 'HU', latitude: 100, longitude: 19 },
      phone: '', email: '', websiteUrl: 'javascript:alert(1)', accessibilityInfo: '', imageUrl: '',
    })
    expect(result.success).toBe(false)
  })
})
