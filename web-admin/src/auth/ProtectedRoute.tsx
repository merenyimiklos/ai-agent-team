import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function ProtectedRoute() {
  const { user } = useAuth()
  const location = useLocation()
  if (!user) return <Navigate to="/bejelentkezes" replace state={{ from: `${location.pathname}${location.search}` }} />
  if (user.role !== 'admin') return <Navigate to="/nincs-jogosultsag" replace />
  return <Outlet />
}
