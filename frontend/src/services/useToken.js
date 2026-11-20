import { useCallback } from 'react'
import { useAuth0 } from '@auth0/auth0-react'

/**
 * Devuelve una funcion que obtiene el token de Auth0.
 * Si Auth0 no puede renovarlo (por ejemplo, cookies de terceros bloqueadas),
 * deja el motivo en la consola y devuelve undefined en vez de romper la pantalla.
 * TEMPORAL: cuando el backend valide JWT (paso de roles), un token ausente
 * debera forzar un nuevo inicio de sesion.
 */
export function useToken() {
  const { getAccessTokenSilently } = useAuth0()
  return useCallback(async () => {
    try {
      return await getAccessTokenSilently()
    } catch (e) {
      console.warn('[auth] No se pudo obtener el token de Auth0:', e?.error || e?.message, e)
      return undefined
    }
  }, [getAccessTokenSilently])
}
