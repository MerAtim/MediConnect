import React, { useState } from 'react'
import { MEDICOS_API } from '../config.js'
import { useSubmitForm } from '../useSubmitForm.js'
import CuentaVinculadaSelect from './CuentaVinculadaSelect.jsx'
import FloatingInput from './FloatingInput.jsx'

export default function MedicoForm({medico, onGuardado, onCancelarEdicion, notify, cuentasDisponibles}){
  const isEditing = !!medico
  const [nombre, setNombre] = useState(medico?.nombre ?? '')
  const [especialidad, setEspecialidad] = useState(medico?.especialidad ?? '')
  const [matricula, setMatricula] = useState(medico?.matricula ?? '')
  const [telefono, setTelefono] = useState(medico?.telefono ?? '')
  const [direccion, setDireccion] = useState(medico?.direccion ?? '')
  const [email, setEmail] = useState(medico?.email ?? '')
  const {loading, submit} = useSubmitForm(notify)

  async function handleSubmit(e){
    e.preventDefault()
    const url = isEditing ? `${MEDICOS_API}/${medico.id}` : MEDICOS_API
    await submit(url, {
      method: isEditing ? 'PUT' : 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({nombre, especialidad, matricula, telefono, direccion, email})
    }, {
      mensajeExito: isEditing ? 'Cambios guardados.' : 'Médico agregado.',
      onExito: async () => {
        if(!isEditing){ setNombre(''); setEspecialidad(''); setMatricula(''); setTelefono(''); setDireccion(''); setEmail('') }
        await onGuardado()
      }
    })
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
      <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <FloatingInput label="Especialidad" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} required />
      <FloatingInput label="Matrícula" value={matricula} onChange={e=>setMatricula(e.target.value)} required />
      <FloatingInput label="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <FloatingInput className="sm:col-span-2" label="Dirección" value={direccion} onChange={e=>setDireccion(e.target.value)} />
      <CuentaVinculadaSelect email={email} onEmailChange={setEmail} cuentasDisponibles={cuentasDisponibles} />
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
