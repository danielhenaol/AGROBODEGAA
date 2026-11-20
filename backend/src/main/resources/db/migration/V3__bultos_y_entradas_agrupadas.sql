-- =====================================================================
-- V3: la bodega es mayorista, se trabaja por BULTOS (no por kilos), y una
-- entrada con varios productos es UN solo registro con UN solo correo.
--   * Plantilla ENTRY_REGISTERED: lista todas las lineas de la entrada.
--   * Plantilla EXIT_REGISTERED: textos en bultos.
--   * Mensajes nuevos/actualizados en el catalogo.
-- Idempotente: se puede ejecutar mas de una vez.
-- =====================================================================

UPDATE notification_template
SET subject = 'Nueva entrada registrada en AGROBODEGA',
    body_template =
$$Hola,

Se registró una nueva entrada en AGROBODEGA:

Cosechero: {{farmerName}}
Lote: {{lotNumber}}
Fecha: {{entryDate}}

Productos:
{{items}}

Total de bultos: {{totalQuantity}}
Valor total: ${{totalPrice}}

AGROBODEGA$$
WHERE event_code = 'ENTRY_REGISTERED' AND channel = 'EMAIL';

UPDATE notification_template
SET body_template =
$$Hola,

Se registró una nueva salida en AGROBODEGA:

Negociante: {{traderName}}
Producto: {{productName}}
Clasificación: {{classification}}
Bultos: {{quantity}}
Valor total: ${{totalPrice}}
Bultos restantes del lote: {{remainingQuantity}}

AGROBODEGA$$
WHERE event_code = 'EXIT_REGISTERED' AND channel = 'EMAIL';

UPDATE message_catalog
SET message = 'Stock insuficiente: no hay suficientes bultos disponibles en ese lote.'
WHERE code = 'INSUFFICIENT_STOCK';

INSERT INTO message_catalog (code, type, title, message) VALUES
    ('CLASSIFICATION_MISMATCH', 'ERROR', 'Clasificacion no corresponde',
     'La clasificación no pertenece al producto seleccionado.')
ON CONFLICT (code, locale) DO NOTHING;
