-- ============================================================
-- V5 — Solicitudes de cotización manual + seña/saldo en pedidos
-- ============================================================

-- Una solicitud = lo que el usuario envía para que el admin cotice (1..N productos)
CREATE TABLE solicitudes (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
  estado       TEXT        NOT NULL DEFAULT 'pendiente',
                           -- pendiente | cotizada | procesada | cancelada | expirada
  nota_cliente TEXT,
  nota_admin   TEXT,
  expires_at   TIMESTAMPTZ,  -- se fija al pasar a cotizada (ahora + 3 días)
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_solicitudes_user_id   ON solicitudes(user_id);
CREATE INDEX idx_solicitudes_estado    ON solicitudes(estado);
CREATE INDEX idx_solicitudes_expires   ON solicitudes(expires_at) WHERE expires_at IS NOT NULL;

-- Trigger para updated_at
CREATE TRIGGER set_solicitudes_updated_at
  BEFORE UPDATE ON solicitudes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Un ítem = un producto dentro de la solicitud
CREATE TABLE solicitud_items (
  id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  solicitud_id      UUID         NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
  -- el usuario llena esto:
  nombre_producto   TEXT         NOT NULL,
  url_producto      TEXT,
  precio_usd_ref    NUMERIC(10,2),
  peso_kg           NUMERIC(6,3),
  categoria         TEXT,
  cantidad          INT          NOT NULL DEFAULT 1,
  origen            TEXT,
  tipo_servicio     TEXT         NOT NULL DEFAULT 'completo',
  tipo              TEXT         NOT NULL DEFAULT 'particular',
  -- el admin llena esto:
  precio_final_usd  NUMERIC(10,2),
  costo_total_ars   NUMERIC(14,2),
  desglose          JSONB,
  nota_item         TEXT,
  estado_item       TEXT         NOT NULL DEFAULT 'pendiente',
                                 -- pendiente | aprobado | rechazado | confirmado | expirado
  motivo_rechazo    TEXT,
  created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_solicitud_items_solicitud ON solicitud_items(solicitud_id);
CREATE INDEX idx_solicitud_items_estado    ON solicitud_items(estado_item);

-- Columnas de seña y saldo en pedidos
ALTER TABLE pedidos ADD COLUMN monto_sena        NUMERIC(14,2);
ALTER TABLE pedidos ADD COLUMN monto_saldo       NUMERIC(14,2);
ALTER TABLE pedidos ADD COLUMN metodo_pago_saldo TEXT;
ALTER TABLE pedidos ADD COLUMN saldo_referencia  TEXT;
ALTER TABLE pedidos ADD COLUMN solicitud_item_id UUID REFERENCES solicitud_items(id) ON DELETE SET NULL;

-- Nota: profiles.telefono ya existe desde V1
-- Nota: pedidos.estado es TEXT desde V4, no requiere ALTER TYPE para nuevos estados
