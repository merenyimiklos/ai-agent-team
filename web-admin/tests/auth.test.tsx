import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../src/auth/AuthContext'
import { LoginPage } from '../src/pages/LoginPage'

describe('administrator authentication', () => {
  it('keeps a successful admin token out of browser storage', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(JSON.stringify({
      accessToken: 'memory-only-token', tokenType: 'Bearer', expiresAtUtc: '2026-07-15T12:00:00Z',
      user: { id: '1', email: 'admin@ugorjbe.local', displayName: 'Admin', locale: 'hu-HU', role: 'admin', createdAtUtc: '2026-07-15T08:00:00Z' },
    }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    render(<QueryClientProvider client={new QueryClient()}><MemoryRouter initialEntries={['/bejelentkezes']}><AuthProvider><Routes><Route path="/bejelentkezes" element={<LoginPage />} /><Route path="/" element={<p>Belépve</p>} /></Routes></AuthProvider></MemoryRouter></QueryClientProvider>)
    fireEvent.change(screen.getByLabelText('E-mail-cím'), { target: { value: 'admin@ugorjbe.local' } })
    fireEvent.change(screen.getByLabelText('Jelszó'), { target: { value: 'secret' } })
    fireEvent.click(screen.getByRole('button', { name: /Belépés a/ }))
    await waitFor(() => expect(screen.getByText('Belépve')).toBeInTheDocument())
    expect(localStorage.length).toBe(0)
    expect(sessionStorage.length).toBe(0)
  })
})
