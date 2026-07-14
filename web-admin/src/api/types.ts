export type Role = 'customer' | 'admin'
export type OfferStatus = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED' | 'ARCHIVED'
export type Category =
  | 'PLAYHOUSE'
  | 'WORKSHOP'
  | 'MOVEMENT'
  | 'SWIMMING'
  | 'SPORT'
  | 'MUSEUM'
  | 'PARENT_CHILD'

export interface User {
  id: string
  email: string
  displayName: string
  locale: string
  role: Role
  createdAtUtc: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresAtUtc: string
  user: User
}

export interface Address {
  postalCode: string
  city: string
  street: string
  countryCode: string
  latitude: number
  longitude: number
}

export interface Money {
  amount: number
  currency: string
}

export interface Page<T> {
  items: T[]
  page: number
  pageSize: number
  totalCount: number
  totalPages: number
}

export interface ProblemDetails {
  type?: string
  title: string
  status: number
  detail?: string
  instance?: string
  code?: string
  traceId?: string
  errors?: Record<string, string[]>
  currentStatus?: OfferStatus
}

export interface ProviderWrite {
  name: string
  shortDescription: string
  description: string
  address: Address
  phone: string | null
  email: string | null
  websiteUrl: string | null
  accessibilityInfo: string | null
  imageUrl: string | null
  version?: string
}

export interface AdminProvider extends ProviderWrite {
  id: string
  activeOfferCount: number
  totalOfferCount: number
  updatedAtUtc: string
  version: string
}

export interface OfferWrite {
  providerId: string
  title: string
  description: string
  category: Category
  address: Address
  startsAtUtc: string
  endsAtUtc: string
  bookingCutoffUtc: string
  cancelUntilUtc: string
  minChildAge: number
  maxChildAge: number
  accompanimentRequired: boolean
  accessibilityInfo: string | null
  originalUnitPrice: Money
  discountedUnitPrice: Money
  totalCapacity: number
  imageUrl: string | null
  version?: string
}

export interface AdminOffer extends OfferWrite {
  id: string
  providerName: string
  status: OfferStatus
  reservedQuantity: number
  availablePlaces: number
  publishedAtUtc: string | null
  archivedAtUtc: string | null
  createdAtUtc: string
  updatedAtUtc: string
  version: string
}

export interface AdminOfferSummary {
  id: string
  providerId: string
  providerName: string
  title: string
  category: Category
  status: OfferStatus
  startsAtUtc: string
  endsAtUtc: string
  address: Address
  discountedUnitPrice: Money
  totalCapacity: number
  reservedQuantity: number
  availablePlaces: number
  updatedAtUtc: string
  version: string
}

export interface Dashboard {
  providerCount: number
  draftOfferCount: number
  publishedOfferCount: number
  unpublishedOfferCount: number
  archivedOfferCount: number
  startingWithin24HoursCount: number
  nextOffers: AdminOfferSummary[]
}

export interface MapEnvelope<T> {
  items: T[]
  returnedCount: number
  limit: number
  isTruncated: boolean
}
