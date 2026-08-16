import React, {useEffect, useState} from 'react'

const TURNOS_API = 'http://localhost:8080/api/turnos'
const MEDICOS_API = 'http://localhost:8080/api/medicos'
const PACIENTES_API = 'http://localhost:8080/api/pacientes'
const AUTH_API = 'http://localhost:8080/api/auth'
const AUTH_STORAGE_KEY = 'medconnect_auth'

const ESTADO_BADGE = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  CANCELADO: 'badge-cancelado',
}

const ROLES = ['ADMINISTRADOR', 'MEDICO', 'PACIENTE']

// Los mensajes nativos de validación del navegador ("Please fill out this
// field") vienen en el idioma del navegador, no de la página. Los pisamos
// con el texto en español vía la Constraint Validation API.
function handleInvalid(e){
  const el = e.target
  if(el.validity.valueMissing){
    el.setCustomValidity(el.tagName === 'SELECT' ? 'Seleccioná una opción' : 'Completá este campo')
  }else if(el.validity.typeMismatch && el.type === 'email'){
    el.setCustomValidity('Ingresá un email válido')
  }else{
    el.setCustomValidity('')
  }
}

function clearValidity(e){
  e.target.setCustomValidity('')
}

// El backend devuelve los errores como texto plano (no JSON), así que
// resp.json() falla en silencio y perdemos el mensaje real. Leemos el body
// como texto siempre y probamos parsearlo como JSON por si acaso.
async function readErrorMessage(resp){
  const text = await resp.text().catch(() => '')
  try{
    const json = JSON.parse(text)
    return typeof json === 'string' ? json : (json?.message ?? `HTTP ${resp.status}`)
  }catch{
    return text || `HTTP ${resp.status}`
  }
}

function EstadoBadge({estado}){
  return <span className={ESTADO_BADGE[estado] ?? 'badge bg-neutral-100 text-neutral-600'}>{estado}</span>
}

function ToastContainer({toasts}){
  if(toasts.length === 0) return null
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-full max-w-sm px-4 sm:px-0">
      {toasts.map(t => (
        <div
          key={t.id}
          className={`toast ${t.type === 'success' ? 'toast-success' : 'toast-error'} ${t.leaving ? 'toast-leaving' : ''}`}
        >
          {t.message}
        </div>
      ))}
    </div>
  )
}

function SkeletonRows({columns, rows = 3}){
  return Array.from({length: rows}).map((_, i) => (
    <tr key={i}>
      {Array.from({length: columns}).map((_, j) => (
        <td key={j} className="px-4 py-3">
          <div className="skeleton" style={{width: `${60 + (j * 13) % 35}%`}} />
        </td>
      ))}
    </tr>
  ))
}

function FloatingInput({label, type = 'text', value, onChange, required = false, className = ''}){
  const [focused, setFocused] = useState(false)
  const floated = focused || String(value ?? '').length > 0

  return (
    <label className={`relative block ${className}`}>
      <input
        type={type}
        value={value}
        onChange={e => { clearValidity(e); onChange(e) }}
        onInvalid={handleInvalid}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        required={required}
        className="input-field"
      />
      <span
        className={`pointer-events-none absolute left-3 transition-all duration-150 ${
          floated
            ? 'top-0 -translate-y-1/2 px-1 bg-paper-50 text-xs text-primary-700'
            : 'top-1/2 -translate-y-1/2 text-sm text-neutral-400'
        }`}
      >
        {label}
      </span>
    </label>
  )
}

