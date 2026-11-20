-- =====================================================================
-- V5: gastos de la bodega, reportes economicos y fecha con hora.
--   * Catalogo de categorias de gasto (expense_category).
--   * Parametros: zona horaria de la bodega y umbral de bajo inventario.
--   * Mensajes y plantilla de correo del gasto registrado.
-- Idempotente: se puede ejecutar mas de una vez.
-- NOTA: no usar el simbolo de dolar seguido de llaves en este archivo;
-- Flyway lo interpreta como una variable.
-- =====================================================================

CREATE TABLE IF NOT EXISTS expense_category (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

ALTER TABLE expense_category ENABLE ROW LEVEL SECURITY;

INSERT INTO expense_category (code, name, sort_order) VALUES
    ('PAYROLL',     'Nómina y jornales',        1),
    ('UTILITIES',   'Servicios públicos',       2),
    ('RENT',        'Arriendo',                 3),
    ('TRANSPORT',   'Transporte y fletes',      4),
    ('PACKAGING',   'Empaques y costales',      5),
    ('MAINTENANCE', 'Mantenimiento',            6),
    ('SUPPLIES',    'Insumos y aseo',           7),
    ('TAXES',       'Impuestos y licencias',    8),
    ('OTHER',       'Otros',                    9)
ON CONFLICT (code) DO NOTHING;

INSERT INTO app_parameter (param_key, param_value, value_type, description) VALUES
    ('APP_TIMEZONE',     'America/Bogota', 'STRING',
     'Zona horaria de la bodega: define que es "hoy" y como se agrupan los reportes por dia'),
    ('LOW_STOCK_BULTOS', '20',             'INTEGER',
     'Un producto con esta cantidad de bultos o menos se marca como bajo inventario')
ON CONFLICT (param_key) DO NOTHING;

INSERT INTO message_catalog (code, type, title, message) VALUES
    ('EXPENSE_CATEGORY_NOT_FOUND', 'ERROR',   'Categoria no encontrada', 'La categoría de gasto no existe.'),
    ('EXPENSE_NOT_FOUND',          'ERROR',   'Gasto no encontrado',     'El gasto no existe.'),
    ('DATE_IN_FUTURE',             'ERROR',   'Fecha futura',            'La fecha y hora no pueden estar en el futuro.'),
    ('INVALID_DATE_RANGE',         'ERROR',   'Rango de fechas',         'El rango de fechas no es válido (máximo un año).'),
    ('EXPENSE_REGISTERED',         'SUCCESS', 'Gasto registrado',        'El gasto fue registrado correctamente.')
ON CONFLICT (code, locale) DO NOTHING;

INSERT INTO notification_template (event_code, channel, subject, body_template) VALUES
    ('EXPENSE_REGISTERED', 'EMAIL', 'Nuevo gasto registrado en AGROBODEGA',
$$Hola,

Se registró un nuevo gasto en AGROBODEGA:

Categoría: {{category}}
Detalle: {{description}}
Valor ($): {{amount}}
Fecha: {{occurredAt}}
Registrado por: {{registeredBy}}

AGROBODEGA$$)
ON CONFLICT (event_code, channel, locale) DO NOTHING;
