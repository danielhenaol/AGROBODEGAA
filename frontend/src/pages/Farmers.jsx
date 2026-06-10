import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

export default function Farmers() {
  const { getAccessTokenSilently } = useAuth0()
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ name: '', lastName: '', phone: '' })

  const load = async () => {
    try {
      const token = await getAccessTokenSilently()
      const res = await api.getFarmers(token)
      setData(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const openCreate = () => { setEditing(null); setForm({ name: '', lastName: '', phone: '' }); setShowModal(true) }
  const openEdit = (f) => { setEditing(f); setForm({ name: f.name, lastName: f.lastName, phone: f.phone }); setShowModal(true) }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const token = await getAccessTokenSilently()
      if (editing) {
        await api.updateFarmer(token, editing.id, form)
        setSuccess('Cosechero actualizado exitosamente')
      } else {
        await api.createFarmer(token, form)
        setSuccess('Cosechero registrado exitosamente')
      }
      setShowModal(false)
      setForm({ name: '', lastName: '', phone: '' })
      load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (e) { console.error(e) }
    finally { setSaving(false) }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Seguro que deseas eliminar este cosechero?')) return
    try {
      const token = await getAccessTokenSilently()
      await api.deleteFarmer(token, id)
      setSuccess('Cosechero eliminado')
      load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (e) { console.error(e) }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Cosecheros</h1>
          <p>Productores agrícolas registrados en el sistema</p>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="badge badge-green">{data.length} registros</span>
          <button className="btn btn-primary" onClick={openCreate}>
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo cosechero
          </button>
        </div>
      </div>

      {success && <div className="alert alert-success">✓ {success}</div>}

      <div className="card">
        {loading ? (
          <div className="card-body"><div className="empty-state"><div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} /></div></div>
        ) : data.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
              <h3>No hay cosecheros registrados</h3>
              <p>Registra el primer cosechero del sistema</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead><tr><th>#</th><th>Nombre completo</th><th>Teléfono</th><th>Estado</th><th>Acciones</th></tr></thead>
              <tbody>
                {data.map((f, i) => (
                  <tr key={f.id}>
                    <td style={{ color: '#9ca3af', fontSize: 12 }}>{i + 1}</td>
                    <td><strong>{f.name} {f.lastName}</strong></td>
                    <td>{f.phone}</td>
                    <td><span className="tag tag-green">Activo</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(f)}>Editar</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(f.id)}>Eliminar</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <div className="modal-title">{editing ? 'Editar cosechero' : 'Registrar cosechero'}</div>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-grid">
                  <div className="form-group">
                    <label>Nombre *</label>
                    <input type="text" value={form.name} onChange={e => setForm({...form, name: e.target.value})} placeholder="Ej: Carlos" required />
                  </div>
                  <div className="form-group">
                    <label>Apellido *</label>
                    <input type="text" value={form.lastName} onChange={e => setForm({...form, lastName: e.target.value})} placeholder="Ej: Ramírez" required />
                  </div>
                  <div className="form-group full">
                    <label>Teléfono *</label>
                    <input type="text" value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} placeholder="Ej: 3001234567" required />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : editing ? 'Actualizar' : 'Registrar cosechero'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
