import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { apiRequest, errorMessage, queryString } from '../api/client'
import type { AdminProvider, Page as PageData } from '../api/types'
import { LoadingPanel, StatePanel } from '../components/StatePanel'
import { Page } from './DashboardPage'

export function ProvidersPage() {
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const providers = useQuery({
    queryKey: ['providers', search, page],
    queryFn: () => apiRequest<PageData<AdminProvider>>(`/api/admin/providers${queryString({ q: search, page, pageSize: 20 })}`),
  })

  return <Page>
    <header className="page-header"><div><span className="eyebrow">Partnerhálózat</span><h1>Szolgáltatók</h1><p>Helyszínek, elérhetőségek és a hozzájuk tartozó programok.</p></div><Link className="button primary" to="/szolgaltatok/uj">+ Új szolgáltató</Link></header>
    <section className="toolbar" aria-label="Szolgáltatók szűrése">
      <form className="search-box" onSubmit={(event) => { event.preventDefault(); setPage(1); setSearch(query.trim()) }}>
        <label className="sr-only" htmlFor="provider-search">Keresés</label><span aria-hidden="true">⌕</span><input id="provider-search" placeholder="Név, leírás vagy város…" value={query} onChange={(event) => setQuery(event.target.value)} /><button type="submit" className="button secondary">Keresés</button>
      </form>
    </section>
    {providers.isLoading ? <LoadingPanel /> : providers.isError ? <StatePanel title="Nem tölthetők be a szolgáltatók" action={<button className="button secondary" onClick={() => providers.refetch()}>Újrapróbálom</button>}>{errorMessage(providers.error)}</StatePanel> : providers.data!.items.length === 0 ? <StatePanel title="Nincs találat" action={<Link className="button primary" to="/szolgaltatok/uj">Első szolgáltató felvétele</Link>}>Módosítsd a keresést, vagy rögzíts új partnert.</StatePanel> : <>
      <div className="card-grid">{providers.data!.items.map((provider) => <article className="provider-card" key={provider.id}>
        <div className="provider-visual" aria-hidden="true">{provider.imageUrl ? <img src={provider.imageUrl} alt="" /> : <span>{provider.name.charAt(0)}</span>}</div>
        <div className="provider-body"><span className="eyebrow">{provider.address.city}</span><h2>{provider.name}</h2><p>{provider.shortDescription}</p><div className="provider-meta"><span><b>{provider.activeOfferCount}</b> élő program</span><span><b>{provider.totalOfferCount}</b> összesen</span></div><Link className="button ghost wide" to={`/szolgaltatok/${provider.id}`}>Szerkesztés</Link></div>
      </article>)}</div>
      <Pagination page={providers.data!.page} totalPages={providers.data!.totalPages} onChange={setPage} />
    </>}
  </Page>
}

function Pagination({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) return null
  return <nav className="pagination" aria-label="Lapozás"><button className="button ghost" disabled={page <= 1} onClick={() => onChange(page - 1)}>← Előző</button><span>{page} / {totalPages}. oldal</span><button className="button ghost" disabled={page >= totalPages} onClick={() => onChange(page + 1)}>Következő →</button></nav>
}