function LoginScreen({onLoginExitoso, notify}){
  const [modo, setModo] = useState('login')
  const [email, setEmail] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [nombre, setNombre] = useState('')
  const [role, setRole] = useState('PACIENTE')
  const [loading, setLoading] = useState(false)

  async function iniciarSesion(emailParam, contrasenaParam){
    const resp = await fetch(`${AUTH_API}/login`, {
      method: 'POST',
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

function MedicoForm({medico, token, onGuardado, onCancelarEdicion, notify}){
  const isEditing = !!medico
  const [nombre, setNombre] = useState(medico?.nombre ?? '')
  const [especialidad, setEspecialidad] = useState(medico?.especialidad ?? '')
  const [matricula, setMatricula] = useState(medico?.matricula ?? '')
  const [telefono, setTelefono] = useState(medico?.telefono ?? '')
  const [direccion, setDireccion] = useState(medico?.direccion ?? '')
  const [email, setEmail] = useState(medico?.email ?? '')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = isEditing ? `${MEDICOS_API}/${medico.id}` : MEDICOS_API
      const resp = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: {'Content-Type':'application/json', 'Authorization': `Bearer ${token}`},
        body: JSON.stringify({nombre, especialidad, matricula, telefono, direccion, email})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      if(!isEditing){ setNombre(''); setEspecialidad(''); setMatricula(''); setTelefono(''); setDireccion(''); setEmail('') }
      notify(isEditing ? 'Cambios guardados.' : 'Médico agregado.', 'success')
      await onGuardado()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
      <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <FloatingInput label="Especialidad" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} required />
      <FloatingInput label="Matrícula" value={matricula} onChange={e=>setMatricula(e.target.value)} required />
      <FloatingInput label="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <FloatingInput className="sm:col-span-2" label="Dirección" value={direccion} onChange={e=>setDireccion(e.target.value)} />
      <FloatingInput className="sm:col-span-2" label="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <div className="sm:col-span-2 flex gap-3">
        <button type="submit" disabled={loading} className="btn-primary sm:w-fit">
          {loading ? 'Guardando…' : isEditing ? 'Guardar cambios' : 'Agregar médico'}
        </button>
        {isEditing && (
          <button type="button" onClick={onCancelarEdicion} className="btn-secondary sm:w-fit">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

function PacienteForm({paciente, token, onGuardado, onCancelarEdicion, notify}){
  const isEditing = !!paciente
  const [nombre, setNombre] = useState(paciente?.nombre ?? '')
  const [dni, setDni] = useState(paciente?.dni ?? '')
  const [telefono, setTelefono] = useState(paciente?.telefono ?? '')
  const [direccion, setDireccion] = useState(paciente?.direccion ?? '')
  const [obraSocial, setObraSocial] = useState(paciente?.obraSocial ?? '')
  const [numeroAfiliado, setNumeroAfiliado] = useState(paciente?.numeroAfiliado ?? '')
  const [plan, setPlan] = useState(paciente?.plan ?? '')
  const [email, setEmail] = useState(paciente?.email ?? '')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = isEditing ? `${PACIENTES_API}/${paciente.id}` : PACIENTES_API
      const resp = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: {'Content-Type':'application/json', 'Authorization': `Bearer ${token}`},
        body: JSON.stringify({nombre, dni, telefono, direccion, obraSocial, numeroAfiliado, plan, email})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      if(!isEditing){ setNombre(''); setDni(''); setTelefono(''); setDireccion(''); setObraSocial(''); setNumeroAfiliado(''); setPlan(''); setEmail('') }
      notify(isEditing ? 'Cambios guardados.' : 'Paciente agregado.', 'success')
      await onGuardado()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
      <FloatingInput label="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <FloatingInput label="DNI" value={dni} onChange={e=>setDni(e.target.value)} required />
      <FloatingInput label="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <FloatingInput label="Dirección" value={direccion} onChange={e=>setDireccion(e.target.value)} />
      <FloatingInput label="Obra social / prepaga" value={obraSocial} onChange={e=>setObraSocial(e.target.value)} />
      <FloatingInput label="Número de afiliado" value={numeroAfiliado} onChange={e=>setNumeroAfiliado(e.target.value)} />
      <FloatingInput label="Plan" value={plan} onChange={e=>setPlan(e.target.value)} />
      <FloatingInput className="sm:col-span-2" label="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      <div className="sm:col-span-2 flex gap-3">
        <button type="submit" disabled={loading} className="btn-primary sm:w-fit">
          {loading ? 'Guardando…' : isEditing ? 'Guardar cambios' : 'Agregar paciente'}
        </button>
        {isEditing && (
          <button type="button" onClick={onCancelarEdicion} className="btn-secondary sm:w-fit">
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}

export default function App(){
  // Efecto ripple estilo Material: un solo listener global cubre todos los
  // botones .btn (actuales y futuros), sin tener que instrumentar cada uno.
  useEffect(() => {
    function handleRipple(e){
      const btn = e.target.closest('.btn-primary, .btn-secondary')
      if(!btn || btn.disabled) return
      const rect = btn.getBoundingClientRect()
      const size = Math.max(rect.width, rect.height)
      const span = document.createElement('span')
      span.className = 'ripple'
      span.style.width = span.style.height = `${size}px`
      span.style.left = `${e.clientX - rect.left - size / 2}px`
      span.style.top = `${e.clientY - rect.top - size / 2}px`
      btn.appendChild(span)
      span.addEventListener('animationend', () => span.remove())
    }
    document.addEventListener('mousedown', handleRipple)
    return () => document.removeEventListener('mousedown', handleRipple)
  }, [])

  const [toasts, setToasts] = useState([])

  function notify(message, type = 'error'){
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, {id, message, type, leaving: false}])
    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === id ? {...t, leaving: true} : t))
      setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 200)
    }, 3500)
  }

  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  })
  const token = auth?.token

  function handleLoginExitoso(data){
    setAuth(data)
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data))
  }

  function handleLogout(){
    setAuth(null)
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  async function apiFetch(url, options = {}){
    const resp = await fetch(url, {
      ...options,
      headers: {...(options.headers || {}), Authorization: `Bearer ${token}`}
    })
    if(resp.status === 401 || resp.status === 403){
      handleLogout()
      throw new Error('Sesión expirada, iniciá sesión de nuevo')
    }
    return resp
  }

  const [medicos, setMedicos] = useState([])
  const [pacientes, setPacientes] = useState([])
  const [medicosLoading, setMedicosLoading] = useState(false)
  const [pacientesLoading, setPacientesLoading] = useState(false)
  const [editingMedico, setEditingMedico] = useState(null)
  const [editingPaciente, setEditingPaciente] = useState(null)

  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('Cardiología')
  const [medicoId, setMedicoId] = useState('')
  const [pacienteId, setPacienteId] = useState('')
  const [loading, setLoading] = useState(false)

  const [turnos, setTurnos] = useState([])
  const [filtroMedicoId, setFiltroMedicoId] = useState('')
  const [filtroPacienteId, setFiltroPacienteId] = useState('')
  const [listLoading, setListLoading] = useState(false)
  const [estadoUpdatingId, setEstadoUpdatingId] = useState(null)

  async function cargarMedicos(){
    setMedicosLoading(true)
    try{
      const resp = await apiFetch(MEDICOS_API)
      if(resp.ok) setMedicos(await resp.json())
    }finally{
      setMedicosLoading(false)
    }
  }

  async function cargarPacientes(){
    setPacientesLoading(true)
    try{
      const resp = await apiFetch(PACIENTES_API)
      if(resp.ok) setPacientes(await resp.json())
    }finally{
      setPacientesLoading(false)
    }
  }

  async function eliminarMedico(medico){
    if(!window.confirm(`¿Eliminar a ${medico.nombre}?`)) return
    try{
      const resp = await apiFetch(`${MEDICOS_API}/${medico.id}`, {method: 'DELETE'})
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      if(editingMedico?.id === medico.id) setEditingMedico(null)
      notify('Médico eliminado.', 'success')
      await cargarMedicos()
    }catch(err){
      notify(err.message)
    }
  }

  async function eliminarPaciente(paciente){
    if(!window.confirm(`¿Eliminar a ${paciente.nombre}?`)) return
    try{
      const resp = await apiFetch(`${PACIENTES_API}/${paciente.id}`, {method: 'DELETE'})
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      if(editingPaciente?.id === paciente.id) setEditingPaciente(null)
      notify('Paciente eliminado.', 'success')
      await cargarPacientes()
    }catch(err){
      notify(err.message)
    }
  }

  async function cargarTurnos(medicoIdParam = filtroMedicoId, pacienteIdParam = filtroPacienteId){
    setListLoading(true)
    try{
      const params = new URLSearchParams()
      if(medicoIdParam) params.set('medicoId', medicoIdParam)
      if(pacienteIdParam) params.set('pacienteId', pacienteIdParam)
      const url = params.toString() ? `${TURNOS_API}?${params}` : TURNOS_API
      const resp = await apiFetch(url)
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      setTurnos(await resp.json())
    }catch(err){
      notify(err.message)
    }finally{
      setListLoading(false)
    }
  }

  useEffect(() => {
    if(token){ cargarMedicos(); cargarPacientes(); cargarTurnos() }
  }, [token])

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const resp = await apiFetch(TURNOS_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          fechaHora, especialidad, medicoId: Number(medicoId), pacienteId: Number(pacienteId)
        })
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      const data = await resp.json()
      notify(`Turno creado con id ${data?.id}.`, 'success')
      setMedicoId('')
      setPacienteId('')
      await cargarTurnos()
    }catch(err){
      notify(err.message)
    }finally{setLoading(false)}
  }

  function handleFiltrar(e){
    e.preventDefault()
    cargarTurnos()
  }

  async function cambiarEstado(id, nuevoEstado){
    setEstadoUpdatingId(id)
    try{
      const resp = await apiFetch(`${TURNOS_API}/${id}/estado`, {
        method: 'PATCH',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({estado: nuevoEstado})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify(nuevoEstado === 'CONFIRMADO' ? 'Turno confirmado.' : 'Turno cancelado.', 'success')
      await cargarTurnos()
    }catch(err){
      notify(err.message)
    }finally{
      setEstadoUpdatingId(null)
    }
  }

  function nombreMedico(id){
    const medico = medicos.find(m => m.id === id)
    return medico ? medico.nombre : `#${id}`
  }

  function nombrePaciente(id){
    const paciente = pacientes.find(p => p.id === id)
    return paciente ? paciente.nombre : `#${id}`
  }

  if(!auth){
    return (
      <>
        <LoginScreen onLoginExitoso={handleLoginExitoso} notify={notify} />
        <ToastContainer toasts={toasts} />
      </>
    )
  }

  return (
    <div className="min-h-screen bg-neutral-200 font-sans">
      <ToastContainer toasts={toasts} />
      <header className="bg-primary-800 text-white">
        <div className="max-w-3xl mx-auto px-6 py-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold tracking-tight">MedConnect</h1>
            <p className="text-sm text-primary-100">Gestión de turnos</p>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-primary-100">{auth.nombre} · {auth.role}</span>
            <button type="button" onClick={handleLogout} className="btn-secondary !px-3 !py-1.5 text-xs">
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-8 space-y-8">
        <section className="card">
          <h2 className="heading mb-4">Médicos</h2>
          <MedicoForm
            key={editingMedico?.id ?? 'new'}
            medico={editingMedico}
            token={token}
            notify={notify}
            onGuardado={async () => { setEditingMedico(null); await cargarMedicos() }}
            onCancelarEdicion={() => setEditingMedico(null)}
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
                            <button type="button" onClick={() => setEditingMedico(m)} className="btn-secondary !px-2 !py-1 text-xs">
                              Editar
                            </button>
                            <button type="button" onClick={() => eliminarMedico(m)} className="btn-secondary !px-2 !py-1 text-xs text-danger-600">
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
        </section>

        <section className="card">
          <h2 className="heading mb-4">Pacientes</h2>
          <PacienteForm
            key={editingPaciente?.id ?? 'new'}
            paciente={editingPaciente}
            token={token}
            notify={notify}
            onGuardado={async () => { setEditingPaciente(null); await cargarPacientes() }}
            onCancelarEdicion={() => setEditingPaciente(null)}
          />
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
                          <div className="flex gap-2">
                            <button type="button" onClick={() => setEditingPaciente(p)} className="btn-secondary !px-2 !py-1 text-xs">
                              Editar
                            </button>
                            <button type="button" onClick={() => eliminarPaciente(p)} className="btn-secondary !px-2 !py-1 text-xs text-danger-600">
                              Eliminar
                            </button>
                          </div>
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
        </section>

        <section className="card">
          <h2 className="heading mb-4">Crear turno</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Fecha y hora</label>
              <input className="input-field" value={fechaHora} onChange={e=>setFechaHora(e.target.value)} />
            </div>
            <div>
              <label className="label">Especialidad</label>
              <input className="input-field" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Médico</label>
                <select className="input-field" value={medicoId} onChange={e=>{clearValidity(e); setMedicoId(e.target.value)}} onInvalid={handleInvalid} required>
                  <option value="" disabled>Seleccionar médico</option>
                  {medicos.map(m => (
                    <option key={m.id} value={m.id}>{m.nombre} — {m.especialidad}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Paciente</label>
                <select className="input-field" value={pacienteId} onChange={e=>{clearValidity(e); setPacienteId(e.target.value)}} onInvalid={handleInvalid} required>
                  <option value="" disabled>Seleccionar paciente</option>
                  {pacientes.map(p => (
                    <option key={p.id} value={p.id}>{p.nombre} — DNI {p.dni}</option>
                  ))}
                </select>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Enviando…' : 'Crear turno'}
            </button>
          </form>
        </section>

        <section className="card">
          <h2 className="heading mb-4">Turnos</h2>
          <form onSubmit={handleFiltrar} className="flex flex-wrap items-end gap-3 mb-4">
            <div>
              <label className="label">Médico ID</label>
              <input type="number" className="input-field w-32" value={filtroMedicoId} onChange={e=>setFiltroMedicoId(e.target.value)} />
            </div>
            <div>
              <label className="label">Paciente ID</label>
              <input type="number" className="input-field w-32" value={filtroPacienteId} onChange={e=>setFiltroPacienteId(e.target.value)} />
            </div>
            <button type="submit" disabled={listLoading} className="btn-primary">
              {listLoading ? 'Buscando…' : 'Buscar'}
            </button>
            <button
              type="button"
              disabled={listLoading}
              onClick={() => { setFiltroMedicoId(''); setFiltroPacienteId(''); cargarTurnos('', '') }}
              className="btn-secondary"
            >
              Ver todos
            </button>
          </form>

          <div className="overflow-x-auto rounded-lg border border-neutral-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-paper-100 text-left text-neutral-500">
                  <th className="px-4 py-2 font-medium">ID</th>
                  <th className="px-4 py-2 font-medium">Fecha y hora</th>
                  <th className="px-4 py-2 font-medium">Especialidad</th>
                  <th className="px-4 py-2 font-medium">Médico</th>
                  <th className="px-4 py-2 font-medium">Paciente</th>
                  <th className="px-4 py-2 font-medium">Estado</th>
                  <th className="px-4 py-2 font-medium">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {listLoading && turnos.length === 0 && <SkeletonRows columns={7} />}
                {turnos.map(t => (
                  <tr key={t.id} className="hover:bg-paper-100/60">
                    <td className="px-4 py-2 text-neutral-500">{t.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{t.fechaHora}</td>
                    <td className="px-4 py-2 text-neutral-900">{t.especialidad}</td>
                    <td className="px-4 py-2 text-neutral-900">{nombreMedico(t.medicoId)}</td>
                    <td className="px-4 py-2 text-neutral-900">{nombrePaciente(t.pacienteId)}</td>
                    <td className="px-4 py-2"><EstadoBadge estado={t.estado} /></td>
                    <td className="px-4 py-2">
                      <div className="flex gap-2">
                        {t.estado === 'PENDIENTE' && (
                          <button
                            type="button"
                            disabled={estadoUpdatingId === t.id}
                            onClick={() => cambiarEstado(t.id, 'CONFIRMADO')}
                            className="btn-primary !px-2 !py-1 text-xs"
                          >
                            Confirmar
                          </button>
                        )}
                        {t.estado !== 'CANCELADO' && (
                          <button
                            type="button"
                            disabled={estadoUpdatingId === t.id}
                            onClick={() => cambiarEstado(t.id, 'CANCELADO')}
                            className="btn-secondary !px-2 !py-1 text-xs"
                          >
                            Cancelar
                          </button>
                        )}
                        {t.estado === 'CANCELADO' && (
                          <span className="text-neutral-400">—</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {turnos.length === 0 && !listLoading && (
                  <tr>
                    <td colSpan={7} className="px-4 py-6 text-center text-neutral-400">
                      Sin turnos para mostrar.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  )
}
