import React from 'react'

// MEDIUM de la re-auditoria e2e (2026-09-08): "prop-drilling excesivo en
// TurnosSection" -- extraido para que TurnosSection no tenga que
// destructurar y reenviar estos 7 props sueltos en su propio nivel.
export default function TurnoFiltros({
  filtroMedicoId, onFiltroMedicoIdChange, filtroPacienteId, onFiltroPacienteIdChange,
  onFiltrar, onVerTodos, loading
}){
  return (
    <form onSubmit={onFiltrar} className="flex flex-wrap items-end gap-3 mb-4">
      <div>
        <label className="label" htmlFor="turnos-filtro-medico-id">Médico ID</label>
        <input id="turnos-filtro-medico-id" type="number" className="input-field w-32" value={filtroMedicoId} onChange={e=>onFiltroMedicoIdChange(e.target.value)} />
      </div>
      <div>
        <label className="label" htmlFor="turnos-filtro-paciente-id">Paciente ID</label>
        <input id="turnos-filtro-paciente-id" type="number" className="input-field w-32" value={filtroPacienteId} onChange={e=>onFiltroPacienteIdChange(e.target.value)} />
      </div>
      <button type="submit" disabled={loading} className="btn-primary">
        {loading ? 'Buscando…' : 'Buscar'}
      </button>
      <button
        type="button"
        disabled={loading}
        onClick={onVerTodos}
        className="btn-secondary"
      >
        Ver todos
      </button>
    </form>
  )
}
