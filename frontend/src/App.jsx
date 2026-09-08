import React, {useEffect, useState} from 'react'
import {setSessionExpiredHandler} from './apiClient.js'
import {useToasts} from './useToasts.js'
import {useAuth} from './useAuth.js'
import {useMedicos} from './useMedicos.js'
import {usePacientes} from './usePacientes.js'
import {useUsuarios} from './useUsuarios.js'
import {useTurnos} from './useTurnos.js'
import {useOtorgarTurno} from './useOtorgarTurno.js'
import {useHistoriaClinica} from './useHistoriaClinica.js'
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

  const {toasts, notify} = useToasts()
  const {auth, handleLoginExitoso, handleLogout, vinculado, chequearVinculacion} = useAuth(notify)
  const medicosHook = useMedicos(notify)
  const pacientesHook = usePacientes(notify)
  const usuariosHook = useUsuarios(notify)
  const turnosHook = useTurnos(notify)
  const otorgarTurnoHook = useOtorgarTurno(notify, turnosHook.cargarTurnos)
  const historiaHook = useHistoriaClinica(notify)

  // apiFetch vive en apiClient.js para que los forms/modals también lo usen
  // (antes tenían su propio fetch sin manejo de sesión expirada). Se
  // re-registra en cada render para que el 401 siempre dispare el
  // handleLogout más reciente, no uno de un render viejo.
  setSessionExpiredHandler(handleLogout)

  const [mostrarCambiarPropia, setMostrarCambiarPropia] = useState(false)
  const [usuarioAResetear, setUsuarioAResetear] = useState(null)

  useEffect(() => {
    if(!auth) return
    if(auth.role === 'ADMINISTRADOR') {
      medicosHook.cargarMedicos()
      usuariosHook.cargarUsuarios()
      medicosHook.cargarMedicosVinculados()
      otorgarTurnoHook.cargarEspecialidades()
    }
    if(auth.role === 'ADMINISTRADOR' || auth.role === 'MEDICO') pacientesHook.cargarPacientes()
    if(auth.role === 'ADMINISTRADOR') pacientesHook.cargarPacientesVinculados()
    if(auth.role === 'MEDICO' || auth.role === 'PACIENTE') chequearVinculacion()
    turnosHook.cargarTurnos()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth])

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
    medicosHook.medicosVinculados.filter(m => m.id !== medicosHook.editingMedico?.id).map(m => m.email).filter(Boolean)
  )
  const cuentasMedicoDisponibles = usuariosHook.usuarios.filter(u => u.role === 'MEDICO' && !emailsMedicosOcupados.has(u.email))

  const emailsPacientesOcupados = new Set(
    pacientesHook.pacientesVinculados.filter(p => p.id !== pacientesHook.editingPaciente?.id).map(p => p.email).filter(Boolean)
  )
  const cuentasPacienteDisponibles = usuariosHook.usuarios.filter(u => u.role === 'PACIENTE' && !emailsPacientesOcupados.has(u.email))

  return (
    <div className="min-h-screen bg-neutral-200 font-sans">
      <ToastContainer toasts={toasts} />
      <ConfirmModal
        open={turnosHook.pasoCancelacion === 1}
        title="Cancelar turno"
        message={turnosHook.turnoACancelar ? `¿Seguro que querés cancelar el turno del ${turnosHook.turnoACancelar.fechaHora}?` : ''}
        confirmLabel="Sí, cancelar"
        cancelLabel="No, mantener el turno"
        onConfirm={turnosHook.confirmarPrimerPaso}
        onCancel={turnosHook.cerrarModalCancelacion}
      />
      <ConfirmModal
        open={turnosHook.pasoCancelacion === 2}
        title="¿Confirmás la cancelación?"
        message="Esta acción no se puede deshacer y libera el horario para otro paciente."
        confirmLabel="Confirmar cancelación"
        cancelLabel="Volver"
        danger
        onConfirm={turnosHook.confirmarCancelacionDefinitiva}
        onCancel={turnosHook.cerrarModalCancelacion}
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
            usuarios={usuariosHook.usuarios}
            notify={notify}
            onGuardado={usuariosHook.cargarUsuarios}
            onResetearClick={setUsuarioAResetear}
          />
        )}

        {esAdmin && (
          <MedicosSection
            medicos={medicosHook.medicos}
            medicosLoading={medicosHook.medicosLoading}
            editingMedico={medicosHook.editingMedico}
            onEditar={medicosHook.setEditingMedico}
            onCancelarEdicion={() => medicosHook.setEditingMedico(null)}
            onGuardado={async () => { medicosHook.setEditingMedico(null); await medicosHook.cargarMedicos(); await medicosHook.cargarMedicosVinculados() }}
            onEliminar={medicosHook.eliminarMedico}
            notify={notify}
            cuentasDisponibles={cuentasMedicoDisponibles}
            paginaMedicos={medicosHook.paginaMedicos}
            totalPaginasMedicos={medicosHook.totalPaginasMedicos}
            onIrAPagina={medicosHook.irAPaginaMedicos}
          />
        )}

        {(esAdmin || esMedico) && (
          <PacientesSection
            esAdmin={esAdmin}
            pacientes={pacientesHook.pacientes}
            pacientesLoading={pacientesHook.pacientesLoading}
            editingPaciente={pacientesHook.editingPaciente}
            onEditar={pacientesHook.setEditingPaciente}
            onCancelarEdicion={() => pacientesHook.setEditingPaciente(null)}
            onGuardado={async () => { pacientesHook.setEditingPaciente(null); await pacientesHook.cargarPacientes(); await pacientesHook.cargarPacientesVinculados() }}
            onEliminar={pacientesHook.eliminarPaciente}
            onDescargarHistoria={historiaHook.descargarHistoria}
            notify={notify}
            cuentasDisponibles={cuentasPacienteDisponibles}
            paginaPacientes={pacientesHook.paginaPacientes}
            totalPaginasPacientes={pacientesHook.totalPaginasPacientes}
            onIrAPagina={pacientesHook.irAPaginaPacientes}
          />
        )}

        {esAdmin && (
          <OtorgarTurnoSection
            dniBusqueda={otorgarTurnoHook.dniBusqueda}
            onDniBusquedaChange={otorgarTurnoHook.onDniBusquedaChange}
            onBuscarPorDni={otorgarTurnoHook.buscarPacientePorDni}
            pacienteEncontrado={otorgarTurnoHook.pacienteEncontrado}
            fechaHora={otorgarTurnoHook.fechaHora}
            onFechaHoraChange={otorgarTurnoHook.setFechaHora}
            especialidad={otorgarTurnoHook.especialidad}
            onEspecialidadChange={otorgarTurnoHook.handleEspecialidadChange}
            especialidades={otorgarTurnoHook.especialidades}
            medicoId={otorgarTurnoHook.medicoId}
            onMedicoIdChange={otorgarTurnoHook.setMedicoId}
            medicosPorEspecialidad={otorgarTurnoHook.medicosPorEspecialidad}
            preparacion={otorgarTurnoHook.preparacion}
            onPreparacionChange={otorgarTurnoHook.setPreparacion}
            loading={otorgarTurnoHook.loading}
            onSubmit={otorgarTurnoHook.handleSubmit}
          />
        )}

        <TurnosSection
          esAdmin={esAdmin}
          esMedico={esMedico}
          esPaciente={esPaciente}
          puedeGestionarTurnos={puedeGestionarTurnos}
          hoy={hoy}
          turnos={turnosHook.turnos}
          listLoading={turnosHook.listLoading}
          filtroMedicoId={turnosHook.filtroMedicoId}
          onFiltroMedicoIdChange={turnosHook.setFiltroMedicoId}
          filtroPacienteId={turnosHook.filtroPacienteId}
          onFiltroPacienteIdChange={turnosHook.setFiltroPacienteId}
          onFiltrar={turnosHook.handleFiltrar}
          onVerTodos={turnosHook.verTodos}
          paginaTurnos={turnosHook.paginaTurnos}
          totalPaginasTurnos={turnosHook.totalPaginasTurnos}
          onIrAPagina={turnosHook.irAPaginaTurnos}
          estadoUpdatingId={turnosHook.estadoUpdatingId}
          onCambiarEstado={turnosHook.cambiarEstado}
          onIniciarCancelacionPaciente={turnosHook.iniciarCancelacionComoPaciente}
          historiaAbiertaId={historiaHook.historiaAbiertaId}
          onToggleHistoria={historiaHook.toggleHistoria}
          historiaPorPaciente={historiaHook.historiaPorPaciente}
          historiaLoading={historiaHook.historiaLoading}
          diagnostico={historiaHook.diagnostico}
          onDiagnosticoChange={historiaHook.setDiagnostico}
          tratamientoRegistro={historiaHook.tratamientoRegistro}
          onTratamientoChange={historiaHook.setTratamientoRegistro}
          observacionesRegistro={historiaHook.observacionesRegistro}
          onObservacionesChange={historiaHook.setObservacionesRegistro}
          guardandoRegistro={historiaHook.guardandoRegistro}
          onAgregarRegistro={historiaHook.agregarRegistro}
        />
      </main>
    </div>
  )
}
