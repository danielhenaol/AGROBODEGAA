import { useAuth0 } from '@auth0/auth0-react'
import { Navigate } from 'react-router-dom'
import { useState } from 'react'

export default function LoginPage() {
  const { loginWithRedirect, isAuthenticated, isLoading } = useAuth0()
  const [error, setError] = useState('')

  const handleLogin = async () => {
    try {
      setError('')

      await loginWithRedirect({
        authorizationParams: {
          redirect_uri: window.location.origin,
          audience: import.meta.env.VITE_AUTH0_AUDIENCE,
          scope: 'openid profile email',
          prompt: 'login',
        },
        appState: {
          returnTo: '/',
        },
      })
    } catch (err) {
      console.error('Error Auth0:', err)
      setError(err.message || 'No se pudo iniciar sesión con Auth0')
    }
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

          {error && (
            <div
              style={{
                background: '#fee2e2',
                color: '#991b1b',
                padding: 12,
                borderRadius: 8,
                marginBottom: 16,
                fontSize: 14,
              }}
            >
              {error}
            </div>
          )}

          <button
            type="button"
            className="btn btn-primary login-btn"
            onClick={handleLogin}
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