import React, { useState } from 'react'
import { clearValidity, handleInvalid } from '../utils.js'

export default function FloatingInput({label, type = 'text', value, onChange, required = false, className = ''}){
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
