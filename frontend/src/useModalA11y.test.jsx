import { describe, test, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useModalA11y } from './useModalA11y.js'

function TestModal({ open, onClose }) {
  const containerRef = useModalA11y(open, onClose)
  if (!open) return null
  return (
    <div ref={containerRef}>
      <input aria-label="Primero" />
      <input aria-label="Segundo" />
    </div>
  )
}

// Reproduce el patron real de CambiarContrasenaModal en App.jsx: onClose se
// pasa como arrow function inline, una referencia nueva en cada render del
// padre. `tick` no se usa para nada mas que forzar ese re-render -- via
// rerender() de Testing Library, sin ningun evento de puntero/teclado que
// por si mismo pudiera mover el foco y viciar el test.
function Harness({ tick }) {
  return <TestModal open={true} onClose={() => {}} />
}

describe('useModalA11y', () => {
  // MEDIUM de la re-auditoria e2e (2026-09-08): "refoco espurio en modales
  // por props onClose/onCancel inestables". El efecto que mueve el foco al
  // abrir el modal dependia de onClose -- si el padre se re-renderizaba por
  // cualquier motivo ajeno al modal (con un onClose inline, referencia
  // nueva cada vez), el efecto se volvia a disparar y robaba el foco de
  // vuelta al primer campo, aunque el usuario ya estuviera escribiendo en
  // otro.
  test('no vuelve a robar el foco cuando el padre se re-renderiza con un onClose inestable', async () => {
    const user = userEvent.setup()
    const { rerender } = render(<Harness tick={0} />)

    const segundo = screen.getByLabelText('Segundo')
    await user.click(segundo)
    expect(segundo).toHaveFocus()

    rerender(<Harness tick={1} />)

    expect(segundo).toHaveFocus()
  })

  test('sigue moviendo el foco al primer campo cuando el modal se abre', async () => {
    render(<Harness tick={0} />)

    expect(screen.getByLabelText('Primero')).toHaveFocus()
  })
})
