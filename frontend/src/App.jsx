import React, {useEffect, useRef, useState} from 'react'
import {AUTH_API, AUTH_STORAGE_KEY, HISTORIAS_API, MEDICOS_API, PACIENTES_API, TURNOS_API, USUARIOS_API} from './config.js'
import {apiFetch, setSessionExpiredHandler} from './apiClient.js'
import {clearValidity, handleInvalid, readErrorMessage} from './utils.js'
import CambiarContrasenaModal from './components/CambiarContrasenaModal.jsx'
import ConfirmModal from './components/ConfirmModal.jsx'
import EstadoBadge from './components/EstadoBadge.jsx'
import LoginScreen from './components/LoginScreen.jsx'
import MedicoForm from './components/MedicoForm.jsx'
import PacienteForm from './components/PacienteForm.jsx'
import SkeletonRows from './components/SkeletonRows.jsx'
import ToastContainer from './components/ToastContainer.jsx'
import UsuarioForm from './components/UsuarioForm.jsx'

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

  // apiFetch vive en apiClient.js para que los forms/modals también lo usen
  // (antes tenían su propio fetch sin manejo de sesión expirada). Se
  // re-registra en cada render para que el 401 siempre dispare el
  // handleLogout más reciente, no uno de un render viejo.
  setSessionExpiredHandler(handleLogout)

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
    try{
      const url = auth.role === 'MEDICO' ? `${MEDICOS_API}/me` : `${PACIENTES_API}/me`
      const resp = await apiFetch(url)
      setVinculado(resp.ok)
    }catch(err){
      notify(err.message)
    }
  }

  async function cargarUsuarios(){
    try{
      const resp = await apiFetch(USUARIOS_API)
      if(resp.ok) setUsuarios(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  // Los tres abort ref de acá abajo (medicos/pacientes/turnos) existen para
  // que clicks rápidos de paginación no dejen la pantalla mostrando la
  // respuesta que llegó última en vez de la que se pidió última: al
  // arrancar un pedido nuevo se cancela el anterior, y el `finally` de la
  // request cancelada no toca el loading si ya hay una más nueva en curso.
  const medicosAbortRef = useRef(null)

  async function cargarMedicos(paginaParam = paginaMedicos){
    medicosAbortRef.current?.abort()
    const controller = new AbortController()
    medicosAbortRef.current = controller
    setMedicosLoading(true)
    try{
      const params = new URLSearchParams()
      params.set('page', paginaParam)
      const resp = await apiFetch(`${MEDICOS_API}?${params}`, {signal: controller.signal})
      if(resp.ok){
        const data = await resp.json()
        setMedicos(data.content)
        setPaginaMedicos(data.page)
        setTotalPaginasMedicos(data.totalPages)
      }
    }catch(err){
      if(err.name !== 'AbortError') notify(err.message)
    }finally{
      if(medicosAbortRef.current === controller) setMedicosLoading(false)
    }
  }

  function irAPaginaMedicos(pagina){
    cargarMedicos(pagina)
  }

  const pacientesAbortRef = useRef(null)

  async function cargarPacientes(paginaParam = paginaPacientes){
    pacientesAbortRef.current?.abort()
    const controller = new AbortController()
    pacientesAbortRef.current = controller
    setPacientesLoading(true)
    try{
      const params = new URLSearchParams()
      params.set('page', paginaParam)
      const resp = await apiFetch(`${PACIENTES_API}?${params}`, {signal: controller.signal})
      if(resp.ok){
        const data = await resp.json()
        setPacientes(data.content)
        setPaginaPacientes(data.page)
        setTotalPaginasPacientes(data.totalPages)
      }
    }catch(err){
      if(err.name !== 'AbortError') notify(err.message)
    }finally{
      if(pacientesAbortRef.current === controller) setPacientesLoading(false)
    }
  }

  function irAPaginaPacientes(pagina){
    cargarPacientes(pagina)
  }

  async function cargarMedicosVinculados(){
    try{
      const resp = await apiFetch(`${MEDICOS_API}/emails-vinculados`)
      if(resp.ok) setMedicosVinculados(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  async function cargarPacientesVinculados(){
    try{
      const resp = await apiFetch(`${PACIENTES_API}/emails-vinculados`)
      if(resp.ok) setPacientesVinculados(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  async function cargarEspecialidades(){
    try{
      const resp = await apiFetch(`${MEDICOS_API}/especialidades`)
      if(resp.ok) setEspecialidades(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  async function handleEspecialidadChange(valor){
    setEspecialidad(valor)
    setMedicoId('')
    if(!valor){ setMedicosPorEspecialidad([]); return }
    try{
      const params = new URLSearchParams({especialidad: valor, size: '500'})
      const resp = await apiFetch(`${MEDICOS_API}?${params}`)
      if(resp.ok){
        const data = await resp.json()
        setMedicosPorEspecialidad(data.content)
      }
    }catch(err){
      notify(err.message)
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

  const turnosAbortRef = useRef(null)

  async function cargarTurnos(medicoIdParam = filtroMedicoId, pacienteIdParam = filtroPacienteId, paginaParam = paginaTurnos){
    turnosAbortRef.current?.abort()
    const controller = new AbortController()
    turnosAbortRef.current = controller
    setListLoading(true)
    try{
      const params = new URLSearchParams()
      if(medicoIdParam) params.set('medicoId', medicoIdParam)
      if(pacienteIdParam) params.set('pacienteId', pacienteIdParam)
      params.set('page', paginaParam)
      const resp = await apiFetch(`${TURNOS_API}?${params}`, {signal: controller.signal})
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      const data = await resp.json()
      setTurnos(data.content)
      setPaginaTurnos(data.page)
      setTotalPaginasTurnos(data.totalPages)
    }catch(err){
      if(err.name !== 'AbortError') notify(err.message)
    }finally{
      if(turnosAbortRef.current === controller) setListLoading(false)
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
