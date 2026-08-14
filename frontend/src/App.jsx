import React, {useState} from 'react'

export default function App(){
  const [fechaHora, setFechaHora] = useState('2026-08-12T10:00:00')
  const [especialidad, setEspecialidad] = useState('Cardiología')
  const [medicoId, setMedicoId] = useState(1)
  const [pacienteId, setPacienteId] = useState(1)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e){
    e.preventDefault()
    setLoading(true)
    setResult(null)
    try{
      const resp = await fetch('http://localhost:8080/api/turnos', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          fechaHora, especialidad, medicoId: Number(medicoId), pacienteId: Number(pacienteId)
        })
      })
      const data = await resp.json()
      setResult({ok: resp.ok, status: resp.status, body: data})
    }catch(err){
      setResult({ok:false, error: err.message})
    }finally{setLoading(false)}
  }

  return (
    <div style={{padding:20,fontFamily:'Arial',maxWidth:600}}>
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
    </div>
  )
}
