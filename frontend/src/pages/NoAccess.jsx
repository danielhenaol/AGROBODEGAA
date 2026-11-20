import { useAuth0 } from '@auth0/auth0-react'

// Se muestra si la persona inicio sesion pero no tiene rol de la bodega.
export default function NoAccess() {
  const { user, logout } = useAuth0()
  return (
    <div className="loading-screen" style={{ flexDirection: 'column', gap: 12, textAlign: 'center', padding: 24 }}>
      <div style={{ fontSize: 40 }}>🔒</div>
      <h2>No tienes acceso a AGROBODEGA</h2>
      <p style={{ maxWidth: 420 }}>
        La cuenta {user?.email} no tiene un rol asignado. Pide al propietario de la bodega que
        te habilite y vuelve a iniciar sesión.
      </p>
      <button
        className="btn btn-primary"
        onClick={() => logout({ logoutParams: { returnTo: window.location.origin + '/login' } })}
      >
        Cerrar sesión
      </button>
    </div>
  )
}
