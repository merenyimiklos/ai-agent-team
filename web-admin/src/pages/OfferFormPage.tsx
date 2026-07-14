import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiRequest, ApiError, errorMessage } from '../api/client'
import type { AdminOffer, AdminProvider, OfferStatus, OfferWrite, Page as PageData } from '../api/types'
import { AddressFields, Field, LocalDateTimeField } from '../components/FormFields'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { LoadingPanel, StatePanel } from '../components/StatePanel'
import { emptyOffer, offerFormSchema, type OfferFormValues } from '../lib/schemas'
import { budapestLocalToUtc, utcToBudapestLocal } from '../lib/time'
import { Page, statusLabel } from './DashboardPage'

const categoryLabels = { PLAYHOUSE: 'Játszóház', WORKSHOP: 'Műhely', MOVEMENT: 'Mozgás', SWIMMING: 'Úszás', SPORT: 'Sport', MUSEUM: 'Múzeum', PARENT_CHILD: 'Szülő–gyerek' }
type Lifecycle = 'publish' | 'unpublish' | 'archive'

export function OfferFormPage() {
  const { offerId } = useParams()
  const editing = Boolean(offerId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [globalError, setGlobalError] = useState('')
  const [confirmAction, setConfirmAction] = useState<Lifecycle | null>(null)
  const offer = useQuery({ queryKey: ['offer', offerId], queryFn: () => apiRequest<AdminOffer>(`/api/admin/offers/${offerId}`), enabled: editing })
  const providers = useQuery({ queryKey: ['providers', 'form'], queryFn: () => apiRequest<PageData<AdminProvider>>('/api/admin/providers?page=1&pageSize=50') })
  const form = useForm<OfferFormValues>({ resolver: zodResolver(offerFormSchema), defaultValues: emptyOffer })
  const values = useWatch({ control: form.control }) as OfferFormValues

  useEffect(() => {
    const data = offer.data
    if (!data) return
    form.reset({
      providerId: data.providerId, title: data.title, description: data.description, category: data.category, address: data.address,
      startsAtLocal: utcToBudapestLocal(data.startsAtUtc), endsAtLocal: utcToBudapestLocal(data.endsAtUtc),
      bookingCutoffLocal: utcToBudapestLocal(data.bookingCutoffUtc), cancelUntilLocal: utcToBudapestLocal(data.cancelUntilUtc),
      minChildAge: data.minChildAge, maxChildAge: data.maxChildAge, accompanimentRequired: data.accompanimentRequired,
      accessibilityInfo: data.accessibilityInfo ?? '', originalAmount: data.originalUnitPrice.amount,
      discountedAmount: data.discountedUnitPrice.amount, currency: data.originalUnitPrice.currency,
      totalCapacity: data.totalCapacity, imageUrl: data.imageUrl ?? '', version: data.version,
    })
  }, [offer.data, form])

  const save = useMutation({
    mutationFn: (values: OfferFormValues) => {
      const payload = toWriteRequest(values)
      return apiRequest<AdminOffer>(editing ? `/api/admin/offers/${offerId}` : '/api/admin/offers', { method: editing ? 'PUT' : 'POST', body: JSON.stringify(payload) })
    },
    onSuccess: async (saved) => { await Promise.all([queryClient.invalidateQueries({ queryKey: ['offers'] }), queryClient.invalidateQueries({ queryKey: ['dashboard'] })]); queryClient.setQueryData(['offer', saved.id], saved); navigate(`/programok/${saved.id}`, { replace: true }) },
    onError: (error) => mapApiErrors(error, form.setError, setGlobalError),
  })

  const lifecycle = useMutation({
    mutationFn: (action: Lifecycle) => apiRequest<AdminOffer>(`/api/admin/offers/${offerId}/${action}`, { method: 'POST', body: JSON.stringify({ version: offer.data?.version }) }),
    onSuccess: async (saved) => { setConfirmAction(null); setGlobalError(''); queryClient.setQueryData(['offer', offerId], saved); await Promise.all([queryClient.invalidateQueries({ queryKey: ['offers'] }), queryClient.invalidateQueries({ queryKey: ['dashboard'] })]) },
    onError: (error) => { setConfirmAction(null); setGlobalError(errorMessage(error)); void offer.refetch() },
  })

  if ((editing && offer.isLoading) || providers.isLoading) return <Page><LoadingPanel /></Page>
  if ((editing && offer.isError) || providers.isError) return <Page><StatePanel title="A szerkesztő nem tölthető be" action={<button className="button secondary" onClick={() => { void offer.refetch(); void providers.refetch() }}>Újrapróbálom</button>}>{errorMessage(offer.error ?? providers.error)}</StatePanel></Page>

  const errors = form.formState.errors
  const current = offer.data
  const archived = current?.status === 'ARCHIVED'
  return <Page>
    <header className="page-header"><div><Link className="back-link" to="/programok">← Programok</Link><div className="title-line"><span className="eyebrow">{editing ? 'Program szerkesztése' : 'Új program'}</span>{current && <span className={`status ${current.status.toLowerCase()}`}>{statusLabel(current.status)}</span>}</div><h1>{editing ? current?.title : 'Új program összeállítása'}</h1><p>Minden időpont Europe/Budapest szerint adható meg; mentéskor UTC-vé alakítjuk.</p></div></header>
    {globalError && <div className="alert error" role="alert">{globalError}</div>}
    {archived && <div className="alert warning" role="status">Az archivált program végleges és nem szerkeszthető.</div>}
    <form className="editor-layout" onSubmit={form.handleSubmit((data) => { setGlobalError(''); save.mutate(data) })} noValidate>
      <div className="editor-main">
        <fieldset className="form-section" disabled={archived}><legend>Alapadatok</legend><div className="form-grid">
          <Field label="Szolgáltató" htmlFor="providerId" error={errors.providerId}><select id="providerId" aria-invalid={!!errors.providerId} {...form.register('providerId')}><option value="">Válassz szolgáltatót…</option>{providers.data!.items.map((provider) => <option key={provider.id} value={provider.id}>{provider.name}</option>)}</select></Field>
          <Field label="Kategória" htmlFor="category" error={errors.category}><select id="category" {...form.register('category')}>{Object.entries(categoryLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></Field>
          <div className="span-all"><Field label="Program neve" htmlFor="title" error={errors.title}><input id="title" aria-invalid={!!errors.title} {...form.register('title')} /></Field></div>
          <div className="span-all"><Field label="Részletes leírás" htmlFor="offer-description" error={errors.description}><textarea id="offer-description" rows={6} aria-invalid={!!errors.description} {...form.register('description')} /></Field></div>
        </div></fieldset>
        <AddressFields register={form.register} errors={errors} disabled={archived} />
        <fieldset className="form-section" disabled={archived}><legend>Időpontok <small>Europe/Budapest</small></legend><div className="form-grid">
          <LocalDateTimeField label="Kezdés" id="startsAtLocal" value={values.startsAtLocal} error={errors.startsAtLocal} register={form.register('startsAtLocal')} />
          <LocalDateTimeField label="Befejezés" id="endsAtLocal" value={values.endsAtLocal} error={errors.endsAtLocal} register={form.register('endsAtLocal')} />
          <LocalDateTimeField label="Foglalási határidő" id="bookingCutoffLocal" value={values.bookingCutoffLocal} error={errors.bookingCutoffLocal} register={form.register('bookingCutoffLocal')} />
          <LocalDateTimeField label="Lemondási határidő" id="cancelUntilLocal" value={values.cancelUntilLocal} error={errors.cancelUntilLocal} register={form.register('cancelUntilLocal')} />
        </div></fieldset>
        <fieldset className="form-section" disabled={archived}><legend>Korosztály és férőhely</legend><div className="form-grid">
          <Field label="Minimum életkor" htmlFor="minChildAge" error={errors.minChildAge}><input id="minChildAge" type="number" min="0" max="18" {...form.register('minChildAge', { valueAsNumber: true })} /></Field>
          <Field label="Maximum életkor" htmlFor="maxChildAge" error={errors.maxChildAge}><input id="maxChildAge" type="number" min="0" max="18" {...form.register('maxChildAge', { valueAsNumber: true })} /></Field>
          <Field label="Összes férőhely" htmlFor="totalCapacity" error={errors.totalCapacity} hint={current ? `${current.reservedQuantity} hely már foglalt; ez alá nem csökkenthető.` : undefined}><input id="totalCapacity" type="number" min="1" max="10000" {...form.register('totalCapacity', { valueAsNumber: true })} /></Field>
          <label className="check-field"><input type="checkbox" {...form.register('accompanimentRequired')} /><span><strong>Felnőtt kísérő szükséges</strong><small>A részvételhez legyen jelen kísérő.</small></span></label>
        </div></fieldset>
        <fieldset className="form-section" disabled={archived}><legend>Ár és további részletek</legend><div className="form-grid">
          <Field label="Eredeti egységár" htmlFor="originalAmount" error={errors.originalAmount}><input id="originalAmount" type="number" min="0" step="0.01" {...form.register('originalAmount', { valueAsNumber: true })} /></Field>
          <Field label="Kedvezményes egységár" htmlFor="discountedAmount" error={errors.discountedAmount}><input id="discountedAmount" type="number" min="0" step="0.01" {...form.register('discountedAmount', { valueAsNumber: true })} /></Field>
          <Field label="Pénznem" htmlFor="currency" error={errors.currency}><input id="currency" maxLength={3} {...form.register('currency')} /></Field>
          <Field label="Kép URL" htmlFor="offer-image" error={errors.imageUrl}><input id="offer-image" type="url" placeholder="https://" {...form.register('imageUrl')} /></Field>
          <div className="span-all"><Field label="Akadálymentességi információ" htmlFor="offer-accessibility" error={errors.accessibilityInfo}><textarea id="offer-accessibility" rows={3} {...form.register('accessibilityInfo')} /></Field></div>
        </div></fieldset>
      </div>
      <aside className="save-panel"><span className="eyebrow">Állapot és mentés</span><h2>{current ? statusLabel(current.status) : 'Új piszkozat'}</h2><p>{current ? `Szabad hely: ${current.availablePlaces} · Foglalt: ${current.reservedQuantity}` : 'Létrehozás után ellenőrizheted, majd külön teheted közzé.'}</p>{!archived && <button className="button primary wide" type="submit" disabled={save.isPending}>{save.isPending ? 'Mentés…' : editing ? 'Módosítások mentése' : 'Piszkozat létrehozása'}</button>}{current && <LifecycleButtons status={current.status} onAction={setConfirmAction} busy={lifecycle.isPending} />}<Link className="button ghost wide" to="/programok">Vissza a listához</Link></aside>
    </form>
    <ConfirmDialog open={!!confirmAction} title={confirmAction ? confirmCopy[confirmAction].title : ''} message={confirmAction ? confirmCopy[confirmAction].message : ''} confirmLabel={confirmAction ? confirmCopy[confirmAction].label : ''} tone={confirmAction === 'archive' ? 'danger' : 'primary'} busy={lifecycle.isPending} onCancel={() => setConfirmAction(null)} onConfirm={() => confirmAction && lifecycle.mutate(confirmAction)} />
  </Page>
}

function toWriteRequest(values: OfferFormValues): OfferWrite {
  const utc = (value: string) => { const result = budapestLocalToUtc(value); if (!result.ok) throw new Error(result.reason); return result.utc }
  return {
    providerId: values.providerId, title: values.title.trim(), description: values.description.trim(), category: values.category, address: values.address,
    startsAtUtc: utc(values.startsAtLocal), endsAtUtc: utc(values.endsAtLocal), bookingCutoffUtc: utc(values.bookingCutoffLocal), cancelUntilUtc: utc(values.cancelUntilLocal),
    minChildAge: values.minChildAge, maxChildAge: values.maxChildAge, accompanimentRequired: values.accompanimentRequired,
    accessibilityInfo: values.accessibilityInfo || null, originalUnitPrice: { amount: values.originalAmount, currency: values.currency },
    discountedUnitPrice: { amount: values.discountedAmount, currency: values.currency }, totalCapacity: values.totalCapacity,
    imageUrl: values.imageUrl || null, ...(values.version ? { version: values.version } : {}),
  }
}

function mapApiErrors(error: unknown, setError: any, setGlobalError: (message: string) => void) {
  setGlobalError(errorMessage(error))
  if (!(error instanceof ApiError)) return
  const mapping: Record<string, string> = { startsAtUtc: 'startsAtLocal', endsAtUtc: 'endsAtLocal', bookingCutoffUtc: 'bookingCutoffLocal', cancelUntilUtc: 'cancelUntilLocal', 'originalUnitPrice.amount': 'originalAmount', 'discountedUnitPrice.amount': 'discountedAmount', 'originalUnitPrice.currency': 'currency', 'discountedUnitPrice.currency': 'currency' }
  for (const [field, messages] of Object.entries(error.fieldErrors)) setError(mapping[field] ?? field, { message: messages[0] })
}

function LifecycleButtons({ status, onAction, busy }: { status: OfferStatus; onAction: (action: Lifecycle) => void; busy: boolean }) {
  if (status === 'ARCHIVED') return null
  return <div className="lifecycle-actions">{(status === 'DRAFT' || status === 'UNPUBLISHED') && <button className="button secondary wide" type="button" disabled={busy} onClick={() => onAction('publish')}>Közzététel</button>}{status === 'PUBLISHED' && <button className="button secondary wide" type="button" disabled={busy} onClick={() => onAction('unpublish')}>Levétel</button>}<button className="button danger-text wide" type="button" disabled={busy} onClick={() => onAction('archive')}>Archiválás</button></div>
}

const confirmCopy = {
  publish: { title: 'Közzéteszed a programot?', message: 'A program megjelenik a nyilvános Android térképen és listában, ha a foglalási feltételek is teljesülnek.', label: 'Igen, közzéteszem' },
  unpublish: { title: 'Leveszed a programot?', message: 'Új foglalás nem érkezhet, de a meglévő foglalások és férőhelyek változatlanok maradnak.', label: 'Igen, leveszem' },
  archive: { title: 'Végleg archiválod?', message: 'Az archivált program nem szerkeszthető és többé nem tehető közzé. Meglévő foglalás nem törlődik.', label: 'Archiválás' },
}
