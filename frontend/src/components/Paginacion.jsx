import React from 'react'

// MEDIUM de la re-auditoria e2e (2026-09-08): "duplicacion entre
// useMedicos/usePacientes y entre los 3 formularios" -- este bloque estaba
// repetido palabra por palabra en MedicosSection, PacientesSection y
// TurnosSection, solo cambiando los nombres de las variables.
export default function Paginacion({pagina, totalPaginas, loading, onIrAPagina}){
  if(totalPaginas <= 1) return null
  return (
    <div className="flex items-center justify-between mt-4">
      <button
        type="button"
        disabled={loading || pagina === 0}
        onClick={() => onIrAPagina(pagina - 1)}
        className="btn-secondary !px-3 !py-1.5 text-xs"
      >
        ← Anterior
      </button>
      <span className="text-sm text-neutral-500">
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button
        type="button"
        disabled={loading || pagina + 1 >= totalPaginas}
        onClick={() => onIrAPagina(pagina + 1)}
        className="btn-secondary !px-3 !py-1.5 text-xs"
      >
        Siguiente →
      </button>
    </div>
  )
}
