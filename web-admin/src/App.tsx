import { Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { ProviderFormPage } from './pages/ProviderFormPage'
import { ProvidersPage } from './pages/ProvidersPage'
import { OfferFormPage } from './pages/OfferFormPage'
import { OffersPage } from './pages/OffersPage'
import { NotFoundPage, UnauthorizedPage } from './pages/SimplePages'

export function App() {
  return <Routes>
    <Route path="/bejelentkezes" element={<LoginPage />} />
    <Route path="/nincs-jogosultsag" element={<UnauthorizedPage />} />
    <Route element={<ProtectedRoute />}>
      <Route element={<AppLayout />}>
        <Route index element={<DashboardPage />} />
        <Route path="szolgaltatok" element={<ProvidersPage />} />
        <Route path="szolgaltatok/uj" element={<ProviderFormPage />} />
        <Route path="szolgaltatok/:providerId" element={<ProviderFormPage />} />
        <Route path="programok" element={<OffersPage />} />
        <Route path="programok/uj" element={<OfferFormPage />} />
        <Route path="programok/:offerId" element={<OfferFormPage />} />
      </Route>
    </Route>
    <Route path="*" element={<NotFoundPage />} />
  </Routes>
}
