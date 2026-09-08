import React, { useState } from 'react'
import { PACIENTES_API } from '../config.js'
import { useSubmitForm } from '../useSubmitForm.js'
import CuentaVinculadaSelect from './CuentaVinculadaSelect.jsx'
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
  const {loading, submit} = useSubmitForm(notify)

  async function handleSubmit(e){
    e.preventDefault()
    const url = isEditing ? `${PACIENTES_API}/${paciente.id}` : PACIENTES_API
    await submit(url, {
      method: isEditing ? 'PUT' : 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({nombre, dni, telefono, direccion, obraSocial, numeroAfiliado, plan, email})
    }, {
      mensajeExito: isEditing ? 'Cambios guardados.' : 'Paciente agregado.',
      onExito: async () => {
        if(!isEditing){ setNombre(''); setDni(''); setTelefono(''); setDireccion(''); setObraSocial(''); setNumeroAfiliado(''); setPlan(''); setEmail('') }
        await onGuardado()
      }
    })
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
      <CuentaVinculadaSelect email={email} onEmailChange={setEmail} cuentasDisponibles={cuentasDisponibles} />
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
