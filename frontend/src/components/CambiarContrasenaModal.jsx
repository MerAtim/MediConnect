import React, { useState } from 'react'
import { apiFetch } from '../apiClient.js'
import { USUARIOS_API } from '../config.js'
import { readErrorMessage } from '../utils.js'
import FloatingInput from './FloatingInput.jsx'

// modo 'propia': el usuario logueado cambia su contraseña sabiendo la
// actual (PATCH /me/contrasena). modo 'reset': un ADMINISTRADOR resetea la
// de otro usuario sin necesitar la vieja (PATCH /{id}/contrasena) — es el
// mecanismo de recuperación, ya que el proyecto no manda emails.
export default function CambiarContrasenaModal({open, modo, usuarioObjetivo, notify, onClose, onExito}){
  const [contrasenaActual, setContrasenaActual] = useState('')
  const [contrasenaNueva, setContrasenaNueva] = useState('')
  const [loading, setLoading] = useState(false)

  if(!open) return null

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = modo === 'propia' ? `${USUARIOS_API}/me/contrasena` : `${USUARIOS_API}/${usuarioObjetivo.id}/contrasena`
      const body = modo === 'propia' ? {contrasenaActual, contrasenaNueva} : {contrasenaNueva}
      const resp = await apiFetch(url, {
        method: 'PATCH',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(body)
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify(modo === 'propia' ? 'Contraseña actualizada.' : `Contraseña reseteada para ${usuarioObjetivo.email}.`, 'success')
      setContrasenaActual(''); setContrasenaNueva('')
      onExito?.()
      onClose()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4">
      <div className="card max-w-sm w-full">
        <h3 className="heading mb-4">
          {modo === 'propia' ? 'Cambiar mi contraseña' : `Resetear contraseña de ${usuarioObjetivo?.nombre}`}
        </h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          {modo === 'propia' && (
            <FloatingInput label="Contraseña actual" type="password" value={contrasenaActual} onChange={e=>setContrasenaActual(e.target.value)} required />
          )}
          <FloatingInput label="Contraseña nueva (mín. 6 caracteres)" type="password" value={contrasenaNueva} onChange={e=>setContrasenaNueva(e.target.value)} required />
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancelar</button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
