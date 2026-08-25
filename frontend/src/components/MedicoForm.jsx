import React, { useState } from 'react'
import { MEDICOS_API } from '../config.js'
import { readErrorMessage } from '../utils.js'
import FloatingInput from './FloatingInput.jsx'

export default function MedicoForm({medico, onGuardado, onCancelarEdicion, notify, cuentasDisponibles}){
  const isEditing = !!medico
  const [nombre, setNombre] = useState(medico?.nombre ?? '')
  const [especialidad, setEspecialidad] = useState(medico?.especialidad ?? '')
  const [matricula, setMatricula] = useState(medico?.matricula ?? '')
  const [telefono, setTelefono] = useState(medico?.telefono ?? '')
  const [direccion, setDireccion] = useState(medico?.direccion ?? '')
  const [email, setEmail] = useState(medico?.email ?? '')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = isEditing ? `${MEDICOS_API}/${medico.id}` : MEDICOS_API
      const resp = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, especialidad, matricula, telefono, direccion, email})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      if(!isEditing){ setNombre(''); setEspecialidad(''); setMatricula(''); setTelefono(''); setDireccion(''); setEmail('') }
      notify(isEditing ? 'Cambios guardados.' : 'Médico agregado.', 'success')
      await onGuardado()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
      <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <FloatingInput label="Especialidad" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} required />
      <FloatingInput label="Matrícula" value={matricula} onChange={e=>setMatricula(e.target.value)} required />
      <FloatingInput label="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <FloatingInput className="sm:col-span-2" label="Dirección" value={direccion} onChange={e=>setDireccion(e.target.value)} />
      <label className="sm:col-span-2 block">
        <span className="label">Cuenta de acceso vinculada</span>
        <select className="input-field" value={email} onChange={e=>setEmail(e.target.value)}>
          <option value="">Sin vincular</option>
          {email && !cuentasDisponibles.some(c => c.email === email) && (
            <option value={email}>{email} (cuenta no encontrada)</option>
          )}
          {cuentasDisponibles.map(c => (
            <option key={c.id} value={c.email}>{c.nombre} ({c.email})</option>
          ))}
        </select>
      </label>
      <div className="sm:col-span-2 flex gap-3">
        <button type="submit" disabled={loading} className="btn-primary sm:w-fit">
          {loading ? 'Guardando…' : isEditing ? 'Guardar cambios' : 'Agregar médico'}
        </button>
        {isEditing && (
          <button type="button" onClick={onCancelarEdicion} className="btn-secondary sm:w-fit">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
