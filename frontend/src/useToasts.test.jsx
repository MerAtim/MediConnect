import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest'
import { act, render, screen, fireEvent } from '@testing-library/react'
import { useToasts } from './useToasts.js'
import ToastContainer from './components/ToastContainer.jsx'

function Harness(){
  const { toasts, notify, dismiss } = useToasts()
  return (
    <div>
      <button onClick={() => notify('mensaje de prueba', 'success')}>Notificar</button>
      <ToastContainer toasts={toasts} onDismiss={dismiss} />
    </div>
  )
}

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): notify() encadenaba
// dos setTimeout sin guardar sus ids ni limpiarlos, y ToastContainer no
// ofrecia forma de descartar un toast manualmente antes de que expirara
// solo.
describe('useToasts', () => {
  beforeEach(() => { vi.useFakeTimers() })
  afterEach(() => { vi.useRealTimers() })

  test('dismiss manual saca el toast sin esperar los 3500ms del auto-hide', () => {
    render(<Harness />)
    fireEvent.click(screen.getByRole('button', { name: 'Notificar' }))
    expect(screen.getByText('mensaje de prueba')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Descartar aviso' }))
    act(() => { vi.advanceTimersByTime(200) })

    expect(screen.queryByText('mensaje de prueba')).not.toBeInTheDocument()
  })

  test('se autodescarta solo despues de 3500ms si nadie lo cierra', () => {
    render(<Harness />)
    fireEvent.click(screen.getByRole('button', { name: 'Notificar' }))

    act(() => { vi.advanceTimersByTime(3500 + 200) })

    expect(screen.queryByText('mensaje de prueba')).not.toBeInTheDocument()
  })
})
