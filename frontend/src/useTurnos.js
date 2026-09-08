import {useRef, useState} from 'react'
import {TURNOS_API} from './config.js'
import {apiFetch} from './apiClient.js'
import {readErrorMessage} from './utils.js'

export function useTurnos(notify){
  const [turnos, setTurnos] = useState([])
  const [filtroMedicoId, setFiltroMedicoId] = useState('')
  const [filtroPacienteId, setFiltroPacienteId] = useState('')
  const [paginaTurnos, setPaginaTurnos] = useState(0)
  const [totalPaginasTurnos, setTotalPaginasTurnos] = useState(0)
  const [listLoading, setListLoading] = useState(false)
  const [estadoUpdatingId, setEstadoUpdatingId] = useState(null)
  const [turnoACancelar, setTurnoACancelar] = useState(null)
  const [pasoCancelacion, setPasoCancelacion] = useState(0)

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

  function handleFiltrar(e){
    e.preventDefault()
    cargarTurnos(filtroMedicoId, filtroPacienteId, 0)
  }

  function verTodos(){
    setFiltroMedicoId('')
    setFiltroPacienteId('')
    cargarTurnos('', '', 0)
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

  return {
    turnos, filtroMedicoId, setFiltroMedicoId, filtroPacienteId, setFiltroPacienteId,
    paginaTurnos, totalPaginasTurnos, listLoading, estadoUpdatingId,
    cargarTurnos, handleFiltrar, verTodos, irAPaginaTurnos, cambiarEstado,
    turnoACancelar, pasoCancelacion, iniciarCancelacionComoPaciente,
    cerrarModalCancelacion, confirmarPrimerPaso, confirmarCancelacionDefinitiva
  }
}
