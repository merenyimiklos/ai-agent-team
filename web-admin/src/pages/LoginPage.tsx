import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { user, login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  if (user) return <Navigate to="/" replace />
  const intended = (location.state as { from?: string } | null)?.from

  async function submit(event: FormEvent) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      await login(email.trim(), password)
      navigate(intended?.startsWith('/') ? intended : '/', { replace: true })
    } catch (caught) {
      setError(errorMessage(caught))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-story" aria-hidden="true">
        <div className="sun-shape" />
        <div className="story-content">
          <span className="eyebrow light">Együtt a megtelt programokért</span>
          <h1>A jó élményeknek legyen gazdája.</h1>
          <p>Kezeld egy helyen a családoknak szóló programokat, férőhelyeket és megjelenést.</p>
          <div className="picnic-illustration"><span>✦</span><span>●</span><span>⌁</span></div>
        </div>
      </section>
      <section className="login-panel">
        <div className="login-card">
          <div className="brand login-brand"><span className="brand-mark">U</span><div><strong>UgorjBe</strong><small>programkezelő</small></div></div>
          <span className="eyebrow">Biztonságos belépés</span>
          <h2>Üdv újra!</h2>
          <p className="muted">Az adminisztrációhoz jelentkezz be a jogosult fiókoddal.</p>
          {error && <div className="alert error" role="alert">{error}</div>}
          <form onSubmit={submit}>
            <div className="field">
              <label htmlFor="email">E-mail-cím</label>
              <input id="email" name="email" type="email" autoComplete="username" required value={email} onChange={(event) => setEmail(event.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="password">Jelszó</label>
              <input id="password" name="password" type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} />
            </div>
            <button className="button primary wide" type="submit" disabled={busy}>{busy ? 'Belépés…' : 'Belépés a programkezelőbe'}</button>
          </form>
          <p className="security-note">A munkamenet csak ebben a böngészőlapban él. Frissítés után újra be kell jelentkezned.</p>
        </div>
      </section>
    </main>
  )
}
