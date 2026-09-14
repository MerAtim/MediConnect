import {useEffect, useRef, useState} from 'react'

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): notify() encadenaba
// dos setTimeout sin guardar sus ids ni limpiarlos, y no habia forma de
// descartar un toast manualmente antes de que expirara solo. timeoutsRef
// guarda los timeout ids pendientes por toast para poder cancelarlos si
// dismiss() se llama antes de que el auto-hide dispare (evita el doble
// disparo/actualizacion de estado redundante), y se limpian todos al
// desmontar.
export function useToasts(){
  const [toasts, setToasts] = useState([])
  const timeoutsRef = useRef(new Map())

  useEffect(() => {
    const timeouts = timeoutsRef.current
    return () => {
      timeouts.forEach(id => clearTimeout(id))
      timeouts.clear()
    }
  }, [])

  function dismiss(id){
    const pendiente = timeoutsRef.current.get(id)
    if(pendiente) clearTimeout(pendiente)
    setToasts(prev => prev.map(t => t.id === id ? {...t, leaving: true} : t))
    const removeTimeout = setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id))
      timeoutsRef.current.delete(id)
    }, 200)
    timeoutsRef.current.set(id, removeTimeout)
  }

  function notify(message, type = 'error'){
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, {id, message, type, leaving: false}])
    const hideTimeout = setTimeout(() => dismiss(id), 3500)
    timeoutsRef.current.set(id, hideTimeout)
  }

  return {toasts, notify, dismiss}
}
