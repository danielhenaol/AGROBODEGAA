import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

export default function Products() {
  const { getAccessTokenSilently } = useAuth0()

  const [products, setProducts] = useState([])
  const [classificationsMap, setClassificationsMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [showProductModal, setShowProductModal] = useState(false)
  const [showClassModal, setShowClassModal] = useState(false)
  const [selectedProduct, setSelectedProduct] = useState(null)
  const [editingProduct, setEditingProduct] = useState(null)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState('')
  const [productForm, setProductForm] = useState({ name: '' })
  const [classForm, setClassForm] = useState({ name: '' })

  const getToken = async () => {
    const isDemo = localStorage.getItem('agrobodega_demo') === 'true'

    if (isDemo) {
      return null
    }

    return await getAccessTokenSilently()
  }

  const load = async () => {
    setLoading(true)

    try {
      const token = await getToken()
      const res = await api.getProducts(token)
      const prods = res.data || []

      setProducts(prods)

      const classMap = {}

      await Promise.all(
        prods.map(async (p) => {
          try {
            const cr = await api.getClassifications(token, p.id)
            classMap[p.id] = cr.data || []
          } catch (error) {
            console.error('Error cargando clasificaciones:', error)
            classMap[p.id] = []
          }
        })
      )

      setClassificationsMap(classMap)
    } catch (error) {
      console.error('Error cargando productos:', error)
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const openCreate = () => {
    setEditingProduct(null)
    setProductForm({ name: '' })
    setShowProductModal(true)
  }

  const openEdit = (p) => {
    setEditingProduct(p)
    setProductForm({ name: p.name })
    setShowProductModal(true)
  }

  const handleProductSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)

    try {
      const token = await getToken()

      if (editingProduct) {
        await api.updateProduct(token, editingProduct.id, productForm)
        setSuccess('Producto actualizado exitosamente')
      } else {
        await api.createProduct(token, productForm)
        setSuccess('Producto registrado exitosamente')
      }

      setShowProductModal(false)
      setProductForm({ name: '' })
      await load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (error) {
      console.error('Error guardando producto:', error)
      alert('No se pudo guardar el producto. Revisa la consola.')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteProduct = async (id) => {
    if (!window.confirm('¿Seguro que deseas eliminar este producto?')) return

    try {
      const token = await getToken()
      await api.deleteProduct(token, id)

      setSuccess('Producto eliminado')
      await load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (error) {
      console.error('Error eliminando producto:', error)
      alert('No se pudo eliminar el producto. Revisa la consola.')
    }
  }

  const handleCreateClassification = async (e) => {
    e.preventDefault()
    setSaving(true)

    try {
      const token = await getToken()

      await api.createClassification(token, selectedProduct.id, classForm)

      setSuccess('Clasificación registrada exitosamente')
      setShowClassModal(false)
      setClassForm({ name: '' })
      await load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (error) {
      console.error('Error creando clasificación:', error)
      alert('No se pudo crear la clasificación. Revisa la consola.')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteClassification = async (productId, classId) => {
    if (!window.confirm('¿Seguro que deseas eliminar esta clasificación?')) return

    try {
      const token = await getToken()

      await api.deleteClassification(token, productId, classId)

      setSuccess('Clasificación eliminada')
      await load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (error) {
      console.error('Error eliminando clasificación:', error)
      alert('No se pudo eliminar la clasificación. Revisa la consola.')
    }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Catálogo de Productos</h1>
          <p>Productos base y sus clasificaciones</p>
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="badge badge-earth">{products.length} productos</span>

          <button className="btn btn-primary" onClick={openCreate}>
            <svg
              width="16"
              height="16"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              viewBox="0 0 24 24"
            >
              <path d="M12 5v14M5 12h14" />
            </svg>
            Nuevo producto
          </button>
        </div>
      </div>

      {success && <div className="alert alert-success">✓ {success}</div>}

      <div className="card">
        {loading ? (
          <div className="card-body">
            <div className="empty-state">
              <div
                className="loading-spinner"
                style={{ borderTopColor: '#38a15c', width: 32, height: 32 }}
              />
            </div>
          </div>
        ) : products.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <svg
                width="48"
                height="48"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                viewBox="0 0 24 24"
              >
                <path d="M20 12V8l-4-4H8L4 8v4M4 12h16M12 12v9" />
              </svg>
              <h3>No hay productos registrados</h3>
              <p>Registra el primer producto del catálogo</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Producto base</th>
                  <th>Clasificaciones</th>
                  <th>Acciones</th>
                </tr>
              </thead>

              <tbody>
                {products.map((p, i) => (
                  <tr key={p.id}>
                    <td style={{ color: '#9ca3af', fontSize: 12 }}>{i + 1}</td>

                    <td>
                      <strong>{p.name}</strong>
                    </td>

                    <td>
                      <div
                        style={{
                          display: 'flex',
                          flexWrap: 'wrap',
                          gap: 6,
                          alignItems: 'center',
                        }}
                      >
                        {(classificationsMap[p.id] || []).length === 0 ? (
                          <span style={{ color: '#9ca3af', fontSize: 12 }}>
                            Sin clasificaciones
                          </span>
                        ) : (
                          (classificationsMap[p.id] || []).map((c) => (
                            <span
                              key={c.id}
                              className="tag tag-earth"
                              style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 4,
                              }}
                            >
                              {c.name}

                              <button
                                onClick={() => handleDeleteClassification(p.id, c.id)}
                                style={{
                                  background: 'none',
                                  border: 'none',
                                  cursor: 'pointer',
                                  color: '#a07040',
                                  fontSize: 12,
                                  padding: 0,
                                  lineHeight: 1,
                                }}
                                title="Eliminar clasificación"
                              >
                                ✕
                              </button>
                            </span>
                          ))
                        )}

                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => {
                            setSelectedProduct(p)
                            setClassForm({ name: '' })
                            setShowClassModal(true)
                          }}
                        >
                          + Clasificación
                        </button>
                      </div>
                    </td>

                    <td>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => openEdit(p)}
                        >
                          Editar
                        </button>

                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => handleDeleteProduct(p.id)}
                        >
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showProductModal && (
        <div
          className="modal-overlay"
          onClick={(e) =>
            e.target === e.currentTarget && setShowProductModal(false)
          }
        >
          <div className="modal">
            <div className="modal-header">
              <div className="modal-title">
                {editingProduct ? 'Editar producto' : 'Registrar producto base'}
              </div>

              <button
                className="modal-close"
                onClick={() => setShowProductModal(false)}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleProductSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Nombre del producto *</label>

                  <input
                    type="text"
                    value={productForm.name}
                    onChange={(e) => setProductForm({ name: e.target.value })}
                    placeholder="Ej: Café, Cacao, Plátano"
                    required
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowProductModal(false)}
                >
                  Cancelar
                </button>

                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving
                    ? 'Guardando...'
                    : editingProduct
                    ? 'Actualizar'
                    : 'Registrar producto'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showClassModal && selectedProduct && (
        <div
          className="modal-overlay"
          onClick={(e) => e.target === e.currentTarget && setShowClassModal(false)}
        >
          <div className="modal">
            <div className="modal-header">
              <div className="modal-title">
                Nueva clasificación — {selectedProduct.name}
              </div>

              <button
                className="modal-close"
                onClick={() => setShowClassModal(false)}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateClassification}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Nombre de la clasificación *</label>

                  <input
                    type="text"
                    value={classForm.name}
                    onChange={(e) => setClassForm({ name: e.target.value })}
                    placeholder="Ej: Primera calidad, Segunda, Pasilla"
                    required
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setShowClassModal(false)}
                >
                  Cancelar
                </button>

                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Guardando...' : 'Registrar clasificación'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}