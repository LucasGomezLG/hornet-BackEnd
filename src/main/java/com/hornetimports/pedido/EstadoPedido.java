package com.hornetimports.pedido;

public enum EstadoPedido {
    // ── Flujo legacy (cotizador automático) ──────────────────────────────────
    en_proceso,      // pedido creado, esperando pago único
    comprado,        // pago único confirmado

    // ── Flujo completo (Hornet compra, seña 50/50) ────────────────────────────
    esperando_sena,      // pedido creado, esperando seña del 50%
    sena_confirmada,     // seña recibida, Hornet inicia la compra

    // ── Flujo forwarding (cliente ya compró, solo logística) ─────────────────
    confirmado_sin_pago, // pedido creado, sin pago hasta llegada a BsAs

    // ── Logística compartida ──────────────────────────────────────────────────
    en_transito,     // viajando a BsAs
    en_aduana,       // en aduana BsAs

    // ── Llegada y pago final — completo ──────────────────────────────────────
    esperando_saldo, // llegó a BsAs, pendiente pago del saldo (50%)
    saldo_confirmado,// saldo recibido

    // ── Llegada y pago final — forwarding ────────────────────────────────────
    esperando_pago,  // llegó a BsAs, pendiente pago total
    pago_confirmado, // pago total recibido

    // ── Final ─────────────────────────────────────────────────────────────────
    entregado,
    cancelado
}
