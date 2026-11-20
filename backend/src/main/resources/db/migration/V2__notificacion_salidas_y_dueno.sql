-- =====================================================================
-- V2: el dueno de la bodega recibe el aviso de cada entrada Y salida.
--   * El parametro del destinatario pasa a llamarse OWNER_EMAIL
--     (conserva el valor que ya tenia configurado).
--   * Se agrega la plantilla de notificacion de salidas.
-- Idempotente: se puede ejecutar mas de una vez.
-- =====================================================================

UPDATE app_parameter
SET param_key   = 'OWNER_EMAIL',
    description = 'Correo del dueño de la bodega: recibe el aviso de cada entrada y salida registradas',
    updated_at  = NOW()
WHERE param_key = 'NOTIFICATION_DEFAULT_RECIPIENT'
  AND NOT EXISTS (SELECT 1 FROM app_parameter WHERE param_key = 'OWNER_EMAIL');

INSERT INTO app_parameter (param_key, param_value, value_type, description) VALUES
    ('OWNER_EMAIL', 'cambiar@ejemplo.com', 'STRING',
     'Correo del dueño de la bodega: recibe el aviso de cada entrada y salida registradas')
ON CONFLICT (param_key) DO NOTHING;

INSERT INTO notification_template (event_code, channel, subject, body_template) VALUES
    ('EXIT_REGISTERED', 'EMAIL', 'Nueva salida registrada en AGROBODEGA',
$$Hola,

Se registró una nueva salida en AGROBODEGA:

Negociante: {{traderName}}
Producto: {{productName}}
Clasificación: {{classification}}
Cantidad: {{quantity}}
Valor total: {{totalPrice}}
Cantidad restante del lote: {{remainingQuantity}}

AGROBODEGA$$)
ON CONFLICT (event_code, channel, locale) DO NOTHING;
