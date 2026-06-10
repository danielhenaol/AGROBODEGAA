import { Outlet, NavLink, useLocation } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'

const navItems = [
  {
    label: 'Principal',
    items: [
      { to: '/', label: 'Dashboard', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
        ), exact: true }
    ]
  },
  {
    label: 'Operaciones',
    items: [
      { to: '/entradas', label: 'Entradas', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M12 5v14M5 12l7 7 7-7"/>
            </svg>
        )},
      { to: '/salidas', label: 'Salidas', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M12 19V5M5 12l7-7 7 7"/>
            </svg>
        )},
    ]
  },
  {
    label: 'Catálogos',
    items: [
      { to: '/productos', label: 'Productos', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M20 12V8l-4-4H8L4 8v4M4 12h16M12 12v9"/>
            </svg>
        )},
      { to: '/cosecheros', label: 'Cosecheros', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
            </svg>
        )},
      { to: '/negociantes', label: 'Negociantes', icon: (
            <svg fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
        )},
    ]
  }
]

export default function Layout() {
  const { user, logout } = useAuth0()
  const location = useLocation()

  const getPageTitle = () => {
    const map = {
      '/': 'Dashboard',
      '/entradas': 'Registro de Entradas',
      '/salidas': 'Registro de Salidas',
      '/cosecheros': 'Cosecheros',
      '/negociantes': 'Negociantes',
      '/productos': 'Catálogo de Productos',
    }
    return map[location.pathname] || 'AGROBODEGA'
  }

  return (
      <div>
        <aside className="sidebar">
          <div className="sidebar-logo">
            <div className="logo-mark">
              <div className="logo-icon">🌿</div>
              <div>
                <div className="logo-text">AGROBODEGA</div>
                <span className="logo-sub">Sistema Agrícola</span>
              </div>
            </div>
          </div>

          <nav className="sidebar-nav">
            {navItems.map((section) => (
                <div key={section.label} className="nav-section">
                  <div className="nav-label">{section.label}</div>
                  {section.items.map((item) => (
                      <NavLink
                          key={item.to}
                          to={item.to}
                          end={item.exact}
                          className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                      >
                        {item.icon}
                        {item.label}
                      </NavLink>
                  ))}
                </div>
            ))}
          </nav>

          <div className="sidebar-footer">
            <div className="user-card">
              <div className="user-avatar">
                {user?.picture
                    ? <img src={user.picture} alt={user.name} />
                    : (user?.name?.[0] || 'U').toUpperCase()
                }
              </div>
              <div className="user-info">
                <div className="user-name">{user?.name || user?.email}</div>
                <div className="user-role">Administrador</div>
              </div>
              <button
                  className="logout-btn"
                  onClick={() => logout({ logoutParams: { returnTo: window.location.origin + '/login' } })}
                  title="Cerrar sesión"
              >
                <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/>
                </svg>
              </button>
            </div>
          </div>
        </aside>

        <div className="main-layout">
          <header className="topbar">
            <div className="topbar-title">{getPageTitle()}</div>
            <div className="topbar-badges">
            <span className="badge badge-green">
              <span className="live-dot" />
              Sistema activo
            </span>
              <span className="badge badge-gray">v1.0.0</span>
            </div>
          </header>

          <main className="page-content">
            <Outlet />
          </main>
        </div>
      </div>
  )
}