import type { ProblemDetails } from './types'

const configuredBase = import.meta.env.VITE_API_BASE_URL as string | undefined
export const API_BASE_URL = (configuredBase ?? 'http://localhost:8081').replace(/\/$/, '')

let accessToken: string | null = null
let unauthorizedHandler: (() => void) | null = null
let forbiddenHandler: (() => void) | null = null

export function configureApiAuth(token: string | null, onUnauthorized?: () => void, onForbidden?: () => void) {
  accessToken = token
  unauthorizedHandler = onUnauthorized ?? null
  forbiddenHandler = onForbidden ?? null
}

export class ApiError extends Error {
  constructor(public readonly problem: ProblemDetails) {
    super(problem.detail ?? problem.title)
    this.name = 'ApiError'
  }

  get status() {
    return this.problem.status
  }

  get fieldErrors() {
    return this.problem.errors ?? {}
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

  const relativePath = API_BASE_URL.endsWith('/api') && (path === '/api' || path.startsWith('/api/')) ? path.slice(4) : path
  const response = await fetch(`${API_BASE_URL}${relativePath}`, { ...init, headers })
  if (response.status === 401 && accessToken) unauthorizedHandler?.()
  if (response.status === 403 && accessToken) forbiddenHandler?.()
  if (!response.ok) {
    let problem: ProblemDetails
    try {
      problem = (await response.json()) as ProblemDetails
    } catch {
      problem = {
        title: 'A kérés nem sikerült.',
        status: response.status,
        detail: 'A szerver nem értelmezhető választ adott.',
        code: 'UNEXPECTED_RESPONSE',
      }
    }
    throw new ApiError({ ...problem, status: problem.status || response.status })
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export function queryString(values: Record<string, string | number | string[] | undefined>) {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(values)) {
    if (value === undefined || value === '') continue
    if (Array.isArray(value)) value.forEach((item) => params.append(key, item))
    else params.set(key, String(value))
  }
  const suffix = params.toString()
  return suffix ? `?${suffix}` : ''
}

export function errorMessage(error: unknown) {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'Ismeretlen hiba történt.'
}
