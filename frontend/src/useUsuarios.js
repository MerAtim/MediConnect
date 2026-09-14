import {useRef, useState} from 'react'
import {USUARIOS_API} from './config.js'
import {apiFetch} from './apiClient.js'

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): a diferencia de
// useMedicos/usePacientes/useTurnos, este hook no tenia paginacion ni
// loading -- traia todo de una sola vez y la tabla no mostraba ningun
// indicador de carga. Mismo patron que esos tres: pagina/totalPaginas/
// loading + AbortController para que clicks rapidos de paginacion no dejen
// la pantalla mostrando la respuesta que llego ultima en vez de la que se
// pidio ultima.
export function useUsuarios(notify){
  const [usuarios, setUsuarios] = useState([])
  const [usuariosLoading, setUsuariosLoading] = useState(false)
  const [paginaUsuarios, setPaginaUsuarios] = useState(0)
  const [totalPaginasUsuarios, setTotalPaginasUsuarios] = useState(0)

  // App.jsx usa esto para poblar el <select> de "cuenta de acceso vinculada"
  // en MedicoForm/PacienteForm -- necesita TODAS las cuentas disponibles, no
  // solo la pagina actual de la tabla de Usuarios (que ahora pagina de a 20).
  // Mismo patron que medicosVinculados/pacientesVinculados: una carga aparte
  // y desacoplada de la paginacion visible de la tabla.
  const [todosLosUsuarios, setTodosLosUsuarios] = useState([])

  const usuariosAbortRef = useRef(null)

  async function cargarUsuarios(paginaParam = paginaUsuarios){
    usuariosAbortRef.current?.abort()
    const controller = new AbortController()
    usuariosAbortRef.current = controller
    setUsuariosLoading(true)
    try{
      const params = new URLSearchParams()
      params.set('page', paginaParam)
      const resp = await apiFetch(`${USUARIOS_API}?${params}`, {signal: controller.signal})
      if(resp.ok){
        const data = await resp.json()
        setUsuarios(data.content)
        setPaginaUsuarios(data.page)
        setTotalPaginasUsuarios(data.totalPages)
      }
    }catch(err){
      if(err.name !== 'AbortError') notify(err.message)
    }finally{
      if(usuariosAbortRef.current === controller) setUsuariosLoading(false)
    }
  }

  function irAPaginaUsuarios(pagina){
    cargarUsuarios(pagina)
  }

  async function cargarTodosLosUsuarios(){
    try{
      const params = new URLSearchParams({size: '1000'})
      const resp = await apiFetch(`${USUARIOS_API}?${params}`)
      if(resp.ok) setTodosLosUsuarios((await resp.json()).content)
    }catch(err){
      notify(err.message)
    }
  }

  return {
    usuarios, usuariosLoading, paginaUsuarios, totalPaginasUsuarios, todosLosUsuarios,
    cargarUsuarios, irAPaginaUsuarios, cargarTodosLosUsuarios
  }
}
