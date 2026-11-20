-- =====================================================================
-- V1: Catalogos de AGROBODEGAA (reemplazan a Strapi y a los valores
--     escritos en el codigo).
--
--   message_catalog       -> mensajes del sistema (errores, avisos)
--   notification_template -> plantillas de notificaciones (correo, etc.)
--   app_parameter         -> parametros de negocio (comisiones, etc.)
--
-- El script es idempotente: se puede ejecutar mas de una vez sin error.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Mensajes del sistema
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS message_catalog (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(100) NOT NULL,
    locale      VARCHAR(10)  NOT NULL DEFAULT 'es-CO',
    type        VARCHAR(10)  NOT NULL
                CHECK (type IN ('SUCCESS', 'ERROR', 'WARNING', 'INFO')),
    title       VARCHAR(150),
    message     TEXT         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_message_catalog_code_locale UNIQUE (code, locale)
);

-- ---------------------------------------------------------------------
-- 2. Plantillas de notificacion
--    Los datos variables se escriben como {{nombre}} dentro del texto.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_template (
    id             BIGSERIAL    PRIMARY KEY,
    event_code     VARCHAR(100) NOT NULL,
    channel        VARCHAR(10)  NOT NULL
                   CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    locale         VARCHAR(10)  NOT NULL DEFAULT 'es-CO',
    subject        VARCHAR(200) NOT NULL,
    body_template  TEXT         NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_notification_template UNIQUE (event_code, channel, locale)
);

-- ---------------------------------------------------------------------
-- 3. Parametros de la aplicacion
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_parameter (
    id           BIGSERIAL    PRIMARY KEY,
    param_key    VARCHAR(100) NOT NULL UNIQUE,
    param_value  VARCHAR(500) NOT NULL,
    value_type   VARCHAR(10)  NOT NULL
                 CHECK (value_type IN ('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN')),
    description  VARCHAR(300),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------
-- Seguridad: Supabase expone por API las tablas de "public". Activamos
-- RLS sin politicas para que nadie pueda leerlas con la clave publica.
-- El backend se conecta directo a Postgres y no se ve afectado.
-- ---------------------------------------------------------------------
ALTER TABLE message_catalog       ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_template ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_parameter         ENABLE ROW LEVEL SECURITY;

-- ---------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------
INSERT INTO message_catalog (code, type, title, message) VALUES
    ('PRODUCT_NOT_FOUND',        'ERROR', 'Producto no encontrado',        'Producto no encontrado'),
    ('CLASSIFICATION_NOT_FOUND', 'ERROR', 'Clasificacion no encontrada',   'Clasificación no encontrada'),
    ('FARMER_NOT_FOUND',         'ERROR', 'Cosechero no encontrado',       'Cosechero no encontrado'),
    ('TRADER_NOT_FOUND',         'ERROR', 'Negociante no encontrado',      'Negociante no encontrado'),
    ('LOT_NOT_FOUND',            'ERROR', 'Lote no encontrado',            'Lote no encontrado'),
    ('INSUFFICIENT_STOCK',       'ERROR', 'Stock insuficiente',            'Stock insuficiente.'),
    ('ENTRY_REGISTERED',         'SUCCESS', 'Entrada registrada',          'La entrada fue registrada correctamente.'),
    ('EXIT_REGISTERED',          'SUCCESS', 'Salida registrada',           'La salida fue registrada correctamente.')
ON CONFLICT (code, locale) DO NOTHING;

INSERT INTO notification_template (event_code, channel, subject, body_template) VALUES
    ('ENTRY_REGISTERED', 'EMAIL', 'Nueva entrada registrada en AGROBODEGA',
$$Hola,

Se registró una nueva entrada en AGROBODEGA:

Cosechero: {{farmerName}}
Producto: {{productName}}
Clasificación: {{classification}}
Lote: {{lotNumber}}
Cantidad: {{quantity}}
Valor total: {{totalPrice}}

AGROBODEGA$$)
ON CONFLICT (event_code, channel, locale) DO NOTHING;

INSERT INTO app_parameter (param_key, param_value, value_type, description) VALUES
    ('ENTRY_COMMISSION_RATE',         '0.05', 'DECIMAL', 'Comision sobre el valor total de una entrada (0.05 = 5%)'),
    ('EXIT_COMMISSION_RATE',          '0.03', 'DECIMAL', 'Comision sobre el valor total de una salida (0.03 = 3%)'),
    ('NOTIFICATION_DEFAULT_RECIPIENT','cambiar@ejemplo.com', 'STRING', 'Correo que recibe las notificaciones. Cambiar por uno real')
ON CONFLICT (param_key) DO NOTHING;
