import React, { useState } from 'react'
import { ROLES_ADMIN, USUARIOS_API } from '../config.js'
import { readErrorMessage } from '../utils.js'
import FloatingInput from './FloatingInput.jsx'

export default function UsuarioForm({notify, onGuardado}){
  const [nombre, setNombre] = useState('')
  const [email, setEmail] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [role, setRole] = useState('MEDICO')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const resp = await fetch(USUARIOS_API, {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, email, contrasena, role})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify(`Cuenta creada para ${email}.`, 'success')
      setNombre(''); setEmail(''); setContrasena(''); setRole('MEDICO')
      await onGuardado()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <FloatingInput label="Email" type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
      <FloatingInput label="Contraseña (mín. 6 caracteres)" type="password" value={contrasena} onChange={e=>setContrasena(e.target.value)} required />
      <select className="input-field" value={role} onChange={e=>setRole(e.target.value)}>
        {ROLES_ADMIN.map(r => <option key={r} value={r}>{r}</option>)}
      </select>
      <div className="sm:col-span-2">
        <button type="submit" disabled={loading} className="btn-primary sm:w-fit">
          {loading ? 'Creando cuenta…' : 'Crear cuenta'}
        </button>
      </div>
    </form>
  )
}
