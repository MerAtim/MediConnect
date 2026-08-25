import { describe, test, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App.jsx'

const PAGE_VACIA = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }

function jsonResponse(body, ok = true, status = ok ? 200 : 400) {
  return {
    ok,
    status,
    json: async () => body,
    text: async () => (typeof body === 'string' ? body : JSON.stringify(body)),
  }
}

// Mock generico de fetch: resuelve segun el path de la URL para que el
// login y los fetch en cascada del useEffect de init no rompan el test.
function mockFetchPorDefecto(url) {
  if (url.includes('/api/medicos/especialidades')) return jsonResponse([])
  if (url.includes('/api/medicos/emails-vinculados')) return jsonResponse([])
  if (url.includes('/api/pacientes/emails-vinculados')) return jsonResponse([])
  if (url.includes('/api/medicos')) return jsonResponse(PAGE_VACIA)
  if (url.includes('/api/pacientes')) return jsonResponse(PAGE_VACIA)
  if (url.includes('/api/usuarios')) return jsonResponse([])
  if (url.includes('/api/turnos')) return jsonResponse(PAGE_VACIA)
  return jsonResponse([])
}

beforeEach(() => {
  localStorage.clear()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('LoginScreen', () => {
  test('renderiza el formulario de login cuando no hay sesion guardada', () => {
    render(<App />)

    expect(screen.getByText('Iniciá sesión para continuar')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
  })

  test('muestra un mensaje de error si el login falla', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', vi.fn(async (url) => {
      if (String(url).includes('/api/auth/login')) {
        return jsonResponse('email o contraseña incorrectos', false, 401)
      }
      return mockFetchPorDefecto(String(url))
    }))

    render(<App />)

    await user.type(screen.getByLabelText('Email'), 'ana@medconnect.com')
    await user.type(screen.getByLabelText('Contraseña'), 'mala')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('email o contraseña incorrectos')).toBeInTheDocument()
    // Sigue en la pantalla de login, no navego a la app principal.
    expect(screen.getByText('Iniciá sesión para continuar')).toBeInTheDocument()
  })

  test('loguea correctamente y muestra la app principal segun el rol', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', vi.fn(async (url) => {
      const u = String(url)
      if (u.includes('/api/auth/login')) {
        // El JWT viaja en una cookie httpOnly (Set-Cookie), no en el body.
        return jsonResponse({
          id: 1,
          nombre: 'Admin Test',
          email: 'admin@medconnect.com',
          role: 'ADMINISTRADOR',
        })
      }
      return mockFetchPorDefecto(u)
    }))

    render(<App />)

    await user.type(screen.getByLabelText('Email'), 'admin@medconnect.com')
    await user.type(screen.getByLabelText('Contraseña'), 'secreto123')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Turnos' })).toBeInTheDocument()
    })
    // Seccion exclusiva de ADMINISTRADOR: confirma que el rol se propago bien.
    expect(screen.getByRole('heading', { name: 'Usuarios' })).toBeInTheDocument()
    expect(localStorage.getItem('medconnect_auth')).toContain('admin@medconnect.com')
  })
})
