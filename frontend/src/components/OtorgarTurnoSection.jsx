import React from 'react'
import {clearValidity, handleInvalid} from '../utils.js'

export default function OtorgarTurnoSection({
  dniBusqueda, onDniBusquedaChange, onBuscarPorDni, pacienteEncontrado,
  fechaHora, onFechaHoraChange, especialidad, onEspecialidadChange, especialidades,
  medicoId, onMedicoIdChange, medicosPorEspecialidad, preparacion, onPreparacionChange,
  loading, onSubmit
}){
  return (
    <section className="card">
      <h2 className="heading mb-4">Otorgar turno</h2>
      <form onSubmit={onBuscarPorDni} className="flex flex-wrap items-end gap-3 mb-4">
        <div>
          <label className="label">DNI del paciente</label>
          <input className="input-field w-40" value={dniBusqueda} onChange={e=>onDniBusquedaChange(e.target.value)} />
        </div>
        <button type="submit" className="btn-primary">Buscar paciente</button>
      </form>

      {pacienteEncontrado && (
        <div className="mb-4 rounded-lg border border-neutral-200 bg-paper-100 px-4 py-3 text-sm">
          <p className="font-medium text-neutral-800">{pacienteEncontrado.nombre} — DNI {pacienteEncontrado.dni}</p>
          {pacienteEncontrado.obraSocial && (
            <p className="text-neutral-500">{pacienteEncontrado.obraSocial}{pacienteEncontrado.plan ? ` · ${pacienteEncontrado.plan}` : ''}</p>
          )}
        </div>
      )}

      {pacienteEncontrado && (
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="label">Fecha y hora</label>
            <input className="input-field" value={fechaHora} onChange={e=>onFechaHoraChange(e.target.value)} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Especialidad</label>
              <select className="input-field" value={especialidad} onChange={e=>{clearValidity(e); onEspecialidadChange(e.target.value)}} onInvalid={handleInvalid} required>
                <option value="" disabled>Seleccionar especialidad</option>
                {especialidades.map(esp => (
                  <option key={esp} value={esp}>{esp}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Médico</label>
              <select className="input-field" value={medicoId} onChange={e=>{clearValidity(e); onMedicoIdChange(e.target.value)}} onInvalid={handleInvalid} required disabled={!especialidad}>
                <option value="" disabled>Seleccionar médico</option>
                {medicosPorEspecialidad.map(m => (
                  <option key={m.id} value={m.id}>{m.nombre}</option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="label">Preparación (opcional)</label>
            <input
              className="input-field"
              placeholder="Ej: asistir 15 minutos antes y pasar por recepción para dar presente"
              value={preparacion}
              onChange={e=>onPreparacionChange(e.target.value)}
            />
          </div>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Enviando…' : 'Otorgar turno'}
          </button>
        </form>
      )}
    </section>
  )
}
