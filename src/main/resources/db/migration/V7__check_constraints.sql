-- V7: CHECK constraints para columnas estado TEXT
-- Garantiza que solo ingresen valores válidos del dominio

ALTER TABLE solicitudes
  ADD CONSTRAINT chk_solicitud_estado
    CHECK (estado IN ('pendiente','cotizada','procesada','cancelada','expirada'));

ALTER TABLE solicitud_items
  ADD CONSTRAINT chk_solicitud_item_estado
    CHECK (estado_item IN ('pendiente','aprobado','rechazado','confirmado','expirado'));

ALTER TABLE pedidos
  ADD CONSTRAINT chk_pedido_estado
    CHECK (estado IN (
      'en_proceso','comprado',
      'esperando_sena','sena_confirmada',
      'confirmado_sin_pago',
      'en_transito','en_aduana',
      'esperando_saldo','saldo_confirmado',
      'esperando_pago','pago_confirmado',
      'entregado','cancelado'
    ));

-- Garantiza que todo pedido tenga al menos un origen (cotización legacy o ítem de solicitud)
ALTER TABLE pedidos
  ADD CONSTRAINT chk_pedido_tiene_origen
    CHECK (cotizacion_id IS NOT NULL OR solicitud_item_id IS NOT NULL);
