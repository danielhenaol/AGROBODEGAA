import { useEffect, useState } from 'react'

const PRODUCTS_URL = '/api/v1/products'

export default function Products() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadProducts = async () => {
    setLoading(true)
    setError('')

    try {
      const response = await fetch(`${PRODUCTS_URL}?t=${Date.now()}`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error(`Error HTTP ${response.status}`)
      }

      const data = await response.json()

      console.log('Productos recibidos:', data)

      if (Array.isArray(data)) {
        setProducts(data)
      } else {
        setProducts([])
        setError('La respuesta no es una lista de productos')
      }
    } catch (err) {
      console.error('Error cargando productos:', err)
      setProducts([])
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProducts()
  }, [])

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Catálogo de Productos</h1>
          <p>Productos base cargados desde Cloudflare Pages → Kong Gateway</p>
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <span className="badge badge-earth">{products.length} productos</span>

          <button className="btn btn-primary" onClick={loadProducts}>
            Recargar productos
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger">
          Error cargando productos: {error}
        </div>
      )}

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
              <p>No se recibieron productos desde la API</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Producto</th>
                  <th>Descripción</th>
                  <th>Origen</th>
                </tr>
              </thead>

              <tbody>
                {products.map((product, index) => (
                  <tr key={product.id || index}>
                    <td>{index + 1}</td>

                    <td>
                      <strong>{product.name}</strong>
                    </td>

                    <td>{product.description || 'Sin descripción'}</td>

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
    </div>
  )
}