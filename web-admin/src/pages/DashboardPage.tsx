import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { apiRequest, errorMessage } from '../api/client'
import type { Dashboard } from '../api/types'
import { formatBudapest } from '../lib/time'
import { LoadingPanel, StatePanel } from '../components/StatePanel'

export function DashboardPage() {
  const dashboard = useQuery({ queryKey: ['dashboard'], queryFn: () => apiRequest<Dashboard>('/api/admin/dashboard') })
  if (dashboard.isLoading) return <Page><LoadingPanel label="Áttekintés készül…" /></Page>
  if (dashboard.isError) return <Page><StatePanel title="Nem érhető el az áttekintés" action={<button className="button secondary" onClick={() => dashboard.refetch()}>Újrapróbálom</button>}>{errorMessage(dashboard.error)}</StatePanel></Page>
  const data = dashboard.data!

  const cards = [
    ['Szolgáltatók', data.providerCount, 'helyszín és szervező'],
    ['Közzétett', data.publishedOfferCount, 'élő program'],
    ['Piszkozat', data.draftOfferCount, 'előkészítés alatt'],
    ['24 órán belül', data.startingWithin24HoursCount, 'hamarosan indul'],
  ] as const

  return (
    <Page>
      <header className="page-header hero-header">
        <div><span className="eyebrow">Mai iránytű</span><h1>Jó reggelt! Mire készülnek a családok?</h1><p>Nézd át a közelgő programokat, vagy tegyél közzé egy új élményt.</p></div>
        <Link className="button primary" to="/programok/uj">+ Új program</Link>
      </header>
      <section className="metric-grid" aria-label="Fő mutatók">
        {cards.map(([label, value, caption]) => <article className="metric-card" key={label}><span>{label}</span><strong>{value}</strong><small>{caption}</small></article>)}
      </section>
      <div className="dashboard-grid">
        <section className="panel">
          <div className="panel-header"><div><span className="eyebrow">Következő állomások</span><h2>Közelgő programok</h2></div><Link to="/programok">Mindet mutat</Link></div>
          {data.nextOffers.length === 0 ? <StatePanel title="Nincs közelgő közzétett program">Készíts piszkozatot, majd ellenőrzés után tedd közzé.</StatePanel> : (
            <div className="event-list">{data.nextOffers.map((offer) => <Link to={`/programok/${offer.id}`} className="event-row" key={offer.id}><span className="date-tile"><b>{formatBudapest(offer.startsAtUtc).split(' ')[1]}</b><small>{formatBudapest(offer.startsAtUtc).split(' ')[0]}</small></span><span className="event-copy"><strong>{offer.title}</strong><small>{offer.providerName} · {offer.address.city}</small></span><span className={`status ${offer.status.toLowerCase()}`}>{statusLabel(offer.status)}</span></Link>)}</div>
          )}
        </section>
        <aside className="panel quick-panel"><span className="eyebrow">Gyors műveletek</span><h2>Indulhat a szervezés</h2><Link to="/szolgaltatok/uj" className="quick-link"><span>◇</span><div><strong>Új szolgáltató</strong><small>Partner és helyszín rögzítése</small></div>→</Link><Link to="/programok/uj" className="quick-link"><span>◉</span><div><strong>Új program</strong><small>Időpont, ár és férőhely</small></div>→</Link></aside>
      </div>
    </Page>
  )
}

export function Page({ children }: { children: React.ReactNode }) { return <div className="page">{children}</div> }
export function statusLabel(status: string) { return ({ DRAFT: 'Piszkozat', PUBLISHED: 'Közzétett', UNPUBLISHED: 'Levett', ARCHIVED: 'Archivált' } as Record<string, string>)[status] ?? status }
