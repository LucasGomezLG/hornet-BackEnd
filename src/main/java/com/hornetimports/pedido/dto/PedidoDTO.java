package com.hornetimports.pedido.dto;

import com.hornetimports.pedido.EstadoPedido;
import com.hornetimports.pedido.Pedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PedidoDTO(
        String id,
        String productoNombre,
        String productoUrl,
        BigDecimal precioUsd,
        BigDecimal costoTotalArs,
        EstadoPedido estado,
        String tipoServicio,
        String origen,
        String metodoPago,
        BigDecimal montoSena,
        BigDecimal montoSaldo,
        String trackingCode,
        String trackingCodigoCliente,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PedidoDTO from(Pedido p) {
        return new PedidoDTO(
                p.getId(),
                p.getProductoNombre(), p.getProductoUrl(),
                p.getPrecioUsd(), p.getCostoTotalArs(),
                p.getEstado(), p.getTipoServicio(), p.getOrigen(),
                p.getMetodoPago(),
                p.getMontoSena(), p.getMontoSaldo(),
                p.getTrackingCode(), p.getTrackingCodigoCliente(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
