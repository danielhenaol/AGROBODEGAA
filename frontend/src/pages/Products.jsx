import { useEffect, useState, useCallback } from 'react'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'

export default function Products() {
  const getToken = useToken()
  const [products, setProducts] = useState([])
  const [classes, setClasses] = useState({}) // { productId: [clasificaciones] }
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [newName, setNewName] = useState('')
  const [newClass, setNewClass] = useState({}) // { productId: texto }
  const [saving, setSaving] = useState(false)

  const flash = (msg) => {
    setSuccess(msg)
    setTimeout(() => setSuccess(''), 3000)
  }

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const token = await getToken()
      const res = await api.getProducts(token)
      const list = Array.isArray(res.data) ? res.data : []
      setProducts(list)
      const entries = await Promise.all(
        list.map(async (p) => [p.id, (await api.getClassifications(token, p.id)).data || []])
      )
      setClasses(Object.fromEntries(entries))
    } catch (e) {
      console.error(e)
      setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [getToken])

  useEffect(() => { load() }, [load])

  const run = async (action, okMsg) => {
    setSaving(true)
    setError('')
    try {
      const token = await getToken()
      await action(token)
      flash(okMsg)
      await load()
    } catch (e) {
      console.error(e)
      setError(errorMessage(e))
    } finally {
      setSaving(false)
    }
  }

  const addProduct = (e) => {
    e.preventDefault()
    run((t) => api.createProduct(t, { name: newName.trim() }), 'Producto registrado')
    setNewName('')
  }

  const removeProduct = (p) => {
    if (!window.confirm(`¿Eliminar el producto "${p.name}"?`)) return
    run((t) => api.deleteProduct(t, p.id), 'Producto eliminado')
  }

  const addClass = (p) => {
    const name = (newClass[p.id] || '').trim()
    if (!name) return
    run((t) => api.createClassification(t, p.id, { name }), 'Clasificación agregada')
    setNewClass({ ...newClass, [p.id]: '' })
  }

  const removeClass = (p, c) =>
    run((t) => api.deleteClassification(t, p.id, c.id), 'Clasificación eliminada')

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Catálogo de Productos</h1>
          <p>Productos base y sus clasificaciones de calidad</p>
        </div>
        <span className="badge badge-earth">{products.length} productos</span>
      </div>

      {success && <div className="alert alert-success">✓ {success}</div>}
      {error && <div className="alert alert-danger" onClick={() => setError('')}>⚠ {error}</div>}

      <div className="card" style={{ marginBottom: 16 }}>
        <div className="card-body">
          <form onSubmit={addProduct} style={{ display: 'flex', gap: 10 }}>
            <input
              type="text"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder="Nuevo producto (Ej: Zanahoria)"
              required
              style={{ flex: 1 }}
            />
            <button type="submit" className="btn btn-primary" disabled={saving}>
              Registrar producto
            </button>
          </form>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="card-body">
            <div className="empty-state">
              <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} />
              <p>Cargando productos... (la primera vez puede tardar mientras despierta el servidor)</p>
            </div>
          </div>
        ) : products.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <h3>No hay productos registrados</h3>
              <p>Registra el primero con el formulario de arriba</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr><th>#</th><th>Producto</th><th>Clasificaciones</th><th>Acciones</th></tr>
              </thead>
              <tbody>
                {products.map((p, i) => (
                  <tr key={p.id}>
                    <td>{i + 1}</td>
                    <td><strong>{p.name}</strong></td>
                    <td>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, alignItems: 'center' }}>
                        {(classes[p.id] || []).map((c) => (
                          <span key={c.id} className="tag tag-green">
                            {c.name}{' '}
                            <button
                              type="button"
                              title="Quitar"
                              onClick={() => removeClass(p, c)}
                              style={{ border: 'none', background: 'none', cursor: 'pointer' }}
                            >✕</button>
                          </span>
                        ))}
                        <input
                          type="text"
                          value={newClass[p.id] || ''}
                          onChange={(e) => setNewClass({ ...newClass, [p.id]: e.target.value })}
                          onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addClass(p))}
                          placeholder="Nueva clasificación"
                          style={{ width: 170 }}
                        />
                        <button type="button" className="btn btn-secondary btn-sm" onClick={() => addClass(p)}>
                          Agregar
                        </button>
                      </div>
                    </td>
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => removeProduct(p)}>Eliminar</button>
                    </td>
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
