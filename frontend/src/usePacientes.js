import {useRef, useState} from 'react'
import {PACIENTES_API} from './config.js'
import {apiFetch} from './apiClient.js'

export function usePacientes(notify){
  const [pacientes, setPacientes] = useState([])
  const [pacientesLoading, setPacientesLoading] = useState(false)
  const [editingPaciente, setEditingPaciente] = useState(null)
  const [paginaPacientes, setPaginaPacientes] = useState(0)
  const [totalPaginasPacientes, setTotalPaginasPacientes] = useState(0)
  const [pacientesVinculados, setPacientesVinculados] = useState([])

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

  async function cargarPacientesVinculados(){
    try{
      const resp = await apiFetch(`${PACIENTES_API}/emails-vinculados`)
      if(resp.ok) setPacientesVinculados(await resp.json())
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

  return {
    pacientes, pacientesLoading, editingPaciente, setEditingPaciente,
    paginaPacientes, totalPaginasPacientes, pacientesVinculados,
    cargarPacientes, irAPaginaPacientes, cargarPacientesVinculados, eliminarPaciente
  }
}
