import {useState} from 'react'
import {USUARIOS_API} from './config.js'
import {apiFetch} from './apiClient.js'

export function useUsuarios(notify){
  const [usuarios, setUsuarios] = useState([])

  async function cargarUsuarios(){
    try{
      const resp = await apiFetch(USUARIOS_API)
      if(resp.ok) setUsuarios(await resp.json())
    }catch(err){
      notify(err.message)
    }
  }

  return {usuarios, cargarUsuarios}
}
