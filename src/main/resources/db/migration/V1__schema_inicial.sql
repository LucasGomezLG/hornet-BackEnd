-- ============================================================
-- V1 — Schema inicial de Hornet Imports
-- ============================================================

-- ENUMS
CREATE TYPE tipo_cuenta AS ENUM ('comprador', 'vendedor', 'admin');
CREATE TYPE estado_cotizacion AS ENUM ('pendiente', 'aprobada', 'rechazada', 'expirada');
CREATE TYPE estado_pedido AS ENUM (
  'en_proceso', 'comprado', 'en_transito', 'en_aduana', 'entregado', 'cancelado'
);

-- SECUENCIA para IDs de pedido (HI-0001, HI-0002...)
CREATE SEQUENCE pedido_seq START 1;

-- TABLA: profiles
CREATE TABLE profiles (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email       TEXT        NOT NULL UNIQUE,
  google_id   TEXT        UNIQUE,
  nombre      TEXT,
  apellido    TEXT,
  telefono    TEXT,
  cuit        TEXT        UNIQUE,
  tipo        tipo_cuenta NOT NULL DEFAULT 'comprador',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- TABLA: cotizaciones
CREATE TABLE cotizaciones (
  id                 UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id            UUID              REFERENCES profiles(id) ON DELETE SET NULL,
  producto_url       TEXT              NOT NULL,
  nombre_producto    TEXT              NOT NULL,
  precio_usd         NUMERIC(10,2)     NOT NULL,
  peso_kg            NUMERIC(6,3)      NOT NULL,
  categoria          TEXT              NOT NULL,
  costo_total_ars    NUMERIC(14,2)     NOT NULL,
  desglose           JSONB             NOT NULL DEFAULT '{}',
  estado             estado_cotizacion NOT NULL DEFAULT 'pendiente',
  aprobada_por_admin BOOLEAN           NOT NULL DEFAULT FALSE,
  tipo_servicio      TEXT              NOT NULL DEFAULT 'completo',
  utm_source         TEXT,
  created_at         TIMESTAMPTZ       NOT NULL DEFAULT now()
);

-- TABLA: pedidos
CREATE TABLE pedidos (
  id                      TEXT          PRIMARY KEY,
  cotizacion_id           UUID          REFERENCES cotizaciones(id) ON DELETE SET NULL,
  user_id                 UUID          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
  producto_nombre         TEXT          NOT NULL,
  producto_url            TEXT,
  precio_usd              NUMERIC(10,2) NOT NULL,
  costo_total_ars         NUMERIC(14,2) NOT NULL,
  estado                  estado_pedido NOT NULL DEFAULT 'en_proceso',
  tracking_code           TEXT,
  tracking_codigo_cliente TEXT,
  tipo_servicio           TEXT          NOT NULL DEFAULT 'completo',
  origen                  TEXT,
  created_at              TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- TABLA: listings (marketplace de vendedores)
CREATE TABLE listings (
  id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  vendedor_id  UUID          NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
  nombre       TEXT          NOT NULL,
  descripcion  TEXT,
  precio_usd   NUMERIC(10,2),
  precio_ars   NUMERIC(14,2) NOT NULL,
  categoria    TEXT          NOT NULL,
  imagen_url   TEXT,
  stock        INT           NOT NULL DEFAULT 0,
  activo       BOOLEAN       NOT NULL DEFAULT true,
  created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- TABLA: tienda_productos (catálogo curado por admin)
CREATE TABLE tienda_productos (
  id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre      TEXT          NOT NULL,
  descripcion TEXT,
  categoria   TEXT          NOT NULL,
  precio_usd  NUMERIC(10,2) NOT NULL,
  imagen_url  TEXT,
  stock       INT           NOT NULL DEFAULT 0,
  destacado   BOOLEAN       NOT NULL DEFAULT false,
  activo      BOOLEAN       NOT NULL DEFAULT true,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- TABLA: refresh_tokens
CREATE TABLE refresh_tokens (
  token      TEXT        PRIMARY KEY,
  user_id    UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ÍNDICES
CREATE INDEX idx_cotizaciones_user    ON cotizaciones(user_id);
CREATE INDEX idx_cotizaciones_estado  ON cotizaciones(estado);
CREATE INDEX idx_pedidos_user         ON pedidos(user_id);
CREATE INDEX idx_pedidos_estado       ON pedidos(estado);
CREATE INDEX idx_listings_vendedor    ON listings(vendedor_id);
CREATE INDEX idx_listings_categoria   ON listings(categoria);
CREATE INDEX idx_listings_activo      ON listings(activo);
CREATE INDEX idx_tienda_categoria     ON tienda_productos(categoria);
CREATE INDEX idx_tienda_activo        ON tienda_productos(activo);
CREATE INDEX idx_refresh_tokens_user  ON refresh_tokens(user_id);
CREATE INDEX idx_profiles_google_id   ON profiles(google_id);

-- TRIGGER: updated_at automático
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END; $$;

CREATE TRIGGER pedidos_updated_at
  BEFORE UPDATE ON pedidos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER tienda_productos_updated_at
  BEFORE UPDATE ON tienda_productos
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
