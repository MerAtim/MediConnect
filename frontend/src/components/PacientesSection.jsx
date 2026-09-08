import React from 'react'
import PacienteForm from './PacienteForm.jsx'
import Paginacion from './Paginacion.jsx'
import SkeletonRows from './SkeletonRows.jsx'

export default function PacientesSection({
  esAdmin, pacientes, pacientesLoading, editingPaciente, onEditar, onCancelarEdicion, onGuardado,
  onEliminar, onDescargarHistoria, notify, cuentasDisponibles, paginaPacientes, totalPaginasPacientes, onIrAPagina
}){
  return (
    <section className="card">
      <h2 className="heading mb-4">{esAdmin ? 'Pacientes' : 'Mis pacientes'}</h2>
      {esAdmin && (
        <PacienteForm
          key={editingPaciente?.id ?? 'new'}
          paciente={editingPaciente}
          notify={notify}
          onGuardado={onGuardado}
          onCancelarEdicion={onCancelarEdicion}
          cuentasDisponibles={cuentasDisponibles}
        />
      )}
      <div className="overflow-x-auto rounded-lg border border-neutral-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-paper-100 text-left text-neutral-500">
              <th className="px-4 py-2 font-medium">ID</th>
              <th className="px-4 py-2 font-medium">Nombre</th>
              <th className="px-4 py-2 font-medium">DNI</th>
              <th className="px-4 py-2 font-medium">Dirección</th>
              <th className="px-4 py-2 font-medium">Obra social</th>
              <th className="px-4 py-2 font-medium">N° afiliado</th>
              <th className="px-4 py-2 font-medium">Plan</th>
              <th className="px-4 py-2 font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-100">
            {pacientesLoading && pacientes.length === 0 ? (
              <SkeletonRows columns={8} />
            ) : (
              <>
                {pacientes.map(p => (
                  <tr key={p.id} className="hover:bg-paper-100/60">
                    <td className="px-4 py-2 text-neutral-500">{p.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.nombre}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.dni}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.direccion}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.obraSocial}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.numeroAfiliado}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.plan}</td>
                    <td className="px-4 py-2">
                      {esAdmin ? (
                        <div className="flex gap-2">
                          <button type="button" onClick={() => onEditar(p)} className="btn-secondary !px-2 !py-1 text-xs">
                            Editar
                          </button>
                          <button type="button" onClick={() => onEliminar(p)} className="btn-secondary !px-2 !py-1 text-xs text-danger-600">
                            Eliminar
                          </button>
                          <button type="button" onClick={() => onDescargarHistoria(p.id)} className="btn-secondary !px-2 !py-1 text-xs">
                            Descargar historia
                          </button>
                        </div>
                      ) : (
                        <span className="text-neutral-400">—</span>
                      )}
                    </td>
                  </tr>
                ))}
                {pacientes.length === 0 && (
                  <tr>
                    <td colSpan={8} className="px-4 py-6 text-center text-neutral-400">
                      Sin pacientes registrados.
                    </td>
                  </tr>
                )}
              </>
            )}
          </tbody>
        </table>
      </div>
      <Paginacion pagina={paginaPacientes} totalPaginas={totalPaginasPacientes} loading={pacientesLoading} onIrAPagina={onIrAPagina} />
    </section>
  )
}
