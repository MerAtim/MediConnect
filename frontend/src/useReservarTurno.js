import {useRef, useState} from 'react'
import {MEDICOS_API, TURNOS_API} from './config.js'
import {apiFetch} from './apiClient.js'
import {formatFechaHora, readErrorMessage} from './utils.js'

// Mismo default/formateo que useOtorgarTurno.js -- se duplica a propósito
// (2 funciones chicas, usadas en 2 archivos) en vez de acoplar ambos hooks
// a un utils.js compartido para esto.
function fechaHoraPorDefecto(){
  const enUnaHora = new Date(Date.now() + 60 * 60 * 1000)
  const pad = n => String(n).padStart(2, '0')
  return `${enUnaHora.getFullYear()}-${pad(enUnaHora.getMonth() + 1)}-${pad(enUnaHora.getDate())}T${pad(enUnaHora.getHours())}:${pad(enUnaHora.getMinutes())}`
}

function conSegundos(fechaHoraLocal){
  return fechaHoraLocal.length === 16 ? `${fechaHoraLocal}:00` : fechaHoraLocal
}

// Autogestión del PACIENTE: variante de useOtorgarTurno sin el paso de
// búsqueda por DNI (reserva para sí mismo, no para otro). No hace falta
// mandar pacienteId en el body -- TurnoController.crear() lo resuelve/
// reemplaza vía el email del token para cualquier caller con rol PACIENTE,
// e ignora lo que venga en el request.
export function useReservarTurno(notify, cargarTurnos){
  const [fechaHora, setFechaHora] = useState(fechaHoraPorDefecto)
  const [especialidad, setEspecialidad] = useState('')
  const [medicoId, setMedicoId] = useState('')
  const [preparacion, setPreparacion] = useState('')
  const [loading, setLoading] = useState(false)

  const [especialidades, setEspecialidades] = useState([])
  const [medicosPorEspecialidad, setMedicosPorEspecialidad] = useState([])

  async function cargarEspecialidades(){
    try{
      const resp = await apiFetch(`${MEDICOS_API}/especialidades`)
      if(resp.ok) setEspecialidades(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  // Mismo patrón que useOtorgarTurno.js: cancela el pedido anterior para
  // que un cambio rápido de especialidad no deje la pantalla mostrando la
  // respuesta que llegó última en vez de la que se pidió última.
  const especialidadAbortRef = useRef(null)

  async function handleEspecialidadChange(valor){
    setEspecialidad(valor)
    setMedicoId('')
    especialidadAbortRef.current?.abort()
    if(!valor){ setMedicosPorEspecialidad([]); return }
    const controller = new AbortController()
    especialidadAbortRef.current = controller
    try{
      // A diferencia de useOtorgarTurno (que usa el listado paginado
      // GET /medicos, solo-admin), esto usa el picker liviano
      // GET /medicos/por-especialidad, abierto a PACIENTE.
      const params = new URLSearchParams({especialidad: valor})
      const resp = await apiFetch(`${MEDICOS_API}/por-especialidad?${params}`, {signal: controller.signal})
      if(resp.ok) setMedicosPorEspecialidad(await resp.json())
    }catch(err){
      if(err.name !== 'AbortError') notify(err.message)
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
          fechaHora: conSegundos(fechaHora), especialidad, medicoId: Number(medicoId), preparacion
        })
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      await resp.json()
      notify(`Turno reservado para el ${formatFechaHora(fechaHora)}.`, 'success')
      setEspecialidad('')
      setMedicoId('')
      setPreparacion('')
      await cargarTurnos()
    }catch(err){
      notify(err.message)
    }finally{setLoading(false)}
  }

  return {
    fechaHora, setFechaHora, especialidad, medicoId, setMedicoId, preparacion, setPreparacion,
    loading, especialidades, medicosPorEspecialidad,
    cargarEspecialidades, handleEspecialidadChange, handleSubmit
  }
}
