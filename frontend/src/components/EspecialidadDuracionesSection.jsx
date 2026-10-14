import React, {useState} from 'react'
import SkeletonRows from './SkeletonRows.jsx'

function FilaDuracion({especialidad, duracionMinutos, guardando, onGuardar}){
  const [valor, setValor] = useState(String(duracionMinutos))

  return (
    <tr className="hover:bg-paper-100/60">
      <td className="px-4 py-2 text-neutral-900">{especialidad}</td>
      <td className="px-4 py-2">
        <input
          type="number"
          min="1"
          className="input-field !py-1 !w-24"
          value={valor}
          onChange={e => setValor(e.target.value)}
          aria-label={`Duración en minutos para ${especialidad}`}
        />
      </td>
      <td className="px-4 py-2">
        <button
          type="button"
          className="btn-secondary !px-2 !py-1 text-xs"
          disabled={guardando}
          onClick={() => onGuardar(especialidad, Number(valor))}
        >
          {guardando ? 'Guardando…' : 'Guardar'}
        </button>
      </td>
    </tr>
  )
}

export default function EspecialidadDuracionesSection({duraciones, duracionesLoading, guardandoEspecialidad, onGuardar}){
  return (
    <section className="card">
      <h2 className="heading mb-4">Duración de turnos por especialidad</h2>
      <p className="text-sm text-neutral-500 mb-4">
        Una especialidad sin duración configurada usa 30 minutos por defecto. Esto define cuándo
        dos turnos del mismo médico o paciente se consideran superpuestos.
      </p>
      <div className="overflow-x-auto rounded-lg border border-neutral-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-paper-100 text-left text-neutral-500">
              <th className="px-4 py-2 font-medium">Especialidad</th>
              <th className="px-4 py-2 font-medium">Duración (minutos)</th>
              <th className="px-4 py-2 font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-100">
            {duracionesLoading && duraciones.length === 0 ? (
              <SkeletonRows columns={3} />
            ) : (
              <>
                {duraciones.map(d => (
                  <FilaDuracion
                    key={d.especialidad}
                    especialidad={d.especialidad}
                    duracionMinutos={d.duracionMinutos}
                    guardando={guardandoEspecialidad === d.especialidad}
                    onGuardar={onGuardar}
                  />
                ))}
                {duraciones.length === 0 && (
                  <tr>
                    <td colSpan={3} className="px-4 py-6 text-center text-neutral-400">
                      No hay especialidades registradas todavía.
                    </td>
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
