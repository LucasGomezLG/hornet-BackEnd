package com.hornetimports.pedido.dto;

public record ConfirmarPedidoResponse(
        String                   pedidoId,
        String                   metodoPago,
        String                   mpInitPoint,
        CriptoInstrucciones      cripto,
        TransferenciaInstrucciones transferencia
) {}