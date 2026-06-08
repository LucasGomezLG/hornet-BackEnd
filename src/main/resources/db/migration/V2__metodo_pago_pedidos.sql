-- V2 — Columnas de método de pago en pedidos

ALTER TABLE pedidos ADD COLUMN metodo_pago TEXT;      -- mp | transferencia | cripto
ALTER TABLE pedidos ADD COLUMN pago_referencia TEXT;  -- tx hash USDT, referencia bancaria, o ID de pago MP

CREATE INDEX idx_pedidos_metodo_pago ON pedidos(metodo_pago);
