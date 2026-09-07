import React from 'react'
import UsuarioForm from './UsuarioForm.jsx'

export default function UsuariosSection({usuarios, notify, onGuardado, onResetearClick}){
  return (
    <section className="card">
      <h2 className="heading mb-4">Usuarios</h2>
      <p className="text-sm text-neutral-500 mb-4">
        Creá una cuenta de acceso (login) para otro administrador, médico o paciente.
      </p>
      <UsuarioForm notify={notify} onGuardado={onGuardado} />
      <div className="overflow-x-auto rounded-lg border border-neutral-200 mt-4">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-paper-100 text-left text-neutral-500">
              <th className="px-4 py-2 font-medium">Nombre</th>
              <th className="px-4 py-2 font-medium">Email</th>
              <th className="px-4 py-2 font-medium">Rol</th>
              <th className="px-4 py-2 font-medium">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-100">
            {usuarios.map(u => (
              <tr key={u.id} className="hover:bg-paper-100/60">
                <td className="px-4 py-2 text-neutral-900">{u.nombre}</td>
                <td className="px-4 py-2 text-neutral-900">{u.email}</td>
                <td className="px-4 py-2 text-neutral-900">{u.role}</td>
                <td className="px-4 py-2">
                  <button type="button" onClick={() => onResetearClick(u)} className="btn-secondary !px-2 !py-1 text-xs">
                    Resetear contraseña
                  </button>
                </td>
              </tr>
            ))}
            {usuarios.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-neutral-400">
                  Sin cuentas registradas.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
