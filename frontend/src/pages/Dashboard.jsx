import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api } from '../services/api'

export default function Dashboard() {
  const { getAccessTokenSilently, user } = useAuth0()
  const [stats, setStats] = useState({ farmers: 0, traders: 0, lots: 0, products: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadStats = async () => {
      try {
        const token = await getAccessTokenSilently()
        const [f, t, l, p] = await Promise.all([
          api.getFarmers(token),
          api.getTraders(token),
          api.getLots(token),
          api.getProducts(token),
        ])
        setStats({
          farmers: f.data?.length || 0,
          traders: t.data?.length || 0,
          lots: l.data?.length || 0,
          products: p.data?.length || 0,
        })
      } catch (e) {
        console.error(e)
      } finally {
        setLoading(false)
      }
    }
    loadStats()
  }, [])

  const hora = new Date().getHours()
  const saludo = hora < 12 ? 'Buenos días' : hora < 18 ? 'Buenas tardes' : 'Buenas noches'

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>{saludo}, {user?.name?.split(' ')[0] || 'Usuario'}</h1>
          <p>Resumen del sistema de bodega agrícola</p>
        </div>
        <span className="badge badge-green">
          <span className="live-dot" />
          Sistema activo
        </span>
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 60 }}>
          <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 40, height: 40 }} />
        </div>
      ) : (
        <>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon green">
                <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z"/>
                  <path d="M16 21V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v16"/>
                </svg>
              </div>
              <div className="stat-info">
                <div className="stat-label">Lotes activos</div>
                <div className="stat-value">{stats.lots}</div>
                <div className="stat-sub">En bodega</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon earth">
                <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                </svg>
              </div>
              <div className="stat-info">
                <div className="stat-label">Cosecheros</div>
                <div className="stat-value">{stats.farmers}</div>
                <div className="stat-sub">Registrados</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon blue">
                <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
              <div className="stat-info">
                <div className="stat-label">Negociantes</div>
                <div className="stat-value">{stats.traders}</div>
                <div className="stat-sub">Registrados</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon amber">
                <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M20 12V8l-4-4H8L4 8v4M4 12h16M12 12v9"/>
                </svg>
              </div>
              <div className="stat-info">
                <div className="stat-label">Productos</div>
                <div className="stat-value">{stats.products}</div>
                <div className="stat-sub">En catálogo</div>
              </div>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="card">
              <div className="card-header">
                <div className="card-title">Acciones rápidas</div>
              </div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <a href="/cosecheros" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                  Registrar cosechero
                </a>
                <a href="/negociantes" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  Registrar negociante
                </a>
                <a href="/productos" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M20 12V8l-4-4H8L4 8v4M4 12h16M12 12v9"/></svg>
                  Gestionar productos
                </a>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <div className="card-title">Operaciones</div>
              </div>
              <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <a href="/lotes" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M20 7H4a2 2 0 00-2 2v10a2 2 0 002 2h16a2 2 0 002-2V9a2 2 0 00-2-2z"/></svg>
                  Registrar lote
                </a>
                <a href="/entradas" className="btn btn-primary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 5v14M5 12l7 7 7-7"/></svg>
                  Registrar entrada
                </a>
                <a href="/salidas" className="btn btn-secondary" style={{ justifyContent: 'flex-start' }}>
                  <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M12 19V5M5 12l7-7 7 7"/></svg>
                  Registrar salida
                </a>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
