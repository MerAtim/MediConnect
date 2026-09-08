import {useState} from 'react'
import {apiFetch} from './apiClient.js'
import {readErrorMessage} from './utils.js'

// MEDIUM de la re-auditoria e2e (2026-09-08): "duplicacion entre
// useMedicos/usePacientes y entre los 3 formularios" -- MedicoForm,
// PacienteForm y UsuarioForm repetian el mismo patron de
// setLoading(true) -> try { fetch, chequear resp.ok, notify, onExito() }
// catch { notify(err.message) } finally { setLoading(false) }, cada uno
// con su propio bug potencial si alguno se desincronizaba del resto.
export function useSubmitForm(notify){
  const [loading, setLoading] = useState(false)

  async function submit(url, options, {mensajeExito, onExito} = {}){
    setLoading(true)
    try{
      const resp = await apiFetch(url, options)
      if(!resp.ok) throw new Error(await readErrorMessage(resp))
      if(mensajeExito) notify(mensajeExito, 'success')
      await onExito?.()
    }catch(err){
      notify(err.message)
    }finally{
      setLoading(false)
    }
  }

  return {loading, submit}
}
