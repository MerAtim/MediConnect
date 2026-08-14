import React, {useEffect, useState} from 'react'

const API_BASE = 'http://localhost:8080/api/turnos'

const ESTADO_BADGE = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  CANCELADO: 'badge-cancelado',
}

function EstadoBadge({estado}){
  return <span className={ESTADO_BADGE[estado] ?? 'badge bg-neutral-100 text-neutral-600'}>{estado}</span>
}

export default function App(){
  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('Cardiología')
  const [medicoId, setMedicoId] = useState(1)
  const [pacienteId, setPacienteId] = useState(1)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)

  const [turnos, setTurnos] = useState([])
  const [filtroMedicoId, setFiltroMedicoId] = useState('')
  const [filtroPacienteId, setFiltroPacienteId] = useState('')
  const [listLoading, setListLoading] = useState(false)
  const [listError, setListError] = useState(null)

  async function cargarTurnos(medicoIdParam = filtroMedicoId, pacienteIdParam = filtroPacienteId){
    setListLoading(true)
    setListError(null)
    try{
      const params = new URLSearchParams()
      if(medicoIdParam) params.set('medicoId', medicoIdParam)
      if(pacienteIdParam) params.set('pacienteId', pacienteIdParam)
      const url = params.toString() ? `${API_BASE}?${params}` : API_BASE
      const resp = await fetch(url)
      if(!resp.ok) throw new Error(`HTTP ${resp.status}`)
      setTurnos(await resp.json())
    }catch(err){
      setListError(err.message)
    }finally{
      setListLoading(false)
    }
  }

  useEffect(() => { cargarTurnos() }, [])

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    setResult(null)
    try{
      const resp = await fetch(API_BASE, {
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
                <label className="label">Médico ID</label>
                <input type="number" className="input-field" value={medicoId} onChange={e=>setMedicoId(e.target.value)} />
              </div>
              <div>
                <label className="label">Paciente ID</label>
                <input type="number" className="input-field" value={pacienteId} onChange={e=>setPacienteId(e.target.value)} />
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
                    <td className="px-4 py-2 text-neutral-900">{t.medicoId}</td>
                    <td className="px-4 py-2 text-neutral-900">{t.pacienteId}</td>
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
