// En build de produccion (Docker/CI) se fija con la env var VITE_API_URL al
// correr `npm run build` (Vite la incrusta en el bundle en build-time, no en
// runtime). En desarrollo local, sin esa env var, cae a localhost:8080.
export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'
export const TURNOS_API = `${API_BASE}/api/turnos`
export const MEDICOS_API = `${API_BASE}/api/medicos`
export const PACIENTES_API = `${API_BASE}/api/pacientes`
export const HISTORIAS_API = `${API_BASE}/api/historias-clinicas`
export const AUTH_API = `${API_BASE}/api/auth`
export const USUARIOS_API = `${API_BASE}/api/usuarios`
export const AUTH_STORAGE_KEY = 'medconnect_auth'

export const ESTADO_BADGE = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  CANCELADO: 'badge-cancelado',
}

export const ROLES = ['MEDICO', 'PACIENTE']
export const ROLES_ADMIN = ['ADMINISTRADOR', 'MEDICO', 'PACIENTE']
