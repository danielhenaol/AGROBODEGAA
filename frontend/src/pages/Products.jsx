import { useEffect, useState } from 'react'

const PRODUCTS_URL =
  'https://90a76c5879.us.serverless.gateways.konggateway.com/api/v1/products'

export default function Products() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [success, setSuccess] = useState('')
  const [showProductModal, setShowProductModal] = useState(false)
  const [saving, setSaving] = useState(false)
  const [productForm, setProductForm] = useState({
    name: '',
    description: '',
  })

  const load = async () => {
    setLoading(true)

    try {
      const response = await fetch(PRODUCTS_URL, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`)
      }

      const data = await response.json()
      console.log('Productos recibidos desde Kong:', data)

      setProducts(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Error cargando productos desde Kong:', error)
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const openCreate = () => {
    setProductForm({
      name: '',
      description: '',
    })
    setShowProductModal(true)
  }

  const handleProductSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)

    try {
      const response = await fetch(PRODUCTS_URL, {
        method: 'POST',
        headers: {
          Accept: 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(productForm),
      })

      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`)
      }

      setSuccess('Producto registrado exitosamente')
      setShowProductModal(false)
      setProductForm({
        name: '',
        description: '',
      })

      await load()
      setTimeout(() => setSuccess(''), 3000)
    } catch (error) {
      console.error('Error registrando producto:', error)
      alert('No se pudo registrar el producto. Revisa la consola.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Catálogo de Productos</h1>
          <p>Productos base registrados desde Kong Gateway</p>
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
                style={{
                  borderTopColor: '#38a15c',
                  width: 32,
                  height: 32,
                }}
              />
              <p>Cargando productos...</p>
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
                  <th>Descripción</th>
                  <th>Origen</th>
                </tr>
              </thead>

              <tbody>
                {products.map((p, i) => (
                  <tr key={p.id || i}>
                    <td style={{ color: '#9ca3af', fontSize: 12 }}>{i + 1}</td>

                    <td>
                      <strong>{p.name}</strong>
                    </td>

                    <td>{p.description || 'Sin descripción'}</td>

                    <td>
                      <span className="tag tag-earth">Kong Gateway</span>
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
              <div className="modal-title">Registrar producto base</div>

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
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        name: e.target.value,
                      })
                    }
                    placeholder="Ej: Café, Cacao, Plátano"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Descripción</label>

                  <input
                    type="text"
                    value={productForm.description}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        description: e.target.value,
                      })
                    }
                    placeholder="Ej: Producto fresco"
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
                  {saving ? 'Guardando...' : 'Registrar producto'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}