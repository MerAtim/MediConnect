import React from 'react'
import TurnoCamposFormulario from './TurnoCamposFormulario.jsx'

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
          <label className="label" htmlFor="otorgar-turno-dni">DNI del paciente</label>
          <input id="otorgar-turno-dni" className="input-field w-40" value={dniBusqueda} onChange={e=>onDniBusquedaChange(e.target.value)} />
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
        <TurnoCamposFormulario
          idPrefix="otorgar-turno"
          fechaHora={fechaHora} onFechaHoraChange={onFechaHoraChange}
          especialidad={especialidad} onEspecialidadChange={onEspecialidadChange} especialidades={especialidades}
          medicoId={medicoId} onMedicoIdChange={onMedicoIdChange} medicosPorEspecialidad={medicosPorEspecialidad}
          preparacion={preparacion} onPreparacionChange={onPreparacionChange}
          loading={loading} onSubmit={onSubmit}
          submitLabel="Otorgar turno" submitLabelEnviando="Enviando"
        />
      )}
    </section>
  )
}
