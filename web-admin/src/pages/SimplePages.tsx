import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function UnauthorizedPage() {
  const { user, logout } = useAuth()
  return <main className="center-page"><span className="state-icon">!</span><h1>Nincs jogosultságod</h1><p>A szerver nem engedélyezte ezt az adminisztrátori műveletet.</p>{user ? <button className="button primary" type="button" onClick={logout}>Kijelentkezés és új belépés</button> : <Link className="button primary" to="/bejelentkezes">Vissza a belépéshez</Link>}</main>
}

export function NotFoundPage() {
  return <main className="center-page"><span className="state-icon">?</span><h1>Ez az oldal elkóborolt</h1><p>A keresett adminisztrációs oldal nem található.</p><Link className="button primary" to="/">Vissza az áttekintéshez</Link></main>
}
