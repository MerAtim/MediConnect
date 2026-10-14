import React from 'react'
import TurnoCamposFormulario from './TurnoCamposFormulario.jsx'

export default function ReservarTurnoSection({
  fechaHora, onFechaHoraChange, especialidad, onEspecialidadChange, especialidades,
  medicoId, onMedicoIdChange, medicosPorEspecialidad, preparacion, onPreparacionChange,
  loading, onSubmit
}){
  return (
    <section className="card">
      <h2 className="heading mb-4">Reservar turno</h2>
      <TurnoCamposFormulario
        idPrefix="reservar-turno"
        fechaHora={fechaHora} onFechaHoraChange={onFechaHoraChange}
        especialidad={especialidad} onEspecialidadChange={onEspecialidadChange} especialidades={especialidades}
        medicoId={medicoId} onMedicoIdChange={onMedicoIdChange} medicosPorEspecialidad={medicosPorEspecialidad}
        preparacion={preparacion} onPreparacionChange={onPreparacionChange}
        loading={loading} onSubmit={onSubmit}
        submitLabel="Reservar turno" submitLabelEnviando="Reservando"
      />
    </section>
  )
}
