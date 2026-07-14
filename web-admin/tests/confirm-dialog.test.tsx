import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ConfirmDialog } from '../src/components/ConfirmDialog'

describe('lifecycle confirmation', () => {
  it('requires an explicit confirmation', () => {
    HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', '') }
    HTMLDialogElement.prototype.close = function () { this.removeAttribute('open') }
    const confirm = vi.fn()
    render(<ConfirmDialog open title="Közzéteszed?" message="Nyilvánossá válik." confirmLabel="Közzététel" onConfirm={confirm} onCancel={() => undefined} />)
    expect(confirm).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: 'Közzététel' }))
    expect(confirm).toHaveBeenCalledOnce()
  })
})
