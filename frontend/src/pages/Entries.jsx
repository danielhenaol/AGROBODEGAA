import { useState, useEffect, useCallback } from 'react'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'

const emptyLine = { productBaseId: '', classificationId: '', quantity: '', pricePerUnit: '' }

export default function Entries() {
  const getAccessTokenSilently = useToken()
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [farmers, setFarmers] = useState([])
  const [products, setProducts] = useState([])
  const [classMap, setClassMap] = useState({})
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState(false)
  const [sseStatus, setSseStatus] = useState('Conectando...')
  const [header, setHeader] = useState({
    farmerId: '',
    lotNumber: '',
    entryDate: new Date().toISOString().split('T')[0]
  })
  const [lines, setLines] = useState([{ ...emptyLine }])

  const loadData = useCallback(async () => {
    try {
      const token = await getAccessTokenSilently()
      const [f, p, e] = await Promise.all([
        api.getFarmers(token),
        api.getProducts(token),
        api.getEntries(token),
      ])
      setFarmers(f.data || [])
      setProducts(p.data || [])
      setEntries(e.data || [])
    } catch (err) {
      console.error(err); setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [getAccessTokenSilently])

  useEffect(() => { loadData() }, [loadData])

  // SSE — escucha actualizaciones en tiempo real
  useEffect(() => {
    const backendUrl = import.meta.env.VITE_API_URL || 'https://agrobodega-gateway.onrender.com'
    const eventSource = new EventSource(`${backendUrl}/api/v1/inventory/stream`)

    eventSource.addEventListener('inventory-update', (event) => {
      setSseStatus('🟢 Conectado')
      if (event.data !== 'Conectado al stream de inventario AGROBODEGA') {
        setTimeout(() => loadData(), 500)
      }
    })

    eventSource.onerror = () => {
      setSseStatus('🔴 Desconectado')
    }

    return () => eventSource.close()
  }, [loadData])

  const loadClassifications = async (productId) => {
    if (!productId || classMap[productId]) return
    try {
      const token = await getAccessTokenSilently()
      const res = await api.getClassifications(token, productId)
      setClassMap(prev => ({ ...prev, [productId]: res.data || [] }))
    } catch (e) { console.error(e); setError(errorMessage(e)) }
  }

  const updateLine = (index, field, value) => {
    setLines(prev => prev.map((l, i) => i === index ? { ...l, [field]: value, ...(field === 'productBaseId' ? { classificationId: '' } : {}) } : l))
    if (field === 'productBaseId') loadClassifications(value)
  }

  const addLine = () => setLines(prev => [...prev, { ...emptyLine }])
  const removeLine = (index) => { if (lines.length > 1) setLines(prev => prev.filter((_, i) => i !== index)) }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const token = await getAccessTokenSilently()
      await api.createEntry(token, {
        farmerId: header.farmerId,
        lotNumber: parseInt(header.lotNumber),
        entryDate: header.entryDate,
        items: lines.map(line => ({
          productBaseId: line.productBaseId,
          classificationId: line.classificationId,
          quantity: parseInt(line.quantity),
          pricePerUnit: parseFloat(line.pricePerUnit),
        })),
      })
      setSuccess(true)
      setShowModal(false)
      setHeader({ farmerId: '', lotNumber: '', entryDate: new Date().toISOString().split('T')[0] })
      setLines([{ ...emptyLine }])
      setTimeout(() => setSuccess(false), 3000)
      setTimeout(() => loadData(), 800)
    } catch (e) {
      console.error(e); setError(errorMessage(e))
    } finally {
      setSaving(false)
    }
  }

  const money = (n) => `$${Number(n ?? 0).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

  const totalBultos = lines.reduce((sum, l) => sum + (parseInt(l.quantity) || 0), 0)
  const totalValor = lines.reduce((sum, l) => sum + (parseInt(l.quantity) || 0) * (parseFloat(l.pricePerUnit) || 0), 0)

  return (
      <div>
        <div className="page-header">
          <div className="page-header-left">
            <h1>Entradas</h1>
            <p>Registro de entradas de productos a la bodega</p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 12, color: '#6b7280' }}>{sseStatus}</span>
            <button className="btn btn-primary" onClick={() => { setError(''); setShowModal(true) }}>
              <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              Nueva entrada
            </button>
          </div>
        </div>

        {success && (
            <div className="alert alert-success">✓ Entrada registrada exitosamente</div>
        )}

        {error && <div className="alert alert-danger" onClick={() => setError('')}>⚠ {error}</div>}

      <div className="card">
          {loading ? (
              <div className="card-body"><div className="empty-state"><div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} /></div></div>
          ) : entries.length === 0 ? (
              <div className="card-body">
                <div className="empty-state">
                  <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                    <path d="M12 5v14M5 12l7 7 7-7"/>
                  </svg>
                  <h3>Registra la primera entrada</h3>
                  <p>Las entradas de productos aparecerán aquí</p>
                </div>
              </div>
          ) : (
              <div className="table-container">
                <table>
                  <thead>
                  <tr><th>Fecha</th><th>Cosechero</th><th>Lote</th><th>Productos</th><th>Bultos</th><th>Total</th></tr>
                  </thead>
                  <tbody>
                  {entries.map((e) => (
                      <tr key={e.id}>
                        <td>{e.entryDate?.toString()}</td>
                        <td>{e.farmerName || '—'}</td>
                        <td><span className="tag tag-green">Lote {String(e.lotNumber).padStart(2,'0')}</span></td>
                        <td>
                          {(e.items || []).map((it) => (
                              <div key={it.movementId} style={{ padding: '2px 0' }}>
                                <strong>{it.productName}</strong> · {it.classificationName}
                                <span style={{ color: '#6b7280' }}> — {it.quantity} bultos × {money(it.pricePerUnit)}</span>
                              </div>
                          ))}
                        </td>
                        <td><strong>{e.totalQuantity}</strong></td>
                        <td><strong>{money(e.totalPrice)}</strong></td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
          )}
        </div>

        {showModal && (
            <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
              <div className="modal" style={{ maxWidth: 680 }}>
                <div className="modal-header">
                  <div className="modal-title">Registrar entrada</div>
                  <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
                </div>
                <form onSubmit={handleSubmit}>
                  <div className="modal-body">
                    {error && <div className="alert alert-danger">⚠ {error}</div>}
                    <div className="form-grid" style={{ marginBottom: 20 }}>
                      <div className="form-group">
                        <label>Cosechero *</label>
                        <select value={header.farmerId} onChange={e => setHeader({...header, farmerId: e.target.value})} required>
                          <option value="">Seleccionar cosechero</option>
                          {farmers.map(f => <option key={f.id} value={f.id}>{f.name} {f.lastName}</option>)}
                        </select>
                      </div>
                      <div className="form-group">
                        <label>Número de lote *</label>
                        <select value={header.lotNumber} onChange={e => setHeader({...header, lotNumber: e.target.value})} required>
                          <option value="">Seleccionar lote</option>
                          {[1,2,3,4,5,6,7,8,9,10].map(n => <option key={n} value={n}>Lote {String(n).padStart(2,'0')}</option>)}
                        </select>
                      </div>
                      <div className="form-group full">
                        <label>Fecha de entrada *</label>
                        <input type="date" value={header.entryDate} onChange={e => setHeader({...header, entryDate: e.target.value})} required />
                      </div>
                    </div>
                    <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: 16 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                        <label style={{ fontWeight: 600, fontSize: 13 }}>Productos</label>
                        <button type="button" className="btn btn-secondary btn-sm" onClick={addLine}>+ Agregar línea</button>
                      </div>
                      {lines.map((line, index) => (
                          <div key={index} style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1fr) 80px 100px 32px', gap: 8, marginBottom: 10, alignItems: 'end' }}>
                            <div className="form-group" style={{ marginBottom: 0 }}>
                              {index === 0 && <label style={{ fontSize: 11 }}>Producto *</label>}
                              <select value={line.productBaseId} onChange={e => updateLine(index, 'productBaseId', e.target.value)} required>
                                <option value="">Producto</option>
                                {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                              </select>
                            </div>
                            <div className="form-group" style={{ marginBottom: 0 }}>
                              {index === 0 && <label style={{ fontSize: 11 }}>Clasificación *</label>}
                              <select value={line.classificationId} onChange={e => updateLine(index, 'classificationId', e.target.value)} required disabled={!line.productBaseId}>
                                <option value="">Clasificación</option>
                                {(classMap[line.productBaseId] || []).map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                              </select>
                            </div>
                            <div className="form-group" style={{ marginBottom: 0 }}>
                              {index === 0 && <label style={{ fontSize: 11 }}>Bultos *</label>}
                              <input type="number" step="1" min="1" value={line.quantity} onChange={e => updateLine(index, 'quantity', e.target.value)} placeholder="0" required />
                            </div>
                            <div className="form-group" style={{ marginBottom: 0 }}>
                              {index === 0 && <label style={{ fontSize: 11 }}>Precio/bulto *</label>}
                              <input type="number" step="0.01" min="0" value={line.pricePerUnit} onChange={e => updateLine(index, 'pricePerUnit', e.target.value)} placeholder="0.00" required />
                            </div>
                            <div style={{ display: 'flex', alignItems: 'flex-end', paddingBottom: 2 }}>
                              {lines.length > 1 && (
                                  <button type="button" onClick={() => removeLine(index)}
                                          style={{ background: '#fee2e2', border: 'none', color: '#ef4444', borderRadius: 6, width: 28, height: 36, cursor: 'pointer', fontSize: 16 }}>✕</button>
                              )}
                            </div>
                          </div>
                      ))}
                      <div style={{ textAlign: 'right', fontSize: 13, color: '#374151', marginTop: 8 }}>
                        Total: <strong>{totalBultos} bultos</strong> · <strong>{money(totalValor)}</strong>
                      </div>
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                    <button type="submit" className="btn btn-primary" disabled={saving}>
                      {saving ? 'Guardando...' : 'Registrar entrada'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  )
}