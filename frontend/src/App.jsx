import React, {useEffect, useState} from 'react'

const API_BASE = 'http://localhost:8080/api/turnos'

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
    <div style={{padding:20,fontFamily:'Arial',maxWidth:700}}>
      <h2>Crear Turno (demo)</h2>
      <form onSubmit={handleSubmit}>
        <div style={{marginBottom:8}}>
          <label>Fecha y hora:</label><br/>
          <input value={fechaHora} onChange={e=>setFechaHora(e.target.value)} style={{width:'100%'}} />
        </div>
        <div style={{marginBottom:8}}>
          <label>Especialidad:</label><br/>
          <input value={especialidad} onChange={e=>setEspecialidad(e.target.value)} style={{width:'100%'}} />
        </div>
        <div style={{display:'flex',gap:8,marginBottom:8}}>
          <div style={{flex:1}}>
            <label>Medico ID:</label><br/>
            <input type="number" value={medicoId} onChange={e=>setMedicoId(e.target.value)} style={{width:'100%'}} />
          </div>
          <div style={{flex:1}}>
            <label>Paciente ID:</label><br/>
            <input type="number" value={pacienteId} onChange={e=>setPacienteId(e.target.value)} style={{width:'100%'}} />
          </div>
        </div>
        <button type="submit" disabled={loading}>{loading ? 'Enviando...' : 'Crear turno'}</button>
      </form>

      {result && (
        <div style={{marginTop:16,padding:12,border:'1px solid #ddd'}}>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}

      <h2 style={{marginTop:32}}>Turnos</h2>
      <form onSubmit={handleFiltrar} style={{display:'flex',gap:8,alignItems:'flex-end',marginBottom:12}}>
        <div>
          <label>Filtrar por medico ID:</label><br/>
          <input type="number" value={filtroMedicoId} onChange={e=>setFiltroMedicoId(e.target.value)} />
        </div>
        <div>
          <label>Filtrar por paciente ID:</label><br/>
          <input type="number" value={filtroPacienteId} onChange={e=>setFiltroPacienteId(e.target.value)} />
        </div>
        <button type="submit" disabled={listLoading}>{listLoading ? 'Buscando...' : 'Buscar'}</button>
        <button type="button" disabled={listLoading} onClick={() => { setFiltroMedicoId(''); setFiltroPacienteId(''); cargarTurnos('', '') }}>Ver todos</button>
      </form>

      {listError && <p style={{color:'red'}}>Error: {listError}</p>}

      <table style={{width:'100%',borderCollapse:'collapse'}}>
        <thead>
          <tr style={{textAlign:'left',borderBottom:'1px solid #ddd'}}>
            <th>ID</th><th>Fecha y hora</th><th>Especialidad</th><th>Medico</th><th>Paciente</th><th>Estado</th>
          </tr>
        </thead>
        <tbody>
          {turnos.map(t => (
            <tr key={t.id} style={{borderBottom:'1px solid #eee'}}>
              <td>{t.id}</td>
              <td>{t.fechaHora}</td>
              <td>{t.especialidad}</td>
              <td>{t.medicoId}</td>
              <td>{t.pacienteId}</td>
              <td>{t.estado}</td>
            </tr>
          ))}
          {turnos.length === 0 && !listLoading && (
            <tr><td colSpan={6} style={{padding:8,color:'#888'}}>Sin turnos para mostrar.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
