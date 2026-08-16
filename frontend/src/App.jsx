import React, {useEffect, useState} from 'react'

const TURNOS_API = 'http://localhost:8080/api/turnos'
const MEDICOS_API = 'http://localhost:8080/api/medicos'
const PACIENTES_API = 'http://localhost:8080/api/pacientes'

const ESTADO_BADGE = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  CANCELADO: 'badge-cancelado',
}

function EstadoBadge({estado}){
  return <span className={ESTADO_BADGE[estado] ?? 'badge bg-neutral-100 text-neutral-600'}>{estado}</span>
}

function MedicoForm({onCreado}){
  const [nombre, setNombre] = useState('')
  const [especialidad, setEspecialidad] = useState('')
  const [matricula, setMatricula] = useState('')
  const [telefono, setTelefono] = useState('')
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    setError(null)
    try{
      const resp = await fetch(MEDICOS_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, especialidad, matricula, telefono, email})
      })
      const data = await resp.json().catch(() => null)
      if(!resp.ok) throw new Error(typeof data === 'string' ? data : `HTTP ${resp.status}`)
      setNombre(''); setEspecialidad(''); setMatricula(''); setTelefono(''); setEmail('')
      await onCreado()
    }catch(err){
      setError(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
      <input className="input-field" placeholder="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <input className="input-field" placeholder="Especialidad" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} required />
      <input className="input-field" placeholder="Matrícula" value={matricula} onChange={e=>setMatricula(e.target.value)} required />
      <input className="input-field" placeholder="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <input className="input-field sm:col-span-2" placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      {error && <p className="sm:col-span-2 text-sm text-danger-600">Error: {error}</p>}
      <button type="submit" disabled={loading} className="btn-primary sm:col-span-2 sm:w-fit">
        {loading ? 'Guardando…' : 'Agregar médico'}
      </button>
    </form>
  )
}

function PacienteForm({onCreado}){
  const [nombre, setNombre] = useState('')
  const [dni, setDni] = useState('')
  const [telefono, setTelefono] = useState('')
  const [obraSocial, setObraSocial] = useState('')
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    setError(null)
    try{
      const resp = await fetch(PACIENTES_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({nombre, dni, telefono, obraSocial, email})
      })
      const data = await resp.json().catch(() => null)
      if(!resp.ok) throw new Error(typeof data === 'string' ? data : `HTTP ${resp.status}`)
      setNombre(''); setDni(''); setTelefono(''); setObraSocial(''); setEmail('')
      await onCreado()
    }catch(err){
      setError(err.message)
    }finally{
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
      <input className="input-field" placeholder="Nombre" value={nombre} onChange={e=>setNombre(e.target.value)} required />
      <input className="input-field" placeholder="DNI" value={dni} onChange={e=>setDni(e.target.value)} required />
      <input className="input-field" placeholder="Teléfono" value={telefono} onChange={e=>setTelefono(e.target.value)} />
      <input className="input-field" placeholder="Obra social" value={obraSocial} onChange={e=>setObraSocial(e.target.value)} />
      <input className="input-field sm:col-span-2" placeholder="Email" value={email} onChange={e=>setEmail(e.target.value)} />
      {error && <p className="sm:col-span-2 text-sm text-danger-600">Error: {error}</p>}
      <button type="submit" disabled={loading} className="btn-primary sm:col-span-2 sm:w-fit">
        {loading ? 'Guardando…' : 'Agregar paciente'}
      </button>
    </form>
  )
}

