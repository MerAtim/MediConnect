import React, {useEffect, useState} from 'react'

// En build de produccion (Docker/CI) se fija con la env var VITE_API_URL al
// correr `npm run build` (Vite la incrusta en el bundle en build-time, no en
// runtime). En desarrollo local, sin esa env var, cae a localhost:8080.
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const TURNOS_API = `${API_BASE}/api/turnos`
const MEDICOS_API = `${API_BASE}/api/medicos`
const PACIENTES_API = `${API_BASE}/api/pacientes`
const HISTORIAS_API = `${API_BASE}/api/historias-clinicas`
const AUTH_API = `${API_BASE}/api/auth`
const USUARIOS_API = `${API_BASE}/api/usuarios`
const AUTH_STORAGE_KEY = 'medconnect_auth'

const ESTADO_BADGE = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  CANCELADO: 'badge-cancelado',
}

const ROLES = ['MEDICO', 'PACIENTE']
const ROLES_ADMIN = ['ADMINISTRADOR', 'MEDICO', 'PACIENTE']

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

function ConfirmModal({open, title, message, confirmLabel, cancelLabel = 'Volver', onConfirm, onCancel, danger = false}){
  if(!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4">
      <div className="card max-w-sm w-full">
        <h3 className="heading mb-2">{title}</h3>
        <p className="text-sm text-neutral-600 mb-6">{message}</p>
        <div className="flex justify-end gap-3">
          <button type="button" onClick={onCancel} className="btn-secondary">{cancelLabel}</button>
          <button type="button" onClick={onConfirm} className={danger ? 'btn-danger' : 'btn-primary'}>{confirmLabel}</button>
        </div>
      </div>
    </div>
  )
}

