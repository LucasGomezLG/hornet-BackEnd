-- Hibernate 6 + PostgreSQL: @Enumerated(EnumType.STRING) vincula como varchar,
-- pero las columnas son native enums. Convertimos a VARCHAR para compatibilidad.

ALTER TABLE profiles ALTER COLUMN tipo DROP DEFAULT;
ALTER TABLE profiles ALTER COLUMN tipo TYPE VARCHAR(50) USING tipo::text;
ALTER TABLE profiles ALTER COLUMN tipo SET DEFAULT 'comprador';

ALTER TABLE cotizaciones ALTER COLUMN estado DROP DEFAULT;
ALTER TABLE cotizaciones ALTER COLUMN estado TYPE VARCHAR(50) USING estado::text;
ALTER TABLE cotizaciones ALTER COLUMN estado SET DEFAULT 'pendiente';

ALTER TABLE pedidos ALTER COLUMN estado DROP DEFAULT;
ALTER TABLE pedidos ALTER COLUMN estado TYPE VARCHAR(50) USING estado::text;
ALTER TABLE pedidos ALTER COLUMN estado SET DEFAULT 'en_proceso';

DROP TYPE IF EXISTS tipo_cuenta;
DROP TYPE IF EXISTS estado_cotizacion;
DROP TYPE IF EXISTS estado_pedido;
