package com.hornetimports.pedido.dto;

import com.hornetimports.pedido.EstadoPedido;

import java.time.OffsetDateTime;

public record SeguimientoPublicoDTO(
        String pedidoId,
        String productoNombre,
        EstadoPedido estado,
        String tipoServicio,
        String trackingCode,
        String trackingCodigoCliente,
        OffsetDateTime createdAt
) {}
