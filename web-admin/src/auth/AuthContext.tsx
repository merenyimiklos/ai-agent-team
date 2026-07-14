import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { apiRequest, configureApiAuth } from '../api/client'
import type { AuthResponse, User } from '../api/types'

interface AuthState {
  user: User | null
  token: string | null
  login: (email: string, password: string) => Promise<User>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<{ user: User; token: string } | null>(null)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  const logout = useCallback(() => {
    setSession(null)
    configureApiAuth(null)
    queryClient.clear()
  }, [queryClient])

  const forbidden = useCallback(() => navigate('/nincs-jogosultsag', { replace: true }), [navigate])

  useEffect(() => {
    configureApiAuth(session?.token ?? null, logout, forbidden)
  }, [session, logout, forbidden])

  const login = useCallback(async (email: string, password: string) => {
    const response = await apiRequest<AuthResponse>('/api/auth/login', {
      method: 'POST', body: JSON.stringify({ email, password }),
    })
    if (response.user.role !== 'admin') {
      configureApiAuth(null)
      throw new Error('Ehhez a felülethez adminisztrátori jogosultság szükséges.')
    }
    setSession({ user: response.user, token: response.accessToken })
    configureApiAuth(response.accessToken, logout, forbidden)
    return response.user
  }, [logout, forbidden])

  const value = useMemo(() => ({ user: session?.user ?? null, token: session?.token ?? null, login, logout }), [session, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
