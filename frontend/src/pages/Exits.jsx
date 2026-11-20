import { useState, useEffect, useCallback } from 'react'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'

const today = () => new Date().toISOString().split('T')[0]
const emptyLine = { lotId: '', quantity: '', pricePerUnit: '' }

const lotLabel = (l) =>
  `${l.productBase?.name ?? 'Producto'} · ${l.classification?.name ?? ''} · ` +
  `${l.farmer ? `${l.farmer.name} ${l.farmer.lastName}` : 'Sin cosechero'}` +
  `${l.lotNumber ? ` · Lote ${String(l.lotNumber).padStart(2, '0')}` : ''} — ` +
  `disponibles: ${Number(l.quantity)} bultos`

const money = (n) =>
  `$${Number(n ?? 0).toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

export default function Exits() {
  const getToken = useToken()
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [traders, setTraders] = useState([])
  const [lots, setLots] = useState([])
  const [exits, setExits] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState(false)
  const [header, setHeader] = useState({ traderId: '', exitDate: today() })
  const [lines, setLines] = useState([{ ...emptyLine }])

  const load = useCallback(async () => {
    try {
      const token = await getToken()
      const [t, l, x] = await Promise.all([
        api.getTraders(token),
        api.getLots(token, true),   // solo lotes con existencias
        api.getExits(token),
      ])
      setTraders(t.data || [])
      setLots(l.data || [])
      setExits(x.data || [])
    } catch (e) {
      console.error(e)
      setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [getToken])

  useEffect(() => { load() }, [load])

  const lotById = (id) => lots.find((l) => l.id === id)
  const updateLine = (index, field, value) =>
    setLines((prev) => prev.map((l, i) => (i === index ? { ...l, [field]: value } : l)))
  const addLine = () => setLines((prev) => [...prev, { ...emptyLine }])
  const removeLine = (index) => lines.length > 1 && setLines((prev) => prev.filter((_, i) => i !== index))

  // Un lote no se repite en dos lineas de la misma salida
  const optionsFor = (index) =>
    lots.filter((l) => l.id === lines[index].lotId || !lines.some((x) => x.lotId === l.id))

  const totalBultos = lines.reduce((s, l) => s + (parseInt(l.quantity) || 0), 0)
  const totalValor = lines.reduce((s, l) => s + (parseInt(l.quantity) || 0) * (parseFloat(l.pricePerUnit) || 0), 0)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    for (const line of lines) {
      const lot = lotById(line.lotId)
      if (lot && parseInt(line.quantity) > Number(lot.quantity)) {
        setError(`Solo hay ${Number(lot.quantity)} bultos disponibles en: ${lotLabel(lot).split(' — ')[0]}`)
        return
      }
    }
    setSaving(true)
    try {
      const token = await getToken()
      await api.createExit(token, {
        traderId: header.traderId,
        exitDate: header.exitDate,
        items: lines.map((l) => ({
          lotId: l.lotId,
          quantity: parseInt(l.quantity),
          pricePerUnit: parseFloat(l.pricePerUnit),
        })),
      })
      setSuccess(true)
      setShowModal(false)
      setHeader({ traderId: '', exitDate: today() })
      setLines([{ ...emptyLine }])
      await load()
      setTimeout(() => setSuccess(false), 3000)
    } catch (err) {
      console.error(err)
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Salidas</h1>
          <p>Registro de salidas de productos de la bodega</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setError(''); setShowModal(true) }}>
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Nueva salida
        </button>
      </div>

      {success && <div className="alert alert-success">✓ Salida registrada exitosamente</div>}
      {error && !showModal && <div className="alert alert-danger" onClick={() => setError('')}>⚠ {error}</div>}

      <div className="card">
        {loading ? (
          <div className="card-body"><div className="empty-state"><div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} /></div></div>
        ) : exits.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                <path d="M12 19V5M5 12l7-7 7 7"/>
              </svg>
              <h3>Registra la primera salida</h3>
              <p>Las salidas de productos aparecerán aquí una vez registradas</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>Fecha</th><th>Negociante</th><th>Productos</th><th>Bultos</th><th>Total</th></tr>
              </thead>
              <tbody>
                {exits.map((x) => (
                  <tr key={x.id}>
                    <td>{x.exitDate?.toString()}</td>
                    <td>{x.traderName || '—'}</td>
                    <td>
                      {(x.items || []).map((it) => (
                        <div key={it.movementId} style={{ padding: '2px 0' }}>
                          <strong>{it.productName}</strong> · {it.classificationName}
                          <span style={{ color: '#6b7280' }}>
                            {' '}({it.farmerName}{it.lotNumber ? `, Lote ${String(it.lotNumber).padStart(2, '0')}` : ''})
                            {' '}— {it.quantity} bultos × {money(it.pricePerUnit)}
                          </span>
                        </div>
                      ))}
                    </td>
                    <td><strong>{x.totalQuantity}</strong></td>
                    <td><strong>{money(x.totalPrice)}</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal" style={{ maxWidth: 760 }}>
            <div className="modal-header">
              <div className="modal-title">Registrar salida</div>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && <div className="alert alert-danger">⚠ {error}</div>}
                <div className="form-grid" style={{ marginBottom: 20 }}>
                  <div className="form-group">
                    <label>Negociante *</label>
                    <select value={header.traderId} onChange={(e) => setHeader({ ...header, traderId: e.target.value })} required>
                      <option value="">Seleccionar negociante</option>
                      {traders.map((t) => <option key={t.id} value={t.id}>{t.name} {t.lastName}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Fecha de salida *</label>
                    <input type="date" value={header.exitDate} onChange={(e) => setHeader({ ...header, exitDate: e.target.value })} required />
                  </div>
                </div>

                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: 16 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <label style={{ fontWeight: 600, fontSize: 13 }}>Productos que salen</label>
                    <button type="button" className="btn btn-secondary btn-sm" onClick={addLine}
                            disabled={lines.length >= lots.length}>+ Agregar línea</button>
                  </div>

                  {lots.length === 0 && (
                    <div className="alert alert-danger">No hay existencias en bodega. Registra primero una entrada.</div>
                  )}

                  {lines.map((line, index) => {
                    const lot = lotById(line.lotId)
                    return (
                      <div key={index} style={{ display: 'grid', gridTemplateColumns: '2fr 90px 110px 32px', gap: 8, marginBottom: 10, alignItems: 'end' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          {index === 0 && <label style={{ fontSize: 11 }}>Lote *</label>}
                          <select value={line.lotId} onChange={(e) => updateLine(index, 'lotId', e.target.value)} required>
                            <option value="">Seleccionar lote</option>
                            {optionsFor(index).map((l) => <option key={l.id} value={l.id}>{lotLabel(l)}</option>)}
                          </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          {index === 0 && <label style={{ fontSize: 11 }}>Bultos *</label>}
                          <input type="number" step="1" min="1" max={lot ? lot.quantity : undefined}
                                 value={line.quantity} onChange={(e) => updateLine(index, 'quantity', e.target.value)}
                                 placeholder="0" required />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                          {index === 0 && <label style={{ fontSize: 11 }}>Precio/bulto *</label>}
                          <input type="number" step="0.01" min="0.01" value={line.pricePerUnit}
                                 onChange={(e) => updateLine(index, 'pricePerUnit', e.target.value)}
                                 placeholder="0.00" required />
                        </div>
                        <div style={{ display: 'flex', alignItems: 'flex-end', paddingBottom: 2 }}>
                          {lines.length > 1 && (
                            <button type="button" onClick={() => removeLine(index)}
                                    style={{ background: '#fee2e2', border: 'none', color: '#ef4444', borderRadius: 6, width: 28, height: 36, cursor: 'pointer', fontSize: 16 }}>✕</button>
                          )}
                        </div>
                      </div>
                    )
                  })}

                  <div style={{ textAlign: 'right', fontSize: 13, color: '#374151', marginTop: 8 }}>
                    Total: <strong>{totalBultos} bultos</strong> · <strong>{money(totalValor)}</strong>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving || lots.length === 0}>
                  {saving ? 'Guardando...' : 'Registrar salida'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
