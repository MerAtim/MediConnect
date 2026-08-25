import React, { useState } from 'react'
import { AUTH_API, ROLES } from '../config.js'
import { readErrorMessage } from '../utils.js'
import FloatingInput from './FloatingInput.jsx'

export default function LoginScreen({onLoginExitoso, notify}){
  const [modo, setModo] = useState('login')
  const [email, setEmail] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [nombre, setNombre] = useState('')
  const [role, setRole] = useState('PACIENTE')
  const [loading, setLoading] = useState(false)

  async function iniciarSesion(emailParam, contrasenaParam){
    const resp = await fetch(`${AUTH_API}/login`, {
      method: 'POST',
      credentials: 'include',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({email: emailParam, contrasena: contrasenaParam})
    })
    if(!resp.ok) throw new Error(await readErrorMessage(resp))
    onLoginExitoso(await resp.json())
  }

  async function handleLogin(e){
    e.preventDefault()
    setLoading(true)
    try{
      await iniciarSesion(email, contrasena)
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  async function handleRegistro(e){
    e.preventDefault()
    setLoading(true)
    try{
      const resp = await fetch(`${AUTH_API}/registro`, {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, email, contrasena, role})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify('Cuenta creada.', 'success')
      await iniciarSesion(email, contrasena)
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-neutral-200 font-sans flex items-center justify-center px-6">
      <div className="card w-full max-w-sm">
        <h1 className="text-xl font-semibold tracking-tight text-neutral-900 mb-1">MedConnect</h1>
        <p className="text-sm text-neutral-500 mb-6">{modo === 'login' ? 'Iniciá sesión para continuar' : 'Crear una cuenta nueva'}</p>

        {modo === 'login' ? (
          <form onSubmit={handleLogin} className="space-y-4">
            <FloatingInput label="Email" type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
            <FloatingInput label="Contraseña" type="password" value={contrasena} onChange={e=>setContrasena(e.target.value)} required />
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? 'Ingresando…' : 'Ingresar'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegistro} className="space-y-4">
            <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
            <FloatingInput label="Email" type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
            <FloatingInput label="Contraseña (mín. 6 caracteres)" type="password" value={contrasena} onChange={e=>setContrasena(e.target.value)} required />
            <select className="input-field" value={role} onChange={e=>setRole(e.target.value)}>
              {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? 'Creando cuenta…' : 'Crear cuenta'}
            </button>
          </form>
        )}

        <button
          type="button"
          onClick={() => setModo(modo === 'login' ? 'registro' : 'login')}
          className="mt-4 text-sm text-primary-700 hover:underline w-full text-center"
        >
          {modo === 'login' ? '¿No tenés cuenta? Registrate' : '¿Ya tenés cuenta? Iniciá sesión'}
        </button>
      </div>
    </div>
  )
}
