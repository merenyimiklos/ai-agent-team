import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const navItems = [
  { to: '/', label: 'Áttekintés', icon: '⌂' },
  { to: '/szolgaltatok', label: 'Szolgáltatók', icon: '◇' },
  { to: '/programok', label: 'Programok', icon: '◉' },
]

export function AppLayout() {
  const { user, logout } = useAuth()
  return (
    <div className="app-shell">
      <a href="#main" className="skip-link">Ugrás a tartalomra</a>
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">U</span>
          <div><strong>UgorjBe</strong><small>programkezelő</small></div>
        </div>
        <nav aria-label="Fő navigáció">
          {navItems.map((item) => <NavLink key={item.to} to={item.to} end={item.to === '/'}><span aria-hidden="true">{item.icon}</span>{item.label}</NavLink>)}
        </nav>
        <div className="sidebar-footer">
          <div className="avatar" aria-hidden="true">{user?.displayName.charAt(0).toUpperCase()}</div>
          <div className="account"><strong>{user?.displayName}</strong><small>{user?.email}</small></div>
          <button className="icon-button" type="button" onClick={logout} title="Kijelentkezés" aria-label="Kijelentkezés">↗</button>
        </div>
      </aside>
      <main id="main"><Outlet /></main>
    </div>
  )
}
