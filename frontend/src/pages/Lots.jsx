import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

export default function Lots() {
  const { getAccessTokenSilently } = useAuth0()
  const [lots, setLots] = useState([])
  const [farmers, setFarmers] = useState([])
  const [products, setProducts] = useState([])
  const [classifications, setClassifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState(false)
  const [form, setForm] = useState({
    farmerId: '', productBaseId: '', classificationId: '',
    quantity: '', entryDate: new Date().toISOString().split('T')[0]
  })

  const load = async () => {
    try {
      const token = await getAccessTokenSilently()
      const [l, f, p] = await Promise.all([
        api.getLots(token),
        api.getFarmers(token),
        api.getProducts(token),
      ])
      setLots(l.data || [])
      setFarmers(f.data || [])
      setProducts(p.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleProductChange = async (productId) => {
    setForm(f => ({ ...f, productBaseId: productId, classificationId: '' }))
    if (!productId) { setClassifications([]); return }
    try {
      const token = await getAccessTokenSilently()
      const res = await api.getClassifications(token, productId)
      setClassifications(res.data || [])
    } catch (e) { console.error(e) }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const token = await getAccessTokenSilently()
      await api.createLot(token, {
        farmerId: form.farmerId,
        productBaseId: form.productBaseId,
        classificationId: form.classificationId,
        quantity: parseFloat(form.quantity),
        entryDate: form.entryDate
      })
      setSuccess(true)
      setShowModal(false)
      setForm({ farmerId: '', productBaseId: '', classificationId: '', quantity: '', entryDate: new Date().toISOString().split('T')[0] })
      load()
      setTimeout(() => setSuccess(false), 3000)
    } catch (e) { console.error(e) }
    finally { setSaving(false) }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Lotes</h1>
          <p>Gestión de lotes de productos en bodega</p>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="badge badge-green">{lots.length} lotes</span>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
            Nuevo lote
          </button>
        </div>
      </div>

      {success && <div className="alert alert-success">✓ Lote registrado exitosamente</div>}

      <div className="card">
        {loading ? (
          <div className="card-body"><div className="empty-state"><div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} /></div></div>
        ) : lots.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                <path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z"/>
                <path d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16"/>
              </svg>
              <h3>No hay lotes registrados</h3>
              <p>Registra el primer lote de la bodega</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Fecha entrada</th>
                  <th>Cosechero</th>
                  <th>Cantidad (kg)</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {lots.map((l, i) => (
                  <tr key={l.id}>
                    <td style={{ color: '#9ca3af', fontSize: 12 }}>{i + 1}</td>
                    <td>{l.entryDate}</td>
                    <td>{l.farmer ? `${l.farmer.name} ${l.farmer.lastName}` : '—'}</td>
                    <td><strong>{l.quantity?.toFixed(2)} kg</strong></td>
                    <td><span className="tag tag-green">Activo</span></td>
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
              <div className="modal-title">Registrar lote</div>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-grid">
                  <div className="form-group">
                    <label>Cosechero *</label>
                    <select value={form.farmerId} onChange={e => setForm({...form, farmerId: e.target.value})} required>
                      <option value="">Seleccionar cosechero</option>
                      {farmers.map(f => <option key={f.id} value={f.id}>{f.name} {f.lastName}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Producto base *</label>
                    <select value={form.productBaseId} onChange={e => handleProductChange(e.target.value)} required>
                      <option value="">Seleccionar producto</option>
                      {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Clasificación *</label>
                    <select value={form.classificationId} onChange={e => setForm({...form, classificationId: e.target.value})} required disabled={!form.productBaseId}>
                      <option value="">Seleccionar clasificación</option>
                      {classifications.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Cantidad (kg) *</label>
                    <input type="number" step="0.01" min="0.01" value={form.quantity}
                      onChange={e => setForm({...form, quantity: e.target.value})} placeholder="0.00" required />
                  </div>
                  <div className="form-group full">
                    <label>Fecha de entrada *</label>
                    <input type="date" value={form.entryDate}
                      onChange={e => setForm({...form, entryDate: e.target.value})} required />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Registrar lote'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
