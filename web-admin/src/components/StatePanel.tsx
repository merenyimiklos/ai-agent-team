import type { ReactNode } from 'react'

export function StatePanel({ title, children, action }: { title: string; children?: ReactNode; action?: ReactNode }) {
  return (
    <section className="state-panel" role="status">
      <div className="state-icon" aria-hidden="true">◎</div>
      <h2>{title}</h2>
      {children && <div className="muted">{children}</div>}
      {action}
    </section>
  )
}

export function LoadingPanel({ label = 'Betöltés…' }: { label?: string }) {
  return <StatePanel title={label}><span className="spinner" aria-hidden="true" /> Kérjük, várj egy pillanatot.</StatePanel>
}
