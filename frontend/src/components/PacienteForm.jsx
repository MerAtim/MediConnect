import React, { useState } from 'react'
import { PACIENTES_API } from '../config.js'
import { readErrorMessage } from '../utils.js'
import FloatingInput from './FloatingInput.jsx'

export default function PacienteForm({paciente, onGuardado, onCancelarEdicion, notify, cuentasDisponibles}){
  const isEditing = !!paciente
  const [nombre, setNombre] = useState(paciente?.nombre ?? '')
  const [dni, setDni] = useState(paciente?.dni ?? '')
  const [telefono, setTelefono] = useState(paciente?.telefono ?? '')
  const [direccion, setDireccion] = useState(paciente?.direccion ?? '')
  const [obraSocial, setObraSocial] = useState(paciente?.obraSocial ?? '')
  const [numeroAfiliado, setNumeroAfiliado] = useState(paciente?.numeroAfiliado ?? '')
  const [plan, setPlan] = useState(paciente?.plan ?? '')
  const [email, setEmail] = useState(paciente?.email ?? '')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = isEditing ? `${PACIENTES_API}/${paciente.id}` : PACIENTES_API
      const resp = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, dni, telefono, direccion, obraSocial, numeroAfiliado, plan, email})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      if(!isEditing){ setNombre(''); setDni(''); setTelefono(''); setDireccion(''); setObraSocial(''); setNumeroAfiliado(''); setPlan(''); setEmail('') }
      notify(isEditing ? 'Cambios guardados.' : 'Paciente agregado.', 'success')
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
      <FloatingInput label="DNI" value={dni} onChange={e=>setDni(e.target.value)} required />
      <FloatingInput label="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <FloatingInput label="Dirección" value={direccion} onChange={e=>setDireccion(e.target.value)} />
      <FloatingInput label="Obra social / prepaga" value={obraSocial} onChange={e=>setObraSocial(e.target.value)} />
      <FloatingInput label="Número de afiliado" value={numeroAfiliado} onChange={e=>setNumeroAfiliado(e.target.value)} />
      <FloatingInput label="Plan" value={plan} onChange={e=>setPlan(e.target.value)} />
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
          {loading ? 'Guardando…' : isEditing ? 'Guardar cambios' : 'Agregar paciente'}
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
