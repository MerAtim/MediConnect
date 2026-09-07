import React, {useEffect, useRef, useState} from 'react'
import {AUTH_API, AUTH_STORAGE_KEY, HISTORIAS_API, MEDICOS_API, PACIENTES_API, TURNOS_API, USUARIOS_API} from './config.js'
import {apiFetch, setSessionExpiredHandler} from './apiClient.js'
import {readErrorMessage} from './utils.js'
import CambiarContrasenaModal from './components/CambiarContrasenaModal.jsx'
import ConfirmModal from './components/ConfirmModal.jsx'
import LoginScreen from './components/LoginScreen.jsx'
import ToastContainer from './components/ToastContainer.jsx'
import UsuariosSection from './components/UsuariosSection.jsx'
import MedicosSection from './components/MedicosSection.jsx'
import PacientesSection from './components/PacientesSection.jsx'
import OtorgarTurnoSection from './components/OtorgarTurnoSection.jsx'
import TurnosSection from './components/TurnosSection.jsx'

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
          <UsuariosSection
            usuarios={usuarios}
            notify={notify}
            onGuardado={cargarUsuarios}
            onResetearClick={setUsuarioAResetear}
          />
        )}

        {esAdmin && (
          <MedicosSection
            medicos={medicos}
            medicosLoading={medicosLoading}
            editingMedico={editingMedico}
            onEditar={setEditingMedico}
            onCancelarEdicion={() => setEditingMedico(null)}
            onGuardado={async () => { setEditingMedico(null); await cargarMedicos(); await cargarMedicosVinculados() }}
            onEliminar={eliminarMedico}
            notify={notify}
            cuentasDisponibles={cuentasMedicoDisponibles}
            paginaMedicos={paginaMedicos}
            totalPaginasMedicos={totalPaginasMedicos}
            onIrAPagina={irAPaginaMedicos}
          />
        )}

        {(esAdmin || esMedico) && (
          <PacientesSection
            esAdmin={esAdmin}
            pacientes={pacientes}
            pacientesLoading={pacientesLoading}
            editingPaciente={editingPaciente}
            onEditar={setEditingPaciente}
            onCancelarEdicion={() => setEditingPaciente(null)}
            onGuardado={async () => { setEditingPaciente(null); await cargarPacientes(); await cargarPacientesVinculados() }}
            onEliminar={eliminarPaciente}
            onDescargarHistoria={descargarHistoria}
            notify={notify}
            cuentasDisponibles={cuentasPacienteDisponibles}
            paginaPacientes={paginaPacientes}
            totalPaginasPacientes={totalPaginasPacientes}
            onIrAPagina={irAPaginaPacientes}
          />
        )}

        {esAdmin && (
          <OtorgarTurnoSection
            dniBusqueda={dniBusqueda}
            onDniBusquedaChange={valor => { setDniBusqueda(valor); setPacienteEncontrado(null) }}
            onBuscarPorDni={buscarPacientePorDni}
            pacienteEncontrado={pacienteEncontrado}
            fechaHora={fechaHora}
            onFechaHoraChange={setFechaHora}
            especialidad={especialidad}
            onEspecialidadChange={handleEspecialidadChange}
            especialidades={especialidades}
            medicoId={medicoId}
            onMedicoIdChange={setMedicoId}
            medicosPorEspecialidad={medicosPorEspecialidad}
            preparacion={preparacion}
            onPreparacionChange={setPreparacion}
            loading={loading}
            onSubmit={handleSubmit}
          />
        )}

        <TurnosSection
          esAdmin={esAdmin}
          esMedico={esMedico}
          esPaciente={esPaciente}
          puedeGestionarTurnos={puedeGestionarTurnos}
          hoy={hoy}
          turnos={turnos}
          listLoading={listLoading}
          filtroMedicoId={filtroMedicoId}
          onFiltroMedicoIdChange={setFiltroMedicoId}
          filtroPacienteId={filtroPacienteId}
          onFiltroPacienteIdChange={setFiltroPacienteId}
          onFiltrar={handleFiltrar}
          onVerTodos={() => { setFiltroMedicoId(''); setFiltroPacienteId(''); cargarTurnos('', '', 0) }}
          paginaTurnos={paginaTurnos}
          totalPaginasTurnos={totalPaginasTurnos}
          onIrAPagina={irAPaginaTurnos}
          estadoUpdatingId={estadoUpdatingId}
          onCambiarEstado={cambiarEstado}
          onIniciarCancelacionPaciente={iniciarCancelacionComoPaciente}
          historiaAbiertaId={historiaAbiertaId}
          onToggleHistoria={toggleHistoria}
          historiaPorPaciente={historiaPorPaciente}
          historiaLoading={historiaLoading}
          diagnostico={diagnostico}
          onDiagnosticoChange={setDiagnostico}
          tratamientoRegistro={tratamientoRegistro}
          onTratamientoChange={setTratamientoRegistro}
          observacionesRegistro={observacionesRegistro}
          onObservacionesChange={setObservacionesRegistro}
          guardandoRegistro={guardandoRegistro}
          onAgregarRegistro={agregarRegistro}
        />
      </main>
    </div>
  )
}
