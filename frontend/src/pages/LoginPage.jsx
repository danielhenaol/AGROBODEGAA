import { useAuth0 } from '@auth0/auth0-react'
import { Navigate } from 'react-router-dom'

export default function LoginPage() {
  const { isAuthenticated, isLoading } = useAuth0()

  const auth0Url =
    'https://dev-04xw5712h1kpdx0f.us.auth0.com/authorize' +
    '?response_type=code' +
    '&client_id=9yZh6WCLawIypifRi28FZPCaRHpXiHPN' +
    '&redirect_uri=https%3A%2F%2Fagrobodega.pages.dev' +
    '&scope=openid%20profile%20email' +
    '&audience=https%3A%2F%2Fagrobodega.api' +
    '&prompt=login'

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
            href={auth0Url}
            className="btn btn-primary login-btn"
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