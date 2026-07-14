import { useEffect, useRef } from 'react'

export function ConfirmDialog({
  open, title, message, confirmLabel, tone = 'primary', busy, onConfirm, onCancel,
}: {
  open: boolean
  title: string
  message: string
  confirmLabel: string
  tone?: 'primary' | 'danger'
  busy?: boolean
  onConfirm: () => void
  onCancel: () => void
}) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  return (
    <dialog ref={dialogRef} onCancel={(event) => { event.preventDefault(); onCancel() }}>
      <div className="dialog-body">
        <span className="eyebrow">Megerősítés</span>
        <h2>{title}</h2>
        <p>{message}</p>
        <div className="button-row">
          <button type="button" className="button ghost" onClick={onCancel} disabled={busy}>Mégse</button>
          <button type="button" className={`button ${tone}`} onClick={onConfirm} disabled={busy}>{busy ? 'Mentés…' : confirmLabel}</button>
        </div>
      </div>
    </dialog>
  )
}
