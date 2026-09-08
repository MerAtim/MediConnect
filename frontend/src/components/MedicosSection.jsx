import React from 'react'
import MedicoForm from './MedicoForm.jsx'
import Paginacion from './Paginacion.jsx'
import SkeletonRows from './SkeletonRows.jsx'

export default function MedicosSection({
  medicos, medicosLoading, editingMedico, onEditar, onCancelarEdicion, onGuardado, onEliminar,
  notify, cuentasDisponibles, paginaMedicos, totalPaginasMedicos, onIrAPagina
}){
  return (
    <section className="card">
      <h2 className="heading mb-4">Médicos</h2>
      <MedicoForm
        key={editingMedico?.id ?? 'new'}
        medico={editingMedico}
        notify={notify}
        onGuardado={onGuardado}
        onCancelarEdicion={onCancelarEdicion}
        cuentasDisponibles={cuentasDisponibles}
      />
      <div className="overflow-x-auto rounded-lg border border-neutral-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-paper-100 text-left text-neutral-500">
              <th className="px-4 py-2 font-medium">ID</th>
              <th className="px-4 py-2 font-medium">Nombre</th>
              <th className="px-4 py-2 font-medium">Especialidad</th>
              <th className="px-4 py-2 font-medium">Matrícula</th>
              <th className="px-4 py-2 font-medium">Dirección</th>
              <th className="px-4 py-2 font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-100">
            {medicosLoading && medicos.length === 0 ? (
              <SkeletonRows columns={6} />
            ) : (
              <>
                {medicos.map(m => (
                  <tr key={m.id} className="hover:bg-paper-100/60">
                    <td className="px-4 py-2 text-neutral-500">{m.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.nombre}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.especialidad}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.matricula}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.direccion}</td>
                    <td className="px-4 py-2">
                      <div className="flex gap-2">
                        <button type="button" onClick={() => onEditar(m)} className="btn-secondary !px-2 !py-1 text-xs">
                          Editar
                        </button>
                        <button type="button" onClick={() => onEliminar(m)} className="btn-secondary !px-2 !py-1 text-xs text-danger-600">
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {medicos.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-6 text-center text-neutral-400">
                      Sin médicos registrados.
                    </td>
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
      </div>
      <Paginacion pagina={paginaMedicos} totalPaginas={totalPaginasMedicos} loading={medicosLoading} onIrAPagina={onIrAPagina} />
    </section>
  )
}
