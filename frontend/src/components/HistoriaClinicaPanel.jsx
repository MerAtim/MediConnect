import React from 'react'

// MEDIUM de la re-auditoria e2e (2026-09-08): "prop-drilling excesivo en
// TurnosSection" -- este era el bloque mas grande de la fila expandible
// (~65 lineas), con 10 props propios mezclados entre los 30 que
// recibia TurnosSection. Solo se monta cuando esMedico && esta abierto
// (lo decide el caller), asi que acá adentro no hace falta repetir esos
// dos chequeos.
export default function HistoriaClinicaPanel({
  turno, historiaPorPaciente, historiaLoading,
  diagnostico, onDiagnosticoChange, tratamientoRegistro, onTratamientoChange,
  observacionesRegistro, onObservacionesChange, guardandoRegistro, onAgregarRegistro
}){
  const registros = historiaPorPaciente[turno.pacienteId]
  return (
    <tr id={`historia-turno-${turno.id}`} className="bg-paper-100/40">
      <td colSpan={8} className="px-4 py-4">
        <div className="space-y-3">
          <h3 className="font-medium text-neutral-700">
            Historia clínica de {turno.pacienteNombre ?? `#${turno.pacienteId}`}
          </h3>
          {historiaLoading ? (
            <p className="text-sm text-neutral-400">Cargando…</p>
          ) : (registros?.length ?? 0) === 0 ? (
            <p className="text-sm text-neutral-400">Sin registros previos.</p>
          ) : (
            <ul className="space-y-2">
              {registros.map(r => (
                <li key={r.id} className="rounded-lg border border-neutral-200 bg-paper-50 px-3 py-2 text-sm">
                  <div className="text-neutral-500">
                    {r.fecha} — {r.medicoNombre ?? `#${r.medicoId}`}{r.medicoEspecialidad ? ` (${r.medicoEspecialidad})` : ''}
                  </div>
                  <div><span className="font-medium">Diagnóstico:</span> {r.diagnostico}</div>
                  <div><span className="font-medium">Tratamiento:</span> {r.tratamiento}</div>
                  {r.observaciones && (
                    <div><span className="font-medium">Observaciones:</span> {r.observaciones}</div>
                  )}
                </li>
              ))}
            </ul>
          )}
          <div className="space-y-2 pt-2 border-t border-neutral-200">
            <p className="text-sm font-medium text-neutral-700">Agregar registro de esta consulta</p>
            <label className="sr-only" htmlFor={`historia-diagnostico-${turno.id}`}>Diagnóstico</label>
            <input
              id={`historia-diagnostico-${turno.id}`}
              className="input-field"
              placeholder="Diagnóstico"
              value={diagnostico}
              onChange={e => onDiagnosticoChange(e.target.value)}
            />
            <label className="sr-only" htmlFor={`historia-tratamiento-${turno.id}`}>Tratamiento</label>
            <input
              id={`historia-tratamiento-${turno.id}`}
              className="input-field"
              placeholder="Tratamiento"
              value={tratamientoRegistro}
              onChange={e => onTratamientoChange(e.target.value)}
            />
            <label className="sr-only" htmlFor={`historia-observaciones-${turno.id}`}>Observaciones (opcional)</label>
            <input
              id={`historia-observaciones-${turno.id}`}
              className="input-field"
              placeholder="Observaciones (opcional)"
              value={observacionesRegistro}
              onChange={e => onObservacionesChange(e.target.value)}
            />
            <button
              type="button"
              disabled={guardandoRegistro || !diagnostico || !tratamientoRegistro}
              onClick={() => onAgregarRegistro(turno)}
              className="btn-primary !px-3 !py-1.5 text-xs"
            >
              {guardandoRegistro ? 'Guardando…' : 'Guardar registro'}
            </button>
          </div>
        </div>
      </td>
    </tr>
  )
}
