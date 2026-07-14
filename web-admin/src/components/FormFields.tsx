import type { FieldError, FieldErrors, UseFormRegister } from 'react-hook-form'
import { budapestLocalToUtc } from '../lib/time'

export function Field({ label, htmlFor, error, hint, children }: { label: string; htmlFor: string; error?: FieldError; hint?: string; children: React.ReactNode }) {
  const describedBy = error ? `${htmlFor}-error` : hint ? `${htmlFor}-hint` : undefined
  return (
    <div className="field">
      <label htmlFor={htmlFor}>{label}</label>
      {children}
      {hint && !error && <small id={`${htmlFor}-hint`}>{hint}</small>}
      {error && <small className="field-error" id={`${htmlFor}-error`} role="alert">{error.message}</small>}
      {describedBy && <span className="sr-only">Kapcsolt leírás: {describedBy}</span>}
    </div>
  )
}

export function AddressFields({ register, errors, prefix = 'address', disabled = false }: { register: UseFormRegister<any>; errors?: FieldErrors<any>; prefix?: string; disabled?: boolean }) {
  const address = (errors?.address ?? {}) as Record<string, FieldError>
  return (
    <fieldset className="form-section" disabled={disabled}>
      <legend>Helyszín</legend>
      <div className="form-grid">
        <Field label="Irányítószám" htmlFor={`${prefix}-postal`} error={address.postalCode}>
          <input id={`${prefix}-postal`} aria-invalid={!!address.postalCode} {...register(`${prefix}.postalCode`)} />
        </Field>
        <Field label="Város" htmlFor={`${prefix}-city`} error={address.city}>
          <input id={`${prefix}-city`} aria-invalid={!!address.city} {...register(`${prefix}.city`)} />
        </Field>
        <Field label="Utca, házszám" htmlFor={`${prefix}-street`} error={address.street}>
          <input id={`${prefix}-street`} aria-invalid={!!address.street} {...register(`${prefix}.street`)} />
        </Field>
        <Field label="Országkód" htmlFor={`${prefix}-country`} error={address.countryCode}>
          <input id={`${prefix}-country`} maxLength={2} aria-invalid={!!address.countryCode} {...register(`${prefix}.countryCode`)} />
        </Field>
        <Field label="Szélesség" htmlFor={`${prefix}-lat`} error={address.latitude} hint="WGS84, például 47.4979">
          <input id={`${prefix}-lat`} type="number" step="0.000001" aria-invalid={!!address.latitude} {...register(`${prefix}.latitude`, { valueAsNumber: true })} />
        </Field>
        <Field label="Hosszúság" htmlFor={`${prefix}-lng`} error={address.longitude} hint="WGS84, például 19.0402">
          <input id={`${prefix}-lng`} type="number" step="0.000001" aria-invalid={!!address.longitude} {...register(`${prefix}.longitude`, { valueAsNumber: true })} />
        </Field>
      </div>
    </fieldset>
  )
}

export function LocalDateTimeField({ label, id, value, error, register }: { label: string; id: string; value: string; error?: FieldError; register: ReturnType<UseFormRegister<any>> }) {
  const conversion = value ? budapestLocalToUtc(value) : null
  return (
    <Field label={label} htmlFor={id} error={error} hint="Europe/Budapest helyi idő">
      <input id={id} type="datetime-local" aria-invalid={!!error} {...register} />
      {conversion?.ok && <small className="utc-preview">UTC előnézet: {conversion.utc}</small>}
    </Field>
  )
}
