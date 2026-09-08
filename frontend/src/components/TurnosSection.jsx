import React from 'react'
import EstadoBadge from './EstadoBadge.jsx'
import HistoriaClinicaPanel from './HistoriaClinicaPanel.jsx'
import Paginacion from './Paginacion.jsx'
import SkeletonRows from './SkeletonRows.jsx'
import TurnoFiltros from './TurnoFiltros.jsx'

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
        <TurnoFiltros
          filtroMedicoId={filtroMedicoId}
          onFiltroMedicoIdChange={onFiltroMedicoIdChange}
          filtroPacienteId={filtroPacienteId}
          onFiltroPacienteIdChange={onFiltroPacienteIdChange}
          onFiltrar={onFiltrar}
          onVerTodos={onVerTodos}
          loading={listLoading}
        />
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
                            aria-expanded={historiaAbiertaId === t.id}
                            aria-controls={`historia-turno-${t.id}`}
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
                  <HistoriaClinicaPanel
                    turno={t}
                    historiaPorPaciente={historiaPorPaciente}
                    historiaLoading={historiaLoading}
                    diagnostico={diagnostico}
                    onDiagnosticoChange={onDiagnosticoChange}
                    tratamientoRegistro={tratamientoRegistro}
                    onTratamientoChange={onTratamientoChange}
                    observacionesRegistro={observacionesRegistro}
                    onObservacionesChange={onObservacionesChange}
                    guardandoRegistro={guardandoRegistro}
                    onAgregarRegistro={onAgregarRegistro}
                  />
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
      <Paginacion pagina={paginaTurnos} totalPaginas={totalPaginasTurnos} loading={listLoading} onIrAPagina={onIrAPagina} />
    </section>
  )
}
