import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

export default function Exits() {
  const { getAccessTokenSilently } = useAuth0()
  const [showModal, setShowModal] = useState(false)
  const [traders, setTraders] = useState([])
  const [lots, setLots] = useState([])
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [form, setForm] = useState({
    traderId: '', lotId: '', quantity: '', pricePerUnit: '', exitDate: new Date().toISOString().split('T')[0]
  })

  useEffect(() => {
    const load = async () => {
      try {
        const token = await getAccessTokenSilently()
        const [t, l] = await Promise.all([api.getTraders(token), api.getLots(token)])
        setTraders(t.data || [])
        setLots(l.data || [])
      } catch (e) { console.error(e) }
    }
    load()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const token = await getAccessTokenSilently()
      await api.createExit(token, {
        traderId: form.traderId,
        lotId: form.lotId,
        quantity: parseFloat(form.quantity),
        pricePerUnit: parseFloat(form.pricePerUnit),
        exitDate: form.exitDate
      })
      setSuccess(true)
      setShowModal(false)
      setForm({ traderId: '', lotId: '', quantity: '', pricePerUnit: '', exitDate: new Date().toISOString().split('T')[0] })
      setTimeout(() => setSuccess(false), 3000)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Salidas</h1>
          <p>Registro de salidas de productos de la bodega</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Nueva salida
        </button>
      </div>

      {success && (
        <div className="alert alert-success">
          ✓ Salida registrada exitosamente en el sistema
        </div>
      )}

      <div className="card">
        <div className="card-body">
          <div className="empty-state">
            <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
              <path d="M12 19V5M5 12l7-7 7 7"/>
            </svg>
            <h3>Registra la primera salida</h3>
            <p>Las salidas de productos aparecerán aquí una vez registradas</p>
          </div>
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <div className="modal-title">Registrar salida</div>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-grid">
                  <div className="form-group">
                    <label>Negociante</label>
                    <select value={form.traderId} onChange={e => setForm({...form, traderId: e.target.value})} required>
                      <option value="">Seleccionar negociante</option>
                      {traders.map(t => (
                        <option key={t.id} value={t.id}>{t.name} {t.lastName}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Lote</label>
                    <select value={form.lotId} onChange={e => setForm({...form, lotId: e.target.value})} required>
                      <option value="">Seleccionar lote</option>
                      {lots.map(l => (
                        <option key={l.id} value={l.id}>Lote #{l.id?.slice(0,8)}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Cantidad (kg)</label>
                    <input type="number" step="0.01" min="0" value={form.quantity}
                      onChange={e => setForm({...form, quantity: e.target.value})} placeholder="0.00" required />
                  </div>
                  <div className="form-group">
                    <label>Precio por unidad ($)</label>
                    <input type="number" step="0.01" min="0" value={form.pricePerUnit}
                      onChange={e => setForm({...form, pricePerUnit: e.target.value})} placeholder="0.00" required />
                  </div>
                  <div className="form-group">
                    <label>Fecha de salida</label>
                    <input type="date" value={form.exitDate}
                      onChange={e => setForm({...form, exitDate: e.target.value})} required />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Guardando...' : 'Registrar salida'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
