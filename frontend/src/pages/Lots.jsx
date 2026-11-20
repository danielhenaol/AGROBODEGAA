import { useState, useEffect } from 'react'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'

// Inventario en bodega. Los lotes no se crean a mano: nacen al registrar una
// entrada y se descuentan con las salidas.
export default function Lots() {
  const getToken = useToken()
  const [lots, setLots] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [onlyAvailable, setOnlyAvailable] = useState(true)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      setError('')
      try {
        const token = await getToken()
        const res = await api.getLots(token, onlyAvailable)
        setLots(res.data || [])
      } catch (e) {
        console.error(e)
        setError(errorMessage(e))
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [getToken, onlyAvailable])

  const totalBultos = lots.reduce((sum, l) => sum + Number(l.quantity || 0), 0)

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Inventario por lotes</h1>
          <p>Existencias en bodega. Se actualizan solas con cada entrada y salida.</p>
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="badge badge-green">{lots.length} lotes · {totalBultos} bultos</span>
          <label style={{ display: 'flex', gap: 6, alignItems: 'center', fontSize: 14 }}>
            <input
              type="checkbox"
              checked={onlyAvailable}
              onChange={(e) => setOnlyAvailable(e.target.checked)}
            />
            Solo con existencias
          </label>
        </div>
      </div>

      {error && <div className="alert alert-danger" onClick={() => setError('')}>⚠ {error}</div>}

      <div className="card">
        {loading ? (
          <div className="card-body">
            <div className="empty-state">
              <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} />
            </div>
          </div>
        ) : lots.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <h3>No hay existencias en bodega</h3>
              <p>Registra una entrada y el lote aparecerá aquí automáticamente.</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Lote</th>
                  <th>Producto</th>
                  <th>Clasificación</th>
                  <th>Cosechero</th>
                  <th>Última entrada</th>
                  <th>Disponible</th>
                </tr>
              </thead>
              <tbody>
                {lots.map((l) => (
                  <tr key={l.id}>
                    <td>
                      <span className="tag tag-green">
                        {l.lotNumber ? `Lote ${String(l.lotNumber).padStart(2, '0')}` : '—'}
                      </span>
                    </td>
                    <td><strong>{l.productBase?.name || '—'}</strong></td>
                    <td>{l.classification?.name || '—'}</td>
                    <td>{l.farmer ? `${l.farmer.name} ${l.farmer.lastName}` : '—'}</td>
                    <td>{l.entryDate}</td>
                    <td><strong>{Number(l.quantity)} bultos</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
