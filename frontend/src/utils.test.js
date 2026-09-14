import {describe, expect, it} from 'vitest'
import {formatFechaHora} from './utils.js'

describe('formatFechaHora', () => {
  it('formatea un ISO valido a fecha/hora legible en es-AR', () => {
    const resultado = formatFechaHora('2026-08-12T10:00:00')
    expect(resultado).toContain('2026')
    expect(resultado).not.toBe('2026-08-12T10:00:00')
  })

  it('devuelve el valor sin tocar si es null, vacio o invalido', () => {
    expect(formatFechaHora(null)).toBe(null)
    expect(formatFechaHora('')).toBe('')
    expect(formatFechaHora('no-es-una-fecha')).toBe('no-es-una-fecha')
  })
})
