import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { apiRequest, errorMessage, queryString } from '../api/client'
import type { AdminOfferSummary, OfferStatus, Page as PageData } from '../api/types'
import { formatBudapest, formatHuf } from '../lib/time'
import { LoadingPanel, StatePanel } from '../components/StatePanel'
import { Page, statusLabel } from './DashboardPage'

const statuses: { value: OfferStatus | ''; label: string }[] = [{ value: '', label: 'Minden állapot' }, { value: 'DRAFT', label: 'Piszkozat' }, { value: 'PUBLISHED', label: 'Közzétett' }, { value: 'UNPUBLISHED', label: 'Levett' }, { value: 'ARCHIVED', label: 'Archivált' }]

export function OffersPage() {
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<OfferStatus | ''>('')
  const [page, setPage] = useState(1)
  const offers = useQuery({
    queryKey: ['offers', search, status, page],
    queryFn: () => apiRequest<PageData<AdminOfferSummary>>(`/api/admin/offers${queryString({ q: search, status: status || undefined, page, pageSize: 20 })}`),
  })

  return <Page>
    <header className="page-header"><div><span className="eyebrow">Kínálat</span><h1>Programok</h1><p>Piszkozatok, élő ajánlatok és a lezárt élmények egy helyen.</p></div><Link className="button primary" to="/programok/uj">+ Új program</Link></header>
    <section className="toolbar" aria-label="Programok szűrése">
      <form className="search-box" onSubmit={(event) => { event.preventDefault(); setPage(1); setSearch(query.trim()) }}><label className="sr-only" htmlFor="offer-search">Keresés</label><span aria-hidden="true">⌕</span><input id="offer-search" placeholder="Program vagy szolgáltató…" value={query} onChange={(event) => setQuery(event.target.value)} /><button className="button secondary" type="submit">Keresés</button></form>
      <label className="select-filter"><span className="sr-only">Állapot</span><select value={status} onChange={(event) => { setPage(1); setStatus(event.target.value as OfferStatus | '') }}>{statuses.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
    </section>
    {offers.isLoading ? <LoadingPanel /> : offers.isError ? <StatePanel title="Nem tölthetők be a programok" action={<button className="button secondary" onClick={() => offers.refetch()}>Újrapróbálom</button>}>{errorMessage(offers.error)}</StatePanel> : offers.data!.items.length === 0 ? <StatePanel title="Nincs találat" action={<Link className="button primary" to="/programok/uj">Új program készítése</Link>}>Módosítsd a szűrést, vagy kezdd el az első piszkozatot.</StatePanel> : <>
      <div className="table-wrap"><table><thead><tr><th>Program</th><th>Kezdés</th><th>Ár</th><th>Férőhely</th><th>Állapot</th><th><span className="sr-only">Művelet</span></th></tr></thead><tbody>{offers.data!.items.map((offer) => <tr key={offer.id}><td><strong>{offer.title}</strong><small>{offer.providerName} · {offer.address.city}</small></td><td>{formatBudapest(offer.startsAtUtc)}</td><td>{formatHuf(offer.discountedUnitPrice.amount, offer.discountedUnitPrice.currency)}</td><td>{offer.availablePlaces} / {offer.totalCapacity}</td><td><span className={`status ${offer.status.toLowerCase()}`}>{statusLabel(offer.status)}</span></td><td><Link className="text-link" to={`/programok/${offer.id}`}>Megnyitás →</Link></td></tr>)}</tbody></table></div>
      {offers.data!.totalPages > 1 && <nav className="pagination" aria-label="Lapozás"><button className="button ghost" disabled={page <= 1} onClick={() => setPage(page - 1)}>← Előző</button><span>{page} / {offers.data!.totalPages}. oldal</span><button className="button ghost" disabled={page >= offers.data!.totalPages} onClick={() => setPage(page + 1)}>Következő →</button></nav>}
    </>}
  </Page>
}
