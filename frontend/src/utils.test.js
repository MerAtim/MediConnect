import {describe, expect, it} from 'vitest'
import {formatFechaHora, readErrorMessage} from './utils.js'

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

// LOW de la re-auditoria e2e (2026-09-08, segunda ronda): "todas las
// respuestas de error devuelven texto plano" -- GlobalExceptionHandler ahora
// devuelve JSON ({"message": "..."}). readErrorMessage ya intentaba
// parsear como JSON antes de este cambio (defensivo); estos tests fijan
// ese contrato para que una regresion futura (ej. volver a texto plano, o
// cambiar el nombre del campo) rompa un test en vez de degradar en
// silencio a "HTTP 400".
describe('readErrorMessage', () => {
  function fakeResponse(status, body) {
    return {
      status,
      text: async () => body,
    }
  }

  it('extrae el mensaje de un body JSON con campo message', async () => {
    const resp = fakeResponse(400, '{"message":"fechaHora debe ser una fecha futura"}')
    expect(await readErrorMessage(resp)).toBe('fechaHora debe ser una fecha futura')
  })

  it('cae al texto crudo si el body no es JSON', async () => {
    const resp = fakeResponse(400, 'texto plano sin comillas')
    expect(await readErrorMessage(resp)).toBe('texto plano sin comillas')
  })

  it('cae al status si el body esta vacio (ej. un 403 bloqueado por Spring Security)', async () => {
    const resp = fakeResponse(403, '')
    expect(await readErrorMessage(resp)).toBe('HTTP 403')
  })
})
