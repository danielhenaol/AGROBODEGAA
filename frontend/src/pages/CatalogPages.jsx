import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

function DataPage({ title, subtitle, fetchFn, columns, EmptyIcon, emptyText }) {
  const { getAccessTokenSilently } = useAuth0()
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const token = await getAccessTokenSilently()
        const res = await fetchFn(token)
        setData(res.data || [])
      } catch (e) {
        console.error(e)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </div>
        <span className="badge badge-green">{data.length} registros</span>
      </div>

      <div className="card">
        {loading ? (
          <div className="card-body">
            <div className="empty-state">
              <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} />
            </div>
          </div>
        ) : data.length === 0 ? (
          <div className="card-body">
            <div className="empty-state">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
                <EmptyIcon />
              </svg>
              <h3>No hay registros</h3>
              <p>{emptyText}</p>
            </div>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  {columns.map(c => <th key={c.key}>{c.label}</th>)}
                </tr>
              </thead>
              <tbody>
                {data.map((row, i) => (
                  <tr key={row.id || i}>
                    {columns.map(c => (
                      <td key={c.key}>
                        {c.render ? c.render(row) : row[c.key] || '—'}
                      </td>
                    ))}
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

const LotsIcon = () => (
  <>
    <path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z"/>
    <path d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16"/>
  </>
)

const FarmersIcon = () => (
  <>
    <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
    <circle cx="9" cy="7" r="4"/>
  </>
)

const TradersIcon = () => (
  <>
    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </>
)

const ProductsIcon = () => (
  <>
    <path d="M20 12V8l-4-4H8L4 8v4M4 12h16M12 12v9"/>
  </>
)

export function Lots() {
  return (
    <DataPage
      title="Lotes"
      subtitle="Gestión de lotes de productos en bodega"
      fetchFn={(token) => api.getLots(token)}
      columns={[
        { key: 'id', label: 'ID', render: r => r.id?.slice(0,8) + '...' },
        { key: 'entryDate', label: 'Fecha entrada' },
        { key: 'quantity', label: 'Cantidad (kg)', render: r => r.quantity?.toFixed(2) },
        { key: 'status', label: 'Estado', render: () => <span className="tag tag-green">Activo</span> },
      ]}
      EmptyIcon={LotsIcon}
      emptyText="Los lotes registrados aparecerán aquí"
    />
  )
}

export function Farmers() {
  return (
    <DataPage
      title="Cosecheros"
      subtitle="Productores agrícolas registrados en el sistema"
      fetchFn={(token) => api.getFarmers(token)}
      columns={[
        { key: 'name', label: 'Nombre', render: r => `${r.name} ${r.lastName}` },
        { key: 'phone', label: 'Teléfono' },
        { key: 'status', label: 'Estado', render: () => <span className="tag tag-green">Activo</span> },
      ]}
      EmptyIcon={FarmersIcon}
      emptyText="Los cosecheros registrados aparecerán aquí"
    />
  )
}

export function Traders() {
  return (
    <DataPage
      title="Negociantes"
      subtitle="Compradores y negociantes registrados en el sistema"
      fetchFn={(token) => api.getTraders(token)}
      columns={[
        { key: 'name', label: 'Nombre', render: r => `${r.name} ${r.lastName}` },
        { key: 'phone', label: 'Teléfono' },
        { key: 'status', label: 'Estado', render: () => <span className="tag tag-blue">Activo</span> },
      ]}
      EmptyIcon={TradersIcon}
      emptyText="Los negociantes registrados aparecerán aquí"
    />
  )
}

export function Products() {
  return (
    <DataPage
      title="Catálogo de Productos"
      subtitle="Productos agrícolas disponibles en el sistema"
      fetchFn={(token) => api.getProducts(token)}
      columns={[
        { key: 'name', label: 'Producto' },
        { key: 'description', label: 'Descripción' },
        { key: 'status', label: 'Estado', render: () => <span className="tag tag-earth">Disponible</span> },
      ]}
      EmptyIcon={ProductsIcon}
      emptyText="Los productos del catálogo aparecerán aquí"
    />
  )
}