// modo 'propia': el usuario logueado cambia su contraseña sabiendo la
// actual (PATCH /me/contrasena). modo 'reset': un ADMINISTRADOR resetea la
// de otro usuario sin necesitar la vieja (PATCH /{id}/contrasena) — es el
// mecanismo de recuperación, ya que el proyecto no manda emails.
function CambiarContrasenaModal({open, modo, usuarioObjetivo, notify, onClose, onExito}){
  const [contrasenaActual, setContrasenaActual] = useState('')
  const [contrasenaNueva, setContrasenaNueva] = useState('')
  const [loading, setLoading] = useState(false)

  if(!open) return null

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const url = modo === 'propia' ? `${USUARIOS_API}/me/contrasena` : `${USUARIOS_API}/${usuarioObjetivo.id}/contrasena`
      const body = modo === 'propia' ? {contrasenaActual, contrasenaNueva} : {contrasenaNueva}
      const resp = await fetch(url, {
        method: 'PATCH',
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(body)
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify(modo === 'propia' ? 'Contraseña actualizada.' : `Contraseña reseteada para ${usuarioObjetivo.email}.`, 'success')
      setContrasenaActual(''); setContrasenaNueva('')
      onExito?.()
      onClose()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4">
      <div className="card max-w-sm w-full">
        <h3 className="heading mb-4">
          {modo === 'propia' ? 'Cambiar mi contraseña' : `Resetear contraseña de ${usuarioObjetivo?.nombre}`}
        </h3>
        <form onSubmit={handleSubmit} className="space-y-4">
          {modo === 'propia' && (
            <FloatingInput label="Contraseña actual" type="password" value={contrasenaActual} onChange={e=>setContrasenaActual(e.target.value)} required />
          )}
          <FloatingInput label="Contraseña nueva (mín. 6 caracteres)" type="password" value={contrasenaNueva} onChange={e=>setContrasenaNueva(e.target.value)} required />
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancelar</button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
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

function MedicoForm({medico, onGuardado, onCancelarEdicion, notify, cuentasDisponibles}){
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
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
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
      <label className="sm:col-span-2 block">
        <span className="label">Cuenta de acceso vinculada</span>
        <select className="input-field" value={email} onChange={e=>setEmail(e.target.value)}>
          <option value="">Sin vincular</option>
          {email && !cuentasDisponibles.some(c => c.email === email) && (
            <option value={email}>{email} (cuenta no encontrada)</option>
          )}
          {cuentasDisponibles.map(c => (
            <option key={c.id} value={c.email}>{c.nombre} ({c.email})</option>
          ))}
        </select>
      </label>
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

function PacienteForm({paciente, onGuardado, onCancelarEdicion, notify, cuentasDisponibles}){
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
        credentials: 'include',
        headers: {'Content-Type':'application/json'},
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
      <label className="sm:col-span-2 block">
        <span className="label">Cuenta de acceso vinculada</span>
        <select className="input-field" value={email} onChange={e=>setEmail(e.target.value)}>
          <option value="">Sin vincular</option>
          {email && !cuentasDisponibles.some(c => c.email === email) && (
            <option value={email}>{email} (cuenta no encontrada)</option>
          )}
          {cuentasDisponibles.map(c => (
            <option key={c.id} value={c.email}>{c.nombre} ({c.email})</option>
          ))}
        </select>
      </label>
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

function UsuarioForm({notify, onGuardado}){
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

export default function App(){
  // Efecto ripple estilo Material: un solo listener global cubre todos los
  // botones .btn (actuales y futuros), sin tener que instrumentar cada uno.
  useEffect(() => {
    function handleRipple(e){
      const btn = e.target.closest('.btn-primary, .btn-secondary, .btn-danger')
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

  // El JWT vive en una cookie httpOnly que el navegador manda solo
  // (credentials: 'include' en cada fetch) — JS no puede leerla ni
  // escribirla, así que acá solo guardamos datos no sensibles para
  // renderizar la UI sin esperar un round-trip.
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  })

  function handleLoginExitoso(data){
    setAuth(data)
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data))
  }

  async function handleLogout(){
    setAuth(null)
    localStorage.removeItem(AUTH_STORAGE_KEY)
    try{
      // Limpia la cookie del lado del servidor. Best-effort: si la llamada
      // de red falla igual ya deslogueamos localmente.
      await fetch(`${AUTH_API}/logout`, {method: 'POST', credentials: 'include'})
    }catch{
      // ignorado a propósito
    }
  }

  async function apiFetch(url, options = {}){
    const resp = await fetch(url, {...options, credentials: 'include'})
    if(resp.status === 401){
      handleLogout()
      throw new Error('Sesión expirada, iniciá sesión de nuevo')
    }
    return resp
  }

  const [medicos, setMedicos] = useState([])
  const [pacientes, setPacientes] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [medicosLoading, setMedicosLoading] = useState(false)
  const [pacientesLoading, setPacientesLoading] = useState(false)
  const [editingMedico, setEditingMedico] = useState(null)
  const [editingPaciente, setEditingPaciente] = useState(null)
  const [paginaMedicos, setPaginaMedicos] = useState(0)
  const [totalPaginasMedicos, setTotalPaginasMedicos] = useState(0)
  const [paginaPacientes, setPaginaPacientes] = useState(0)
  const [totalPaginasPacientes, setTotalPaginasPacientes] = useState(0)
  const [medicosVinculados, setMedicosVinculados] = useState([])
  const [pacientesVinculados, setPacientesVinculados] = useState([])
  const [especialidades, setEspecialidades] = useState([])
  const [medicosPorEspecialidad, setMedicosPorEspecialidad] = useState([])

  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('')
  const [medicoId, setMedicoId] = useState('')
  const [preparacion, setPreparacion] = useState('')
  const [loading, setLoading] = useState(false)

  const [dniBusqueda, setDniBusqueda] = useState('')
  const [pacienteEncontrado, setPacienteEncontrado] = useState(null)

  const [turnos, setTurnos] = useState([])
  const [filtroMedicoId, setFiltroMedicoId] = useState('')
  const [filtroPacienteId, setFiltroPacienteId] = useState('')
  const [paginaTurnos, setPaginaTurnos] = useState(0)
  const [totalPaginasTurnos, setTotalPaginasTurnos] = useState(0)
  const [listLoading, setListLoading] = useState(false)
  const [estadoUpdatingId, setEstadoUpdatingId] = useState(null)
  const [turnoACancelar, setTurnoACancelar] = useState(null)
  const [pasoCancelacion, setPasoCancelacion] = useState(0)

  const [historiaAbiertaId, setHistoriaAbiertaId] = useState(null)
  const [historiaPorPaciente, setHistoriaPorPaciente] = useState({})
  const [historiaLoading, setHistoriaLoading] = useState(false)
  const [diagnostico, setDiagnostico] = useState('')
  const [tratamientoRegistro, setTratamientoRegistro] = useState('')
  const [observacionesRegistro, setObservacionesRegistro] = useState('')
  const [guardandoRegistro, setGuardandoRegistro] = useState(false)

  const [vinculado, setVinculado] = useState(null)
  const [mostrarCambiarPropia, setMostrarCambiarPropia] = useState(false)
  const [usuarioAResetear, setUsuarioAResetear] = useState(null)

  async function chequearVinculacion(){
    const url = auth.role === 'MEDICO' ? `${MEDICOS_API}/me` : `${PACIENTES_API}/me`
    const resp = await apiFetch(url)
    setVinculado(resp.ok)
  }

  async function cargarUsuarios(){
    const resp = await apiFetch(USUARIOS_API)
    if(resp.ok) setUsuarios(await resp.json())
  }

  async function cargarMedicos(paginaParam = paginaMedicos){
    setMedicosLoading(true)
    try{
      const params = new URLSearchParams()
      params.set('page', paginaParam)
      const resp = await apiFetch(`${MEDICOS_API}?${params}`)
      if(resp.ok){
        const data = await resp.json()
        setMedicos(data.content)
        setPaginaMedicos(data.page)
        setTotalPaginasMedicos(data.totalPages)
      }
    }finally{
      setMedicosLoading(false)
    }
  }

  function irAPaginaMedicos(pagina){
    cargarMedicos(pagina)
  }

  async function cargarPacientes(paginaParam = paginaPacientes){
    setPacientesLoading(true)
    try{
      const params = new URLSearchParams()
      params.set('page', paginaParam)
      const resp = await apiFetch(`${PACIENTES_API}?${params}`)
      if(resp.ok){
        const data = await resp.json()
        setPacientes(data.content)
        setPaginaPacientes(data.page)
        setTotalPaginasPacientes(data.totalPages)
      }
    }finally{
      setPacientesLoading(false)
    }
  }

  function irAPaginaPacientes(pagina){
    cargarPacientes(pagina)
  }

  async function cargarMedicosVinculados(){
    const resp = await apiFetch(`${MEDICOS_API}/emails-vinculados`)
    if(resp.ok) setMedicosVinculados(await resp.json())
  }

  async function cargarPacientesVinculados(){
    const resp = await apiFetch(`${PACIENTES_API}/emails-vinculados`)
    if(resp.ok) setPacientesVinculados(await resp.json())
  }

  async function cargarEspecialidades(){
    const resp = await apiFetch(`${MEDICOS_API}/especialidades`)
    if(resp.ok) setEspecialidades(await resp.json())
  }

  async function handleEspecialidadChange(valor){
    setEspecialidad(valor)
    setMedicoId('')
    if(!valor){ setMedicosPorEspecialidad([]); return }
    const params = new URLSearchParams({especialidad: valor, size: '500'})
    const resp = await apiFetch(`${MEDICOS_API}?${params}`)
    if(resp.ok){
      const data = await resp.json()
      setMedicosPorEspecialidad(data.content)
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
      await cargarMedicosVinculados()
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
      await cargarPacientesVinculados()
    }catch(err){
      notify(err.message)
    }
  }

  async function cargarTurnos(medicoIdParam = filtroMedicoId, pacienteIdParam = filtroPacienteId, paginaParam = paginaTurnos){
    setListLoading(true)
    try{
      const params = new URLSearchParams()
      if(medicoIdParam) params.set('medicoId', medicoIdParam)
      if(pacienteIdParam) params.set('pacienteId', pacienteIdParam)
      params.set('page', paginaParam)
      const resp = await apiFetch(`${TURNOS_API}?${params}`)
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      const data = await resp.json()
      setTurnos(data.content)
      setPaginaTurnos(data.page)
      setTotalPaginasTurnos(data.totalPages)
    }catch(err){
      notify(err.message)
    }finally{
      setListLoading(false)
    }
  }

  useEffect(() => {
    if(!auth) return
    if(auth.role === 'ADMINISTRADOR') {
      cargarMedicos(); cargarUsuarios(); cargarMedicosVinculados(); cargarEspecialidades()
    }
    if(auth.role === 'ADMINISTRADOR' || auth.role === 'MEDICO') cargarPacientes()
    if(auth.role === 'ADMINISTRADOR') cargarPacientesVinculados()
    if(auth.role === 'MEDICO' || auth.role === 'PACIENTE') chequearVinculacion()
    cargarTurnos()
  }, [auth])

  async function buscarPacientePorDni(e){
    e.preventDefault()
    const params = new URLSearchParams({dni: dniBusqueda.trim()})
    const resp = await apiFetch(`${PACIENTES_API}/buscar-por-dni?${params}`)
    if(resp.ok){
      setPacienteEncontrado(await resp.json())
    }else{
      setPacienteEncontrado(null)
      notify('No se encontró ningún paciente con ese DNI. Dalo de alta primero en la sección Pacientes.')
    }
  }

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    try{
      const resp = await apiFetch(TURNOS_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          fechaHora, especialidad, medicoId: Number(medicoId), pacienteId: pacienteEncontrado.id, preparacion
        })
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      const data = await resp.json()
      notify(`Turno creado para ${pacienteEncontrado.nombre} el ${fechaHora}.`, 'success')
      setEspecialidad('')
      setMedicoId('')
      setPreparacion('')
      setDniBusqueda('')
      setPacienteEncontrado(null)
      await cargarTurnos()
    }catch(err){
      notify(err.message)
    }finally{setLoading(false)}
  }

  function handleFiltrar(e){
    e.preventDefault()
    cargarTurnos(filtroMedicoId, filtroPacienteId, 0)
  }

  function irAPaginaTurnos(pagina){
    cargarTurnos(filtroMedicoId, filtroPacienteId, pagina)
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

  function iniciarCancelacionComoPaciente(turno){
    setTurnoACancelar(turno)
    setPasoCancelacion(1)
  }

  function cerrarModalCancelacion(){
    setTurnoACancelar(null)
    setPasoCancelacion(0)
  }

  function confirmarPrimerPaso(){
    setPasoCancelacion(2)
  }

  function confirmarCancelacionDefinitiva(){
    cambiarEstado(turnoACancelar.id, 'CANCELADO')
    cerrarModalCancelacion()
  }

  async function cargarHistoria(pacienteId){
    setHistoriaLoading(true)
    try{
      const resp = await apiFetch(`${HISTORIAS_API}?pacienteId=${pacienteId}`)
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      const data = await resp.json()
      setHistoriaPorPaciente(prev => ({...prev, [pacienteId]: data}))
    }catch(err){
      notify(err.message)
    }finally{
      setHistoriaLoading(false)
    }
  }

  function toggleHistoria(turno){
    if(historiaAbiertaId === turno.id){
      setHistoriaAbiertaId(null)
      return
    }
    setHistoriaAbiertaId(turno.id)
    setDiagnostico('')
    setTratamientoRegistro('')
    setObservacionesRegistro('')
    if(!historiaPorPaciente[turno.pacienteId]){
      cargarHistoria(turno.pacienteId)
    }
  }

  async function agregarRegistro(turno){
    setGuardandoRegistro(true)
    try{
      const resp = await apiFetch(HISTORIAS_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          medicoId: turno.medicoId,
          pacienteId: turno.pacienteId,
          diagnostico,
          tratamiento: tratamientoRegistro,
          observaciones: observacionesRegistro
        })
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify('Registro clínico agregado.', 'success')
      setDiagnostico('')
      setTratamientoRegistro('')
      setObservacionesRegistro('')
      await cargarHistoria(turno.pacienteId)
    }catch(err){
      notify(err.message)
    }finally{
      setGuardandoRegistro(false)
    }
  }

  async function descargarHistoria(pacienteId){
    try{
      const resp = await apiFetch(`${HISTORIAS_API}/exportar?pacienteId=${pacienteId}`)
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      const blob = await resp.blob()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `historia-clinica-paciente-${pacienteId}.txt`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
    }catch(err){
      notify(err.message)
    }
  }

  if(!auth){
    return (
      <>
        <LoginScreen onLoginExitoso={handleLoginExitoso} notify={notify} />
        <ToastContainer toasts={toasts} />
      </>
    )
  }

  const esAdmin = auth.role === 'ADMINISTRADOR'
  const esMedico = auth.role === 'MEDICO'
  const esPaciente = auth.role === 'PACIENTE'
  const puedeGestionarTurnos = esAdmin || esMedico
  const hoy = new Date().toLocaleDateString('es-AR', {day: '2-digit', month: '2-digit', year: 'numeric'})

  const emailsMedicosOcupados = new Set(
    medicosVinculados.filter(m => m.id !== editingMedico?.id).map(m => m.email).filter(Boolean)
  )
  const cuentasMedicoDisponibles = usuarios.filter(u => u.role === 'MEDICO' && !emailsMedicosOcupados.has(u.email))

  const emailsPacientesOcupados = new Set(
    pacientesVinculados.filter(p => p.id !== editingPaciente?.id).map(p => p.email).filter(Boolean)
  )
  const cuentasPacienteDisponibles = usuarios.filter(u => u.role === 'PACIENTE' && !emailsPacientesOcupados.has(u.email))

  return (
    <div className="min-h-screen bg-neutral-200 font-sans">
      <ToastContainer toasts={toasts} />
      <ConfirmModal
        open={pasoCancelacion === 1}
        title="Cancelar turno"
        message={turnoACancelar ? `¿Seguro que querés cancelar el turno del ${turnoACancelar.fechaHora}?` : ''}
        confirmLabel="Sí, cancelar"
        cancelLabel="No, mantener el turno"
        onConfirm={confirmarPrimerPaso}
        onCancel={cerrarModalCancelacion}
      />
      <ConfirmModal
        open={pasoCancelacion === 2}
        title="¿Confirmás la cancelación?"
        message="Esta acción no se puede deshacer y libera el horario para otro paciente."
        confirmLabel="Confirmar cancelación"
        cancelLabel="Volver"
        danger
        onConfirm={confirmarCancelacionDefinitiva}
        onCancel={cerrarModalCancelacion}
      />
      <CambiarContrasenaModal
        open={mostrarCambiarPropia}
        modo="propia"
        notify={notify}
        onClose={() => setMostrarCambiarPropia(false)}
      />
      <CambiarContrasenaModal
        open={!!usuarioAResetear}
        modo="reset"
        usuarioObjetivo={usuarioAResetear}
        notify={notify}
        onClose={() => setUsuarioAResetear(null)}
      />
      <header className="bg-primary-800 text-white">
        <div className="max-w-3xl mx-auto px-6 py-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold tracking-tight">MedConnect</h1>
            <p className="text-sm text-primary-100">Gestión de turnos</p>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-primary-100">{auth.nombre} · {auth.role}</span>
            <button type="button" onClick={() => setMostrarCambiarPropia(true)} className="btn-secondary !px-3 !py-1.5 text-xs">
              Cambiar contraseña
            </button>
            <button type="button" onClick={handleLogout} className="btn-secondary !px-3 !py-1.5 text-xs">
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-8 space-y-8">
        {(esMedico || esPaciente) && vinculado === false && (
          <div className="card border border-warning-300 bg-warning-50">
            <p className="text-sm text-warning-800">
              Tu cuenta todavía no está vinculada a ningún perfil de {esMedico ? 'médico' : 'paciente'}
              {' '}en el sistema, por eso no ves {esMedico ? 'tus pacientes ni turnos' : 'tu turno ni tu historial'} todavía.
              Pedile a un administrador que cargue tu email (<strong>{auth.email}</strong>) en tu ficha
              {esMedico ? ' de médico' : ' de paciente'} para quedar vinculado.
            </p>
          </div>
        )}

        {esAdmin && (
          <section className="card">
            <h2 className="heading mb-4">Usuarios</h2>
            <p className="text-sm text-neutral-500 mb-4">
              Creá una cuenta de acceso (login) para otro administrador, médico o paciente.
            </p>
            <UsuarioForm notify={notify} onGuardado={cargarUsuarios} />
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
                        <button type="button" onClick={() => setUsuarioAResetear(u)} className="btn-secondary !px-2 !py-1 text-xs">
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
        )}

        {esAdmin && (
          <section className="card">
            <h2 className="heading mb-4">Médicos</h2>
            <MedicoForm
              key={editingMedico?.id ?? 'new'}
              medico={editingMedico}
              notify={notify}
              onGuardado={async () => { setEditingMedico(null); await cargarMedicos(); await cargarMedicosVinculados() }}
              onCancelarEdicion={() => setEditingMedico(null)}
              cuentasDisponibles={cuentasMedicoDisponibles}
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
            {totalPaginasMedicos > 1 && (
              <div className="flex items-center justify-between mt-4">
                <button
                  type="button"
                  disabled={medicosLoading || paginaMedicos === 0}
                  onClick={() => irAPaginaMedicos(paginaMedicos - 1)}
                  className="btn-secondary !px-3 !py-1.5 text-xs"
                >
                  ← Anterior
                </button>
                <span className="text-sm text-neutral-500">
                  Página {paginaMedicos + 1} de {totalPaginasMedicos}
                </span>
                <button
                  type="button"
                  disabled={medicosLoading || paginaMedicos + 1 >= totalPaginasMedicos}
                  onClick={() => irAPaginaMedicos(paginaMedicos + 1)}
                  className="btn-secondary !px-3 !py-1.5 text-xs"
                >
                  Siguiente →
                </button>
              </div>
            )}
          </section>
        )}

        {(esAdmin || esMedico) && (
        <section className="card">
          <h2 className="heading mb-4">{esAdmin ? 'Pacientes' : 'Mis pacientes'}</h2>
          {esAdmin && (
            <PacienteForm
              key={editingPaciente?.id ?? 'new'}
              paciente={editingPaciente}
              notify={notify}
              onGuardado={async () => { setEditingPaciente(null); await cargarPacientes(); await cargarPacientesVinculados() }}
              onCancelarEdicion={() => setEditingPaciente(null)}
              cuentasDisponibles={cuentasPacienteDisponibles}
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
                              <button type="button" onClick={() => setEditingPaciente(p)} className="btn-secondary !px-2 !py-1 text-xs">
                                Editar
                              </button>
                              <button type="button" onClick={() => eliminarPaciente(p)} className="btn-secondary !px-2 !py-1 text-xs text-danger-600">
                                Eliminar
                              </button>
                              <button type="button" onClick={() => descargarHistoria(p.id)} className="btn-secondary !px-2 !py-1 text-xs">
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
          {totalPaginasPacientes > 1 && (
            <div className="flex items-center justify-between mt-4">
              <button
                type="button"
                disabled={pacientesLoading || paginaPacientes === 0}
                onClick={() => irAPaginaPacientes(paginaPacientes - 1)}
                className="btn-secondary !px-3 !py-1.5 text-xs"
              >
                ← Anterior
              </button>
              <span className="text-sm text-neutral-500">
                Página {paginaPacientes + 1} de {totalPaginasPacientes}
              </span>
              <button
                type="button"
                disabled={pacientesLoading || paginaPacientes + 1 >= totalPaginasPacientes}
                onClick={() => irAPaginaPacientes(paginaPacientes + 1)}
                className="btn-secondary !px-3 !py-1.5 text-xs"
              >
                Siguiente →
              </button>
            </div>
          )}
        </section>
        )}

        {esAdmin && (
          <section className="card">
            <h2 className="heading mb-4">Otorgar turno</h2>
            <form onSubmit={buscarPacientePorDni} className="flex flex-wrap items-end gap-3 mb-4">
              <div>
                <label className="label">DNI del paciente</label>
                <input className="input-field w-40" value={dniBusqueda} onChange={e=>{setDniBusqueda(e.target.value); setPacienteEncontrado(null)}} />
              </div>
              <button type="submit" className="btn-primary">Buscar paciente</button>
            </form>

            {pacienteEncontrado && (
              <div className="mb-4 rounded-lg border border-neutral-200 bg-paper-100 px-4 py-3 text-sm">
                <p className="font-medium text-neutral-800">{pacienteEncontrado.nombre} — DNI {pacienteEncontrado.dni}</p>
                {pacienteEncontrado.obraSocial && (
                  <p className="text-neutral-500">{pacienteEncontrado.obraSocial}{pacienteEncontrado.plan ? ` · ${pacienteEncontrado.plan}` : ''}</p>
                )}
              </div>
            )}

            {pacienteEncontrado && (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="label">Fecha y hora</label>
                  <input className="input-field" value={fechaHora} onChange={e=>setFechaHora(e.target.value)} />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">Especialidad</label>
                    <select className="input-field" value={especialidad} onChange={e=>{clearValidity(e); handleEspecialidadChange(e.target.value)}} onInvalid={handleInvalid} required>
                      <option value="" disabled>Seleccionar especialidad</option>
                      {especialidades.map(esp => (
                        <option key={esp} value={esp}>{esp}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="label">Médico</label>
                    <select className="input-field" value={medicoId} onChange={e=>{clearValidity(e); setMedicoId(e.target.value)}} onInvalid={handleInvalid} required disabled={!especialidad}>
                      <option value="" disabled>Seleccionar médico</option>
                      {medicosPorEspecialidad.map(m => (
                        <option key={m.id} value={m.id}>{m.nombre}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div>
                  <label className="label">Preparación (opcional)</label>
                  <input
                    className="input-field"
                    placeholder="Ej: asistir 15 minutos antes y pasar por recepción para dar presente"
                    value={preparacion}
                    onChange={e=>setPreparacion(e.target.value)}
                  />
                </div>
                <button type="submit" disabled={loading} className="btn-primary">
                  {loading ? 'Enviando…' : 'Otorgar turno'}
                </button>
              </form>
            )}
          </section>
        )}

        <section className="card">
          <h2 className="heading mb-4">{esPaciente ? 'Mis turnos' : 'Turnos'}</h2>

          {esMedico && (
            <div className="mb-4 rounded-lg bg-primary-800 text-white px-4 py-3">
              <p className="text-xs uppercase tracking-wide text-primary-100">Turnos para hoy</p>
              <p className="text-2xl font-semibold tabular-nums">{hoy}</p>
            </div>
          )}

          {esAdmin && (
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
                onClick={() => { setFiltroMedicoId(''); setFiltroPacienteId(''); cargarTurnos('', '', 0) }}
                className="btn-secondary"
              >
                Ver todos
              </button>
            </form>
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
                            {t.estado === 'CANCELADO' && !esMedico && (
                              <span className="text-neutral-400">—</span>
                            )}
                            {esMedico && (
                              <button
                                type="button"
                                onClick={() => toggleHistoria(t)}
                                className="btn-secondary !px-2 !py-1 text-xs"
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
                              onClick={() => iniciarCancelacionComoPaciente(t)}
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
                      <tr className="bg-paper-100/40">
                        <td colSpan={8} className="px-4 py-4">
                          <div className="space-y-3">
                            <h3 className="font-medium text-neutral-700">
                              Historia clínica de {t.pacienteNombre ?? `#${t.pacienteId}`}
                            </h3>
                            {historiaLoading ? (
                              <p className="text-sm text-neutral-400">Cargando…</p>
                            ) : (historiaPorPaciente[t.pacienteId]?.length ?? 0) === 0 ? (
                              <p className="text-sm text-neutral-400">Sin registros previos.</p>
                            ) : (
                              <ul className="space-y-2">
                                {historiaPorPaciente[t.pacienteId].map(r => (
                                  <li key={r.id} className="rounded-lg border border-neutral-200 bg-paper-50 px-3 py-2 text-sm">
                                    <div className="text-neutral-500">
                                      {r.fecha} — {r.medicoNombre ?? `#${r.medicoId}`}{r.medicoEspecialidad ? ` (${r.medicoEspecialidad})` : ''}
                                    </div>
                                    <div><span className="font-medium">Diagnóstico:</span> {r.diagnostico}</div>
                                    <div><span className="font-medium">Tratamiento:</span> {r.tratamiento}</div>
                                    {r.observaciones && (
                                      <div><span className="font-medium">Observaciones:</span> {r.observaciones}</div>
                                    )}
                                  </li>
                                ))}
                              </ul>
                            )}
                            <div className="space-y-2 pt-2 border-t border-neutral-200">
                              <p className="text-sm font-medium text-neutral-700">Agregar registro de esta consulta</p>
                              <input
                                className="input-field"
                                placeholder="Diagnóstico"
                                value={diagnostico}
                                onChange={e => setDiagnostico(e.target.value)}
                              />
                              <input
                                className="input-field"
                                placeholder="Tratamiento"
                                value={tratamientoRegistro}
                                onChange={e => setTratamientoRegistro(e.target.value)}
                              />
                              <input
                                className="input-field"
                                placeholder="Observaciones (opcional)"
                                value={observacionesRegistro}
                                onChange={e => setObservacionesRegistro(e.target.value)}
                              />
                              <button
                                type="button"
                                disabled={guardandoRegistro || !diagnostico || !tratamientoRegistro}
                                onClick={() => agregarRegistro(t)}
                                className="btn-primary !px-3 !py-1.5 text-xs"
                              >
                                {guardandoRegistro ? 'Guardando…' : 'Guardar registro'}
                              </button>
                            </div>
                          </div>
                        </td>
                      </tr>
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
          {totalPaginasTurnos > 1 && (
            <div className="flex items-center justify-between mt-4">
              <button
                type="button"
                disabled={listLoading || paginaTurnos === 0}
                onClick={() => irAPaginaTurnos(paginaTurnos - 1)}
                className="btn-secondary !px-3 !py-1.5 text-xs"
              >
                ← Anterior
              </button>
              <span className="text-sm text-neutral-500">
                Página {paginaTurnos + 1} de {totalPaginasTurnos}
              </span>
              <button
                type="button"
                disabled={listLoading || paginaTurnos + 1 >= totalPaginasTurnos}
                onClick={() => irAPaginaTurnos(paginaTurnos + 1)}
                className="btn-secondary !px-3 !py-1.5 text-xs"
              >
                Siguiente →
              </button>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
