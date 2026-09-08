import {useState} from 'react'
import {HISTORIAS_API} from './config.js'
import {apiFetch} from './apiClient.js'
import {readErrorMessage} from './utils.js'

export function useHistoriaClinica(notify){
  const [historiaAbiertaId, setHistoriaAbiertaId] = useState(null)
  const [historiaPorPaciente, setHistoriaPorPaciente] = useState({})
  const [historiaLoading, setHistoriaLoading] = useState(false)
  const [diagnostico, setDiagnostico] = useState('')
  const [tratamientoRegistro, setTratamientoRegistro] = useState('')
  const [observacionesRegistro, setObservacionesRegistro] = useState('')
  const [guardandoRegistro, setGuardandoRegistro] = useState(false)

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

  return {
    historiaAbiertaId, historiaPorPaciente, historiaLoading,
    diagnostico, setDiagnostico, tratamientoRegistro, setTratamientoRegistro,
    observacionesRegistro, setObservacionesRegistro, guardandoRegistro,
    cargarHistoria, toggleHistoria, agregarRegistro, descargarHistoria
  }
}
