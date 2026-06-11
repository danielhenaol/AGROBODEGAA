import { useAuth0 } from '@auth0/auth0-react'
import { Navigate, useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth0()
  const navigate = useNavigate()

  const domain = import.meta.env.VITE_AUTH0_DOMAIN
  const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID
  const audience = import.meta.env.VITE_AUTH0_AUDIENCE
  const redirectUri = window.location.origin

  const auth0Url =
    `https://${domain}/authorize?` +
    `response_type=code&` +
    `client_id=${encodeURIComponent(clientId)}&` +
    `redirect_uri=${encodeURIComponent(redirectUri)}&` +
    `scope=${encodeURIComponent('openid profile email')}&` +
    `audience=${encodeURIComponent(audience)}`

  const handleDemo = () => {
    localStorage.setItem('agrobodega_demo', 'true')
    navigate('/')
  }

  if (isLoading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner" />
        <span>Cargando...</span>
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="login-page">
      <div className="login-left">
        <div className="login-brand">
          <div className="login-brand-icon">🌿</div>
          <h1>AGROBODEGA</h1>
          <p>
            Sistema integral de gestión para bodegas agrícolas. Control de entradas,
            salidas e inventario en tiempo real.
          </p>
        </div>
      </div>

      <div className="login-right">
        <div className="login-box">
          <h2>Bienvenido</h2>
          <p>Inicia sesión para acceder al sistema de gestión agrícola.</p>

          <a
            className="btn btn-primary login-btn"
            href={auth0Url}
            style={{
              textDecoration: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
            }}
          >
            <svg
              width="18"
              height="18"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              viewBox="0 0 24 24"
            >
              <path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M15 12H3" />
            </svg>
            Iniciar sesión con Auth0
          </a>

          <button
            type="button"
            className="btn login-btn"
            onClick={handleDemo}
            style={{
              marginTop: 12,
              backgroundColor: '#e5e7eb',
              color: '#111827',
              border: '1px solid #d1d5db',
            }}
          >
            Entrar en modo demo
          </button>

          <p
            style={{
              marginTop: 24,
              fontSize: 12,
              color: '#9ca3af',
              textAlign: 'center',
            }}
          >
            Autenticación segura con Auth0 · JWT · OAuth 2.0
          </p>
        </div>
      </div>
    </div>
  )
}