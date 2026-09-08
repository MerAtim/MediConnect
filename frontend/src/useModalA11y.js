import {useEffect, useRef} from 'react'

const SELECTOR_FOCUSABLE = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

// Comportamiento minimo esperado de un dialog modal (WCAG): al abrir, el foco
// se mueve adentro; Tab/Shift+Tab quedan atrapados entre sus elementos
// (no se puede tabular a la pagina de atras); Escape cierra; al cerrar, el
// foco vuelve a donde estaba antes de abrir el modal.
export function useModalA11y(open, onClose){
  const containerRef = useRef(null)
  const elementoPrevioRef = useRef(null)
  // MEDIUM de la re-auditoria e2e (2026-09-08): "refoco espurio en modales
  // por props onClose/onCancel inestables". Antes el efecto de abajo tenia
  // a onClose en sus dependencias -- si el padre pasaba un onClose inline
  // (referencia nueva en cada render, el patron mas comun: onClose={() =>
  // setAlgo(false)}), cualquier re-render del padre ajeno al modal volvia
  // a disparar el efecto entero y robaba el foco de vuelta al primer campo,
  // aunque el usuario ya estuviera escribiendo en otro. Guardar la ultima
  // referencia en un ref (mutacion durante el render, no un efecto -- es el
  // uso previsto de un ref) permite que el efecto de foco/Tab-trap dependa
  // solo de `open`, sin perder acceso al onClose mas reciente para Escape.
  const onCloseRef = useRef(onClose)
  onCloseRef.current = onClose

  useEffect(() => {
    if(!open) return

    elementoPrevioRef.current = document.activeElement
    const container = containerRef.current
    const primero = container?.querySelector(SELECTOR_FOCUSABLE)
    primero?.focus()

    function handleKeyDown(e){
      if(e.key === 'Escape'){
        onCloseRef.current()
        return
      }
      if(e.key !== 'Tab' || !container) return
      const focusables = Array.from(container.querySelectorAll(SELECTOR_FOCUSABLE))
      if(focusables.length === 0) return
      const primero = focusables[0]
      const ultimo = focusables[focusables.length - 1]
      if(e.shiftKey && document.activeElement === primero){
        e.preventDefault()
        ultimo.focus()
      }else if(!e.shiftKey && document.activeElement === ultimo){
        e.preventDefault()
        primero.focus()
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      elementoPrevioRef.current?.focus?.()
    }
  }, [open])

  return containerRef
}
