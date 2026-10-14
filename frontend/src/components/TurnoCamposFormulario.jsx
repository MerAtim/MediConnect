import React from 'react'
import {clearValidity, handleInvalid} from '../utils.js'

// Bloque especialidad -> médico -> fecha -> preparación, compartido por
// OtorgarTurnoSection (admin, agrega un paso previo de búsqueda por DNI) y
// ReservarTurnoSection (paciente, reserva para sí mismo). idPrefix evita
// ids de HTML duplicados si ambas secciones llegaran a convivir en la
// misma página (hoy no pasa: son mutuamente excluyentes por rol).
export default function TurnoCamposFormulario({
  idPrefix, fechaHora, onFechaHoraChange, especialidad, onEspecialidadChange, especialidades,
  medicoId, onMedicoIdChange, medicosPorEspecialidad, preparacion, onPreparacionChange,
  loading, onSubmit, submitLabel, submitLabelEnviando
}){
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div>
        <label className="label" htmlFor={`${idPrefix}-fecha-hora`}>Fecha y hora</label>
        <input
          id={`${idPrefix}-fecha-hora`}
          type="datetime-local"
          className="input-field"
          value={fechaHora}
          onChange={e=>onFechaHoraChange(e.target.value)}
        />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label" htmlFor={`${idPrefix}-especialidad`}>Especialidad</label>
          <select
            id={`${idPrefix}-especialidad`}
            className="input-field"
            value={especialidad}
            onChange={e=>{clearValidity(e); onEspecialidadChange(e.target.value)}}
            onInvalid={handleInvalid}
            required
          >
            <option value="" disabled>Seleccionar especialidad</option>
            {especialidades.map(esp => (
              <option key={esp} value={esp}>{esp}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label" htmlFor={`${idPrefix}-medico`}>Médico</label>
          <select
            id={`${idPrefix}-medico`}
            className="input-field"
            value={medicoId}
            onChange={e=>{clearValidity(e); onMedicoIdChange(e.target.value)}}
            onInvalid={handleInvalid}
            required
            disabled={!especialidad}
          >
            <option value="" disabled>Seleccionar médico</option>
            {medicosPorEspecialidad.map(m => (
              <option key={m.id} value={m.id}>{m.nombre}</option>
            ))}
          </select>
        </div>
      </div>
      <div>
        <label className="label" htmlFor={`${idPrefix}-preparacion`}>Preparación (opcional)</label>
        <input
          id={`${idPrefix}-preparacion`}
          className="input-field"
          placeholder="Ej: asistir 15 minutos antes y pasar por recepción para dar presente"
          value={preparacion}
          onChange={e=>onPreparacionChange(e.target.value)}
        />
      </div>
      <button type="submit" disabled={loading} className="btn-primary">
        {loading ? `${submitLabelEnviando}…` : submitLabel}
      </button>
    </form>
  )
}
