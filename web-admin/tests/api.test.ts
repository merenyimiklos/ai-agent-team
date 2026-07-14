import { beforeEach, describe, expect, it, vi } from 'vitest'
import { apiRequest, ApiError, configureApiAuth } from '../src/api/client'

describe('API client', () => {
  beforeEach(() => configureApiAuth(null))

  it('adds the in-memory bearer token and maps RFC7807 errors', async () => {
    configureApiAuth('secret-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ title: 'Hibás mező.', status: 400, code: 'VALIDATION_FAILED', errors: { title: ['Kötelező.'] } }), { status: 400, headers: { 'Content-Type': 'application/problem+json' } }))
    await expect(apiRequest('/api/admin/offers')).rejects.toMatchObject({ problem: { code: 'VALIDATION_FAILED', errors: { title: ['Kötelező.'] } } })
    const headers = new Headers(vi.mocked(fetch).mock.calls[0]?.[1]?.headers)
    expect(headers.get('Authorization')).toBe('Bearer secret-token')
  })

  it('expires the active session on 401', async () => {
    const expired = vi.fn()
    configureApiAuth('expired', expired)
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({ title: 'Lejárt.', status: 401 }), { status: 401 }))
    await expect(apiRequest('/api/admin/dashboard')).rejects.toBeInstanceOf(ApiError)
    expect(expired).toHaveBeenCalledOnce()
  })
})
