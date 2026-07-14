import { useEffect, useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiRequest, ApiError, errorMessage } from '../api/client'
import type { AdminProvider, ProviderWrite } from '../api/types'
import { AddressFields, Field } from '../components/FormFields'
import { LoadingPanel, StatePanel } from '../components/StatePanel'
import { emptyProvider, providerFormSchema, type ProviderFormValues } from '../lib/schemas'
import { Page } from './DashboardPage'

export function ProviderFormPage() {
  const { providerId } = useParams()
  const editing = Boolean(providerId)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [globalError, setGlobalError] = useState('')
  const provider = useQuery({ queryKey: ['provider', providerId], queryFn: () => apiRequest<AdminProvider>(`/api/admin/providers/${providerId}`), enabled: editing })
  const form = useForm<ProviderFormValues>({ resolver: zodResolver(providerFormSchema), defaultValues: emptyProvider })

  useEffect(() => {
    if (!provider.data) return
    const { name, shortDescription, description, address, phone, email, websiteUrl, accessibilityInfo, imageUrl, version } = provider.data
    form.reset({ name, shortDescription, description, address, phone: phone ?? '', email: email ?? '', websiteUrl: websiteUrl ?? '', accessibilityInfo: accessibilityInfo ?? '', imageUrl: imageUrl ?? '', version })
  }, [provider.data, form])

  const save = useMutation({
    mutationFn: (values: ProviderFormValues) => {
      const payload: ProviderWrite = { ...values, phone: values.phone || null, email: values.email || null, websiteUrl: values.websiteUrl || null, accessibilityInfo: values.accessibilityInfo || null, imageUrl: values.imageUrl || null }
      return apiRequest<AdminProvider>(editing ? `/api/admin/providers/${providerId}` : '/api/admin/providers', { method: editing ? 'PUT' : 'POST', body: JSON.stringify(payload) })
    },
    onSuccess: async (saved) => { await queryClient.invalidateQueries({ queryKey: ['providers'] }); navigate(`/szolgaltatok/${saved.id}`, { replace: true }) },
    onError: (error) => {
      setGlobalError(errorMessage(error))
      if (error instanceof ApiError) for (const [field, messages] of Object.entries(error.fieldErrors)) form.setError(field as any, { message: messages[0] })
    },
  })

  if (editing && provider.isLoading) return <Page><LoadingPanel /></Page>
  if (editing && provider.isError) return <Page><StatePanel title="A szolgáltató nem tölthető be" action={<button className="button secondary" onClick={() => provider.refetch()}>Újrapróbálom</button>}>{errorMessage(provider.error)}</StatePanel></Page>

  const errors = form.formState.errors
  return <Page>
    <header className="page-header"><div><Link className="back-link" to="/szolgaltatok">← Szolgáltatók</Link><span className="eyebrow">{editing ? 'Partneradatok frissítése' : 'Új együttműködés'}</span><h1>{editing ? provider.data?.name : 'Új szolgáltató'}</h1><p>A pontos helyszín és elérhetőség segíti a családokat a döntésben.</p></div></header>
    {globalError && <div className="alert error" role="alert">{globalError}</div>}
    <form className="editor-layout" onSubmit={form.handleSubmit((values) => { setGlobalError(''); save.mutate(values) })} noValidate>
      <div className="editor-main">
        <fieldset className="form-section"><legend>Bemutatkozás</legend><div className="form-grid">
          <Field label="Név" htmlFor="name" error={errors.name}><input id="name" aria-invalid={!!errors.name} {...form.register('name')} /></Field>
          <Field label="Rövid leírás" htmlFor="shortDescription" error={errors.shortDescription}><input id="shortDescription" aria-invalid={!!errors.shortDescription} {...form.register('shortDescription')} /></Field>
          <div className="span-all"><Field label="Részletes bemutatkozás" htmlFor="description" error={errors.description}><textarea id="description" rows={5} aria-invalid={!!errors.description} {...form.register('description')} /></Field></div>
        </div></fieldset>
        <AddressFields register={form.register} errors={errors} />
        <fieldset className="form-section"><legend>Kapcsolat és akadálymentesség</legend><div className="form-grid">
          <Field label="Telefonszám" htmlFor="phone" error={errors.phone}><input id="phone" type="tel" {...form.register('phone')} /></Field>
          <Field label="E-mail-cím" htmlFor="provider-email" error={errors.email}><input id="provider-email" type="email" {...form.register('email')} /></Field>
          <Field label="Weboldal" htmlFor="website" error={errors.websiteUrl}><input id="website" type="url" placeholder="https://" {...form.register('websiteUrl')} /></Field>
          <Field label="Kép URL" htmlFor="image" error={errors.imageUrl}><input id="image" type="url" placeholder="https://" {...form.register('imageUrl')} /></Field>
          <div className="span-all"><Field label="Akadálymentességi információ" htmlFor="accessibility" error={errors.accessibilityInfo}><textarea id="accessibility" rows={3} {...form.register('accessibilityInfo')} /></Field></div>
        </div></fieldset>
      </div>
      <aside className="save-panel"><span className="eyebrow">Mentés előtt</span><h2>{editing ? 'Frissíted az adatlapot?' : 'Kész a partnerlap?'}</h2><p>A szolgáltató adatai az adminfelületen azonnal frissülnek. Nyilvánosan csak közzétett programon jelennek meg.</p><button className="button primary wide" type="submit" disabled={save.isPending}>{save.isPending ? 'Mentés…' : editing ? 'Módosítások mentése' : 'Szolgáltató létrehozása'}</button><Link className="button ghost wide" to="/szolgaltatok">Mégse</Link></aside>
    </form>
  </Page>
}
