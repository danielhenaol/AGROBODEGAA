-- =====================================================================
-- V4: una salida con varios productos es UN solo registro y UN solo correo.
--   * Plantilla EXIT_REGISTERED: lista todas las lineas de la salida.
-- Idempotente: se puede ejecutar mas de una vez.
-- =====================================================================

UPDATE notification_template
SET body_template =
$$Hola,

Se registró una nueva salida en AGROBODEGA:

Negociante: {{traderName}}
Fecha: {{exitDate}}

Productos:
{{items}}

Total de bultos: {{totalQuantity}}
Valor total ($): {{totalPrice}}

AGROBODEGA$$
WHERE event_code = 'EXIT_REGISTERED' AND channel = 'EMAIL';
