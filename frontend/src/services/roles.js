import { useAuth0 } from '@auth0/auth0-react'

// La accion "Agregar roles al token" de Auth0 guarda los roles en esta propiedad del token.
export const ROLES_CLAIM = 'https://agrobodega.api/roles'

export const ROLE_OWNER = 'OWNER'
export const ROLE_ADMIN = 'WAREHOUSE_ADMIN'

const ETIQUETAS = {
  [ROLE_OWNER]: 'Propietario',
  [ROLE_ADMIN]: 'Administrador de bodega',
}

/** Rol de la persona que inicio sesion, segun Auth0. */
export function useRole() {
  const { user } = useAuth0()
  const roles = Array.isArray(user?.[ROLES_CLAIM]) ? user[ROLES_CLAIM] : []
  const isOwner = roles.includes(ROLE_OWNER)
  const isAdmin = roles.includes(ROLE_ADMIN)
  const principal = isOwner ? ROLE_OWNER : isAdmin ? ROLE_ADMIN : null
  return {
    roles,
    isOwner,
    isAdmin,
    hasAccess: isOwner || isAdmin,
    label: principal ? ETIQUETAS[principal] : 'Sin rol asignado',
  }
}
