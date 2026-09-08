import {useRef, useState} from 'react'
import {MEDICOS_API} from './config.js'
import {apiFetch} from './apiClient.js'

export function useMedicos(notify){
  const [medicos, setMedicos] = useState([])
  const [medicosLoading, setMedicosLoading] = useState(false)
  const [editingMedico, setEditingMedico] = useState(null)
  const [paginaMedicos, setPaginaMedicos] = useState(0)
  const [totalPaginasMedicos, setTotalPaginasMedicos] = useState(0)
  const [medicosVinculados, setMedicosVinculados] = useState([])

  // Existe para que clicks rápidos de paginación no dejen la pantalla
  // mostrando la respuesta que llegó última en vez de la que se pidió
  // última: al arrancar un pedido nuevo se cancela el anterior, y el
  // `finally` de la request cancelada no toca el loading si ya hay una
  // más nueva en curso.
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

  async function cargarMedicosVinculados(){
    try{
      const resp = await apiFetch(`${MEDICOS_API}/emails-vinculados`)
      if(resp.ok) setMedicosVinculados(await resp.json())
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

  return {
    medicos, medicosLoading, editingMedico, setEditingMedico,
    paginaMedicos, totalPaginasMedicos, medicosVinculados,
    cargarMedicos, irAPaginaMedicos, cargarMedicosVinculados, eliminarMedico
  }
}
