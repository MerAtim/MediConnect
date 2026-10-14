import {useState} from 'react'
import {ESPECIALIDADES_API} from './config.js'
import {apiFetch} from './apiClient.js'
import {readErrorMessage} from './utils.js'

// Lista chica (una fila por especialidad, no paginada por backend -- ver
// EspecialidadController.listarDuraciones) a diferencia de useMedicos/
// usePacientes/useUsuarios, que sí paginan.
export function useEspecialidadDuraciones(notify){
  const [duraciones, setDuraciones] = useState([])
  const [duracionesLoading, setDuracionesLoading] = useState(false)
  const [guardandoEspecialidad, setGuardandoEspecialidad] = useState(null)

  async function cargarDuraciones(){
    setDuracionesLoading(true)
    try{
      const resp = await apiFetch(`${ESPECIALIDADES_API}/duraciones`)
      if(resp.ok) setDuraciones(await resp.json())
    }catch(err){
      notify(err.message)
    }finally{
      setDuracionesLoading(false)
    }
  }

  async function establecerDuracion(especialidad, duracionMinutos){
    setGuardandoEspecialidad(especialidad)
    try{
      const resp = await apiFetch(`${ESPECIALIDADES_API}/${encodeURIComponent(especialidad)}/duracion`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({duracionMinutos})
      })
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      notify('Duración actualizada.', 'success')
      await cargarDuraciones()
    }catch(err){
      notify(err.message)
    }finally{
      setGuardandoEspecialidad(null)
    }
  }

  return {duraciones, duracionesLoading, guardandoEspecialidad, cargarDuraciones, establecerDuracion}
}
