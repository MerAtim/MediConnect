import {useState} from 'react'
import {MEDICOS_API, PACIENTES_API, TURNOS_API} from './config.js'
import {apiFetch} from './apiClient.js'
import {readErrorMessage} from './utils.js'

// Flujo completo de "Otorgar turno" (solo ADMINISTRADOR): buscar paciente
// por DNI, elegir especialidad -> médico, crear el turno. cargarTurnos
// viene de useTurnos, para refrescar la lista tras crear uno nuevo.
export function useOtorgarTurno(notify, cargarTurnos){
  const [dniBusqueda, setDniBusqueda] = useState('')
  const [pacienteEncontrado, setPacienteEncontrado] = useState(null)

  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('')
  const [medicoId, setMedicoId] = useState('')
  const [preparacion, setPreparacion] = useState('')
  const [loading, setLoading] = useState(false)

  const [especialidades, setEspecialidades] = useState([])
  const [medicosPorEspecialidad, setMedicosPorEspecialidad] = useState([])

  function onDniBusquedaChange(valor){
    setDniBusqueda(valor)
    setPacienteEncontrado(null)
  }

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
      await resp.json()
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

  return {
    dniBusqueda, onDniBusquedaChange, pacienteEncontrado, buscarPacientePorDni,
    fechaHora, setFechaHora, especialidad, medicoId, setMedicoId, preparacion, setPreparacion,
    loading, especialidades, medicosPorEspecialidad,
    cargarEspecialidades, handleEspecialidadChange, handleSubmit
  }
}
