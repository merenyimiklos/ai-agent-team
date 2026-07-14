import { z } from 'zod'
import { budapestLocalToUtc } from './time'

const nullableUrl = z
  .string()
  .trim()
  .max(1000, 'Legfeljebb 1000 karakter lehet.')
  .refine((value) => !value || /^https?:\/\//i.test(value), 'HTTP vagy HTTPS URL szükséges.')

export const addressSchema = z.object({
  postalCode: z.string().trim().min(1, 'Kötelező mező.').max(16),
  city: z.string().trim().min(2, 'Legalább 2 karakter.').max(100),
  street: z.string().trim().min(2, 'Legalább 2 karakter.').max(200),
  countryCode: z.string().regex(/^[A-Z]{2}$/, 'Két nagybetűs országkód szükséges.'),
  latitude: z.number().min(-90).max(90),
  longitude: z.number().min(-180).max(180),
})

export const providerFormSchema = z.object({
  name: z.string().trim().min(2, 'Legalább 2 karakter.').max(120),
  shortDescription: z.string().trim().min(2, 'Legalább 2 karakter.').max(240),
  description: z.string().trim().min(10, 'Legalább 10 karakter.').max(2000),
  address: addressSchema,
  phone: z.string().trim().max(40),
  email: z.union([z.literal(''), z.email('Érvényes e-mail-cím szükséges.').max(254)]),
  websiteUrl: nullableUrl.refine((value) => value.length <= 500, 'Legfeljebb 500 karakter lehet.'),
  accessibilityInfo: z.string().trim().max(500),
  imageUrl: nullableUrl,
  version: z.string().optional(),
})

const localTime = z.string().superRefine((value, context) => {
  const result = budapestLocalToUtc(value)
  if (!result.ok) context.addIssue({ code: 'custom', message: result.reason })
})

export const offerFormSchema = z
  .object({
    providerId: z.uuid('Válassz szolgáltatót.'),
    title: z.string().trim().min(2, 'Legalább 2 karakter.').max(160),
    description: z.string().trim().min(10, 'Legalább 10 karakter.').max(3000),
    category: z.enum(['PLAYHOUSE', 'WORKSHOP', 'MOVEMENT', 'SWIMMING', 'SPORT', 'MUSEUM', 'PARENT_CHILD']),
    address: addressSchema,
    startsAtLocal: localTime,
    endsAtLocal: localTime,
    bookingCutoffLocal: localTime,
    cancelUntilLocal: localTime,
    minChildAge: z.number().int().min(0).max(18),
    maxChildAge: z.number().int().min(0).max(18),
    accompanimentRequired: z.boolean(),
    accessibilityInfo: z.string().trim().max(500),
    originalAmount: z.number().min(0).max(9_999_999_999.99),
    discountedAmount: z.number().min(0).max(9_999_999_999.99),
    currency: z.string().regex(/^[A-Z]{3}$/, 'Hárombetűs pénznemkód szükséges.'),
    totalCapacity: z.number().int().min(1).max(10_000),
    imageUrl: nullableUrl,
    version: z.string().optional(),
  })
  .superRefine((value, context) => {
    if (value.minChildAge > value.maxChildAge) context.addIssue({ code: 'custom', path: ['maxChildAge'], message: 'Nem lehet kisebb a minimum életkornál.' })
    if (value.discountedAmount > value.originalAmount) context.addIssue({ code: 'custom', path: ['discountedAmount'], message: 'Nem lehet magasabb az eredeti árnál.' })
    const times = ['startsAtLocal', 'endsAtLocal', 'bookingCutoffLocal', 'cancelUntilLocal'] as const
    const utc = Object.fromEntries(times.map((key) => [key, budapestLocalToUtc(value[key])])) as Record<(typeof times)[number], ReturnType<typeof budapestLocalToUtc>>
    if (utc.startsAtLocal.ok && utc.endsAtLocal.ok && utc.endsAtLocal.utc <= utc.startsAtLocal.utc) {
      context.addIssue({ code: 'custom', path: ['endsAtLocal'], message: 'A befejezésnek a kezdés után kell lennie.' })
    }
    if (utc.startsAtLocal.ok && utc.bookingCutoffLocal.ok && utc.bookingCutoffLocal.utc > utc.startsAtLocal.utc) {
      context.addIssue({ code: 'custom', path: ['bookingCutoffLocal'], message: 'A foglalási határidő nem lehet a kezdés után.' })
    }
    if (utc.startsAtLocal.ok && utc.cancelUntilLocal.ok && utc.cancelUntilLocal.utc > utc.startsAtLocal.utc) {
      context.addIssue({ code: 'custom', path: ['cancelUntilLocal'], message: 'A lemondási határidő nem lehet a kezdés után.' })
    }
  })

export type ProviderFormValues = z.infer<typeof providerFormSchema>
export type OfferFormValues = z.infer<typeof offerFormSchema>

export const emptyProvider: ProviderFormValues = {
  name: '', shortDescription: '', description: '',
  address: { postalCode: '', city: 'Budapest', street: '', countryCode: 'HU', latitude: 47.4979, longitude: 19.0402 },
  phone: '', email: '', websiteUrl: '', accessibilityInfo: '', imageUrl: '',
}

export const emptyOffer: OfferFormValues = {
  providerId: '', title: '', description: '', category: 'WORKSHOP',
  address: { postalCode: '', city: 'Budapest', street: '', countryCode: 'HU', latitude: 47.4979, longitude: 19.0402 },
  startsAtLocal: '', endsAtLocal: '', bookingCutoffLocal: '', cancelUntilLocal: '',
  minChildAge: 0, maxChildAge: 18, accompanimentRequired: false, accessibilityInfo: '',
  originalAmount: 0, discountedAmount: 0, currency: 'HUF', totalCapacity: 1, imageUrl: '',
}
