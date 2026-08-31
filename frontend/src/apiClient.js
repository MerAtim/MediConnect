import { AUTH_STORAGE_KEY } from './config.js'

// App.setSessionExpiredHandler() lo pisa con el logout real (limpia el auth
// en memoria y avisa al backend) apenas monta. Este fallback solo cubre el
// caso de que algo llame apiFetch antes de que eso pase (o en un test que
// renderiza un componente aislado sin <App>).
let sessionExpiredHandler = () => {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function setSessionExpiredHandler(handler){
  sessionExpiredHandler = handler
}

// Wrapper de fetch compartido por App y por los forms/modals que escriben
// datos. Antes cada form tenía su propio fetch con credentials:'include'
// pero sin el manejo de "401 -> sesión expirada" que sí tenía App, así que
// una sesión vencida a mitad de una edición mostraba un error crudo en vez
// del logout automático.
export async function apiFetch(url, options = {}){
  const resp = await fetch(url, {...options, credentials: 'include'})
  if(resp.status === 401){
    sessionExpiredHandler()
    throw new Error('Sesión expirada, iniciá sesión de nuevo')
  }
  return resp
}
