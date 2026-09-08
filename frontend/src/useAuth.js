import {useState} from 'react'
import {AUTH_API, AUTH_STORAGE_KEY, MEDICOS_API, PACIENTES_API} from './config.js'
import {apiFetch} from './apiClient.js'

// El JWT vive en una cookie httpOnly que el navegador manda solo
// (credentials: 'include' en cada fetch) — JS no puede leerla ni
// escribirla, así que acá solo guardamos datos no sensibles para
// renderizar la UI sin esperar un round-trip.
export function useAuth(notify){
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  })
  const [vinculado, setVinculado] = useState(null)

  function handleLoginExitoso(data){
    setAuth(data)
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data))
  }

  async function handleLogout(){
    setAuth(null)
    localStorage.removeItem(AUTH_STORAGE_KEY)
    try{
      // Limpia la cookie del lado del servidor. Best-effort: si la llamada
      // de red falla igual ya deslogueamos localmente.
      await fetch(`${AUTH_API}/logout`, {method: 'POST', credentials: 'include'})
    }catch{
      // ignorado a propósito
    }
  }

  async function chequearVinculacion(){
    try{
      const url = auth.role === 'MEDICO' ? `${MEDICOS_API}/me` : `${PACIENTES_API}/me`
      const resp = await apiFetch(url)
      setVinculado(resp.ok)
    }catch(err){
      notify(err.message)
    }
  }

  return {auth, handleLoginExitoso, handleLogout, vinculado, chequearVinculacion}
}
