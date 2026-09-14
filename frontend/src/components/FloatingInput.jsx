import React, { useState } from 'react'
import { clearValidity, handleInvalid } from '../utils.js'

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): el componente no
// exponia ningun atributo autoComplete, asi que los campos de email/password
// (login, registro, cambio/reseteo de contrasena) quedaban sin
// autoComplete="username"/"current-password"/"new-password", degradando el
// autocompletado de gestores de contrasenas del navegador. Opcional: si no
// se pasa, el input queda sin el atributo (comportamiento identico a antes).
export default function FloatingInput({label, type = 'text', value, onChange, required = false, className = '', autoComplete}){
  const [focused, setFocused] = useState(false)
  const floated = focused || String(value ?? '').length > 0

  return (
    <label className={`relative block ${className}`}>
      <input
        type={type}
        value={value}
        onChange={e => { clearValidity(e); onChange(e) }}
        onInvalid={handleInvalid}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        required={required}
        autoComplete={autoComplete}
        className="input-field"
      />
      <span
        className={`pointer-events-none absolute left-3 transition-all duration-150 ${
          floated
            ? 'top-0 -translate-y-1/2 px-1 bg-paper-50 text-xs text-primary-700'
            : 'top-1/2 -translate-y-1/2 text-sm text-neutral-400'
        }`}
      >
        {label}
      </span>
    </label>
  )
}
