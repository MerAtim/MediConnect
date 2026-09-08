import React from 'react'

// MEDIUM de la re-auditoria e2e (2026-09-08): "duplicacion entre
// useMedicos/usePacientes y entre los 3 formularios" -- este select estaba
// repetido palabra por palabra en MedicoForm y PacienteForm.
export default function CuentaVinculadaSelect({email, onEmailChange, cuentasDisponibles}){
  return (
    <label className="sm:col-span-2 block">
      <span className="label">Cuenta de acceso vinculada</span>
      <select className="input-field" value={email} onChange={e=>onEmailChange(e.target.value)}>
        <option value="">Sin vincular</option>
        {email && !cuentasDisponibles.some(c => c.email === email) && (
          <option value={email}>{email} (cuenta no encontrada)</option>
        )}
        {cuentasDisponibles.map(c => (
          <option key={c.id} value={c.email}>{c.nombre} ({c.email})</option>
        ))}
      </select>
    </label>
  )
}
