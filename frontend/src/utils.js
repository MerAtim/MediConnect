// Los mensajes nativos de validación del navegador ("Please fill out this
// field") vienen en el idioma del navegador, no de la página. Los pisamos
// con el texto en español vía la Constraint Validation API.
export function handleInvalid(e){
  const el = e.target
  if(el.validity.valueMissing){
    el.setCustomValidity(el.tagName === 'SELECT' ? 'Seleccioná una opción' : 'Completá este campo')
  }else if(el.validity.typeMismatch && el.type === 'email'){
    el.setCustomValidity('Ingresá un email válido')
  }else{
    el.setCustomValidity('')
  }
}

export function clearValidity(e){
  e.target.setCustomValidity('')
}

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): fechaHora se
// mostraba tal cual llega del backend (ISO crudo, ej.
// "2026-08-12T10:00:00") en vez de una fecha/hora legible. Si el valor no
// es un ISO valido (null, string vacio, formato inesperado) se devuelve
// sin tocar en vez de mostrar "Invalid Date".
export function formatFechaHora(fechaHora){
  if(!fechaHora) return fechaHora
  const fecha = new Date(fechaHora)
  if(Number.isNaN(fecha.getTime())) return fechaHora
  return fecha.toLocaleString('es-AR', {day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'})
}

// El backend devuelve los errores como texto plano (no JSON), así que
// resp.json() falla en silencio y perdemos el mensaje real. Leemos el body
// como texto siempre y probamos parsearlo como JSON por si acaso.
export async function readErrorMessage(resp){
  const text = await resp.text().catch(() => '')
  try{
    const json = JSON.parse(text)
    return typeof json === 'string' ? json : (json?.message ?? `HTTP ${resp.status}`)
  }catch{
    return text || `HTTP ${resp.status}`
  }
}
