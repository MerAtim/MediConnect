import React from 'react'
import EstadoBadge from './EstadoBadge.jsx'
import SkeletonRows from './SkeletonRows.jsx'

export default function TurnosSection({
  esAdmin, esMedico, esPaciente, puedeGestionarTurnos, hoy,
  turnos, listLoading, filtroMedicoId, onFiltroMedicoIdChange, filtroPacienteId, onFiltroPacienteIdChange,
  onFiltrar, onVerTodos, paginaTurnos, totalPaginasTurnos, onIrAPagina,
  estadoUpdatingId, onCambiarEstado, onIniciarCancelacionPaciente,
  historiaAbiertaId, onToggleHistoria, historiaPorPaciente, historiaLoading,
  diagnostico, onDiagnosticoChange, tratamientoRegistro, onTratamientoChange,
  observacionesRegistro, onObservacionesChange, guardandoRegistro, onAgregarRegistro
}){
  return (
    <section className="card">
      <h2 className="heading mb-4">{esPaciente ? 'Mis turnos' : 'Turnos'}</h2>

      {esMedico && (
        <div className="mb-4 rounded-lg bg-primary-800 text-white px-4 py-3">
          <p className="text-xs uppercase tracking-wide text-primary-100">Turnos para hoy</p>
          <p className="text-2xl font-semibold tabular-nums">{hoy}</p>
        </div>
      )}

      {esAdmin && (
        <form onSubmit={onFiltrar} className="flex flex-wrap items-end gap-3 mb-4">
          <div>
            <label className="label">Médico ID</label>
            <input type="number" className="input-field w-32" value={filtroMedicoId} onChange={e=>onFiltroMedicoIdChange(e.target.value)} />
          </div>
          <div>
            <label className="label">Paciente ID</label>
            <input type="number" className="input-field w-32" value={filtroPacienteId} onChange={e=>onFiltroPacienteIdChange(e.target.value)} />
          </div>
          <button type="submit" disabled={listLoading} className="btn-primary">
            {listLoading ? 'Buscando…' : 'Buscar'}
          </button>
          <button
            type="button"
            disabled={listLoading}
            onClick={onVerTodos}
            className="btn-secondary"
          >
            Ver todos
          </button>
        </form>
      )}

      <div className="overflow-x-auto rounded-lg border border-neutral-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-paper-100 text-left text-neutral-500">
              <th className="px-4 py-2 font-medium">ID</th>
              <th className="px-4 py-2 font-medium">Fecha y hora</th>
              <th className="px-4 py-2 font-medium">Especialidad</th>
              <th className="px-4 py-2 font-medium">Médico</th>
              <th className="px-4 py-2 font-medium">Paciente</th>
              <th className="px-4 py-2 font-medium">Preparación</th>
              <th className="px-4 py-2 font-medium">Estado</th>
              <th className="px-4 py-2 font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-100">
            {listLoading && turnos.length === 0 && <SkeletonRows columns={8} />}
            {turnos.map(t => (
              <React.Fragment key={t.id}>
                <tr className="hover:bg-paper-100/60">
                  <td className="px-4 py-2 text-neutral-500">{t.id}</td>
                  <td className="px-4 py-2 text-neutral-900">{t.fechaHora}</td>
                  <td className="px-4 py-2 text-neutral-900">{t.especialidad}</td>
                  <td className="px-4 py-2 text-neutral-900">
                    {t.medicoNombre ?? `#${t.medicoId}`}{t.medicoEspecialidad ? ` (${t.medicoEspecialidad})` : ''}
                  </td>
                  <td className="px-4 py-2 text-neutral-900">{t.pacienteNombre ?? `#${t.pacienteId}`}</td>
                  <td className="px-4 py-2 text-neutral-500">{t.preparacion || '—'}</td>
                  <td className="px-4 py-2"><EstadoBadge estado={t.estado} /></td>
                  <td className="px-4 py-2">
                    {puedeGestionarTurnos ? (
                      <div className="flex flex-wrap gap-2">
                        {t.estado === 'PENDIENTE' && (
                          <button
                            type="button"
                            disabled={estadoUpdatingId === t.id}
                            onClick={() => onCambiarEstado(t.id, 'CONFIRMADO')}
                            className="btn-primary !px-2 !py-1 text-xs"
                          >
                            Confirmar
                          </button>
                        )}
                        {t.estado !== 'CANCELADO' && (
                          <button
                            type="button"
                            disabled={estadoUpdatingId === t.id}
                            onClick={() => onCambiarEstado(t.id, 'CANCELADO')}
                            className="btn-secondary !px-2 !py-1 text-xs"
                          >
                            Cancelar
                          </button>
                        )}
                        {t.estado === 'CANCELADO' && !esMedico && (
                          <span className="text-neutral-400">—</span>
                        )}
                        {esMedico && (
                          <button
                            type="button"
                            onClick={() => onToggleHistoria(t)}
                            className="btn-secondary !px-2 !py-1 text-xs"
                          >
                            {historiaAbiertaId === t.id ? 'Ocultar historia' : 'Ver historia'}
                          </button>
                        )}
                      </div>
                    ) : esPaciente ? (
                      t.estado !== 'CANCELADO' ? (
                        <button
                          type="button"
                          disabled={estadoUpdatingId === t.id}
                          onClick={() => onIniciarCancelacionPaciente(t)}
                          className="btn-secondary !px-2 !py-1 text-xs text-danger-600"
                        >
                          Cancelar
                        </button>
                      ) : (
                        <span className="text-neutral-400">—</span>
                      )
                    ) : (
                      <span className="text-neutral-400">—</span>
                    )}
                  </td>
                </tr>
                {esMedico && historiaAbiertaId === t.id && (
                  <tr className="bg-paper-100/40">
                    <td colSpan={8} className="px-4 py-4">
                      <div className="space-y-3">
                        <h3 className="font-medium text-neutral-700">
                          Historia clínica de {t.pacienteNombre ?? `#${t.pacienteId}`}
                        </h3>
                        {historiaLoading ? (
                          <p className="text-sm text-neutral-400">Cargando…</p>
                        ) : (historiaPorPaciente[t.pacienteId]?.length ?? 0) === 0 ? (
                          <p className="text-sm text-neutral-400">Sin registros previos.</p>
                        ) : (
                          <ul className="space-y-2">
                            {historiaPorPaciente[t.pacienteId].map(r => (
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
                          <input
                            className="input-field"
                            placeholder="Diagnóstico"
                            value={diagnostico}
                            onChange={e => onDiagnosticoChange(e.target.value)}
                          />
                          <input
                            className="input-field"
                            placeholder="Tratamiento"
                            value={tratamientoRegistro}
                            onChange={e => onTratamientoChange(e.target.value)}
                          />
                          <input
                            className="input-field"
                            placeholder="Observaciones (opcional)"
                            value={observacionesRegistro}
                            onChange={e => onObservacionesChange(e.target.value)}
                          />
                          <button
                            type="button"
                            disabled={guardandoRegistro || !diagnostico || !tratamientoRegistro}
                            onClick={() => onAgregarRegistro(t)}
                            className="btn-primary !px-3 !py-1.5 text-xs"
                          >
                            {guardandoRegistro ? 'Guardando…' : 'Guardar registro'}
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
            {turnos.length === 0 && !listLoading && (
              <tr>
                <td colSpan={8} className="px-4 py-6 text-center text-neutral-400">
                  Sin turnos para mostrar.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      {totalPaginasTurnos > 1 && (
        <div className="flex items-center justify-between mt-4">
          <button
            type="button"
            disabled={listLoading || paginaTurnos === 0}
            onClick={() => onIrAPagina(paginaTurnos - 1)}
            className="btn-secondary !px-3 !py-1.5 text-xs"
          >
            ← Anterior
          </button>
          <span className="text-sm text-neutral-500">
            Página {paginaTurnos + 1} de {totalPaginasTurnos}
          </span>
          <button
            type="button"
            disabled={listLoading || paginaTurnos + 1 >= totalPaginasTurnos}
            onClick={() => onIrAPagina(paginaTurnos + 1)}
            className="btn-secondary !px-3 !py-1.5 text-xs"
          >
            Siguiente →
          </button>
        </div>
      )}
    </section>
  )
}