export default function App(){
  const [medicos, setMedicos] = useState([])
  const [pacientes, setPacientes] = useState([])

  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('Cardiología')
  const [medicoId, setMedicoId] = useState('')
  const [pacienteId, setPacienteId] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)

  const [turnos, setTurnos] = useState([])
  const [filtroMedicoId, setFiltroMedicoId] = useState('')
  const [filtroPacienteId, setFiltroPacienteId] = useState('')
  const [listLoading, setListLoading] = useState(false)
  const [listError, setListError] = useState(null)

  async function cargarMedicos(){
    const resp = await fetch(MEDICOS_API)
    if(resp.ok) setMedicos(await resp.json())
  }

  async function cargarPacientes(){
    const resp = await fetch(PACIENTES_API)
    if(resp.ok) setPacientes(await resp.json())
  }

  async function cargarTurnos(medicoIdParam = filtroMedicoId, pacienteIdParam = filtroPacienteId){
    setListLoading(true)
    setListError(null)
    try{
      const params = new URLSearchParams()
      if(medicoIdParam) params.set('medicoId', medicoIdParam)
      if(pacienteIdParam) params.set('pacienteId', pacienteIdParam)
      const url = params.toString() ? `${TURNOS_API}?${params}` : TURNOS_API
      const resp = await fetch(url)
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      setTurnos(await resp.json())
    }catch(err){
      setListError(err.message)
    }finally{
      setListLoading(false)
    }
  }

  useEffect(() => { cargarMedicos(); cargarPacientes(); cargarTurnos() }, [])

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    setResult(null)
    try{
      const resp = await fetch(TURNOS_API, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          fechaHora, especialidad, medicoId: Number(medicoId), pacienteId: Number(pacienteId)
        })
      })
      const data = await resp.json()
      setResult({ok: resp.ok, status: resp.status, body: data})
      if(resp.ok) await cargarTurnos()
    }catch(err){
      setResult({ok:false, error: err.message})
    }finally{setLoading(false)}
  }

  function handleFiltrar(e){
    e.preventDefault()
    cargarTurnos()
  }

  function nombreMedico(id){
    const medico = medicos.find(m => m.id === id)
    return medico ? medico.nombre : `#${id}`
  }

  function nombrePaciente(id){
    const paciente = pacientes.find(p => p.id === id)
    return paciente ? paciente.nombre : `#${id}`
  }

  return (
    <div className="min-h-screen bg-neutral-50 font-sans">
      <header className="bg-primary-800 text-white">
        <div className="max-w-3xl mx-auto px-6 py-4">
          <h1 className="text-xl font-semibold tracking-tight">MedConnect</h1>
          <p className="text-sm text-primary-100">Gestión de turnos</p>
        </div>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-8 space-y-8">
        <section className="card">
          <h2 className="heading mb-4">Médicos</h2>
          <MedicoForm onCreado={cargarMedicos} />
          <div className="overflow-x-auto rounded-lg border border-neutral-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-neutral-50 text-left text-neutral-500">
                  <th className="px-4 py-2 font-medium">ID</th>
                  <th className="px-4 py-2 font-medium">Nombre</th>
                  <th className="px-4 py-2 font-medium">Especialidad</th>
                  <th className="px-4 py-2 font-medium">Matrícula</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {medicos.map(m => (
                  <tr key={m.id} className="hover:bg-neutral-50">
                    <td className="px-4 py-2 text-neutral-500">{m.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.nombre}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.especialidad}</td>
                    <td className="px-4 py-2 text-neutral-900">{m.matricula}</td>
                  </tr>
                ))}
                {medicos.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-4 py-6 text-center text-neutral-400">
                      Sin médicos registrados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="card">
          <h2 className="heading mb-4">Pacientes</h2>
          <PacienteForm onCreado={cargarPacientes} />
          <div className="overflow-x-auto rounded-lg border border-neutral-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-neutral-50 text-left text-neutral-500">
                  <th className="px-4 py-2 font-medium">ID</th>
                  <th className="px-4 py-2 font-medium">Nombre</th>
                  <th className="px-4 py-2 font-medium">DNI</th>
                  <th className="px-4 py-2 font-medium">Obra social</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {pacientes.map(p => (
                  <tr key={p.id} className="hover:bg-neutral-50">
                    <td className="px-4 py-2 text-neutral-500">{p.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.nombre}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.dni}</td>
                    <td className="px-4 py-2 text-neutral-900">{p.obraSocial}</td>
                  </tr>
                ))}
                {pacientes.length === 0 && (
                  <tr>
                    <td colSpan={4} className="px-4 py-6 text-center text-neutral-400">
                      Sin pacientes registrados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="card">
          <h2 className="heading mb-4">Crear turno</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">Fecha y hora</label>
              <input className="input-field" value={fechaHora} onChange={e=>setFechaHora(e.target.value)} />
            </div>
            <div>
              <label className="label">Especialidad</label>
              <input className="input-field" value={especialidad} onChange={e=>setEspecialidad(e.target.value)} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Médico</label>
                <select className="input-field" value={medicoId} onChange={e=>setMedicoId(e.target.value)} required>
                  <option value="" disabled>Seleccionar médico</option>
                  {medicos.map(m => (
                    <option key={m.id} value={m.id}>{m.nombre} — {m.especialidad}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Paciente</label>
                <select className="input-field" value={pacienteId} onChange={e=>setPacienteId(e.target.value)} required>
                  <option value="" disabled>Seleccionar paciente</option>
                  {pacientes.map(p => (
                    <option key={p.id} value={p.id}>{p.nombre} — DNI {p.dni}</option>
                  ))}
                </select>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Enviando…' : 'Crear turno'}
            </button>
          </form>

          {result && (
            <div className={`mt-4 rounded-lg border px-4 py-3 text-sm ${
              result.ok
                ? 'border-success-200 bg-success-50 text-success-700'
                : 'border-danger-200 bg-danger-50 text-danger-700'
            }`}>
              {result.ok
                ? `Turno creado con id ${result.body?.id}.`
                : `Error (${result.status ?? '—'}): ${result.body ?? result.error}`}
            </div>
          )}
        </section>

        <section className="card">
          <h2 className="heading mb-4">Turnos</h2>
          <form onSubmit={handleFiltrar} className="flex flex-wrap items-end gap-3 mb-4">
            <div>
              <label className="label">Médico ID</label>
              <input type="number" className="input-field w-32" value={filtroMedicoId} onChange={e=>setFiltroMedicoId(e.target.value)} />
            </div>
            <div>
              <label className="label">Paciente ID</label>
              <input type="number" className="input-field w-32" value={filtroPacienteId} onChange={e=>setFiltroPacienteId(e.target.value)} />
            </div>
            <button type="submit" disabled={listLoading} className="btn-primary">
              {listLoading ? 'Buscando…' : 'Buscar'}
            </button>
            <button
              type="button"
              disabled={listLoading}
              onClick={() => { setFiltroMedicoId(''); setFiltroPacienteId(''); cargarTurnos('', '') }}
              className="btn-secondary"
            >
              Ver todos
            </button>
          </form>

          {listError && (
            <p className="mb-4 text-sm text-danger-600">Error: {listError}</p>
          )}

          <div className="overflow-x-auto rounded-lg border border-neutral-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-neutral-50 text-left text-neutral-500">
                  <th className="px-4 py-2 font-medium">ID</th>
                  <th className="px-4 py-2 font-medium">Fecha y hora</th>
                  <th className="px-4 py-2 font-medium">Especialidad</th>
                  <th className="px-4 py-2 font-medium">Médico</th>
                  <th className="px-4 py-2 font-medium">Paciente</th>
                  <th className="px-4 py-2 font-medium">Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-100">
                {turnos.map(t => (
                  <tr key={t.id} className="hover:bg-neutral-50">
                    <td className="px-4 py-2 text-neutral-500">{t.id}</td>
                    <td className="px-4 py-2 text-neutral-900">{t.fechaHora}</td>
                    <td className="px-4 py-2 text-neutral-900">{t.especialidad}</td>
                    <td className="px-4 py-2 text-neutral-900">{nombreMedico(t.medicoId)}</td>
                    <td className="px-4 py-2 text-neutral-900">{nombrePaciente(t.pacienteId)}</td>
                    <td className="px-4 py-2"><EstadoBadge estado={t.estado} /></td>
                  </tr>
                ))}
                {turnos.length === 0 && !listLoading && (
                  <tr>
                    <td colSpan={6} className="px-4 py-6 text-center text-neutral-400">
                      Sin turnos para mostrar.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  )
}
