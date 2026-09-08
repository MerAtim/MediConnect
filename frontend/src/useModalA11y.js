import {useEffect, useRef} from 'react'

const SELECTOR_FOCUSABLE = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

// Comportamiento minimo esperado de un dialog modal (WCAG): al abrir, el foco
// se mueve adentro; Tab/Shift+Tab quedan atrapados entre sus elementos
// (no se puede tabular a la pagina de atras); Escape cierra; al cerrar, el
// foco vuelve a donde estaba antes de abrir el modal.
export function useModalA11y(open, onClose){
  const containerRef = useRef(null)
  const elementoPrevioRef = useRef(null)

  useEffect(() => {
    if(!open) return

    elementoPrevioRef.current = document.activeElement
    const container = containerRef.current
    const primero = container?.querySelector(SELECTOR_FOCUSABLE)
    primero?.focus()

    function handleKeyDown(e){
      if(e.key === 'Escape'){
        onClose()
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
  }, [open, onClose])

  return containerRef
}
