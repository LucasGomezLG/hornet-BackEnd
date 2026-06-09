package com.hornetimports.admin.dto;

import com.hornetimports.pedido.EstadoPedido;
import com.hornetimports.pedido.Pedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PedidoAdminDTO(
        String id,
        String userEmail,
        String productoNombre,
        BigDecimal precioUsd,
        BigDecimal costoTotalArs,
        EstadoPedido estado,
        String tipoServicio,
        String metodoPago,
        String pagoReferencia,
        BigDecimal montoSena,
        BigDecimal montoSaldo,
        String saldoReferencia,
        String trackingCode,
        String trackingCodigoCliente,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PedidoAdminDTO from(Pedido p) {
        return new PedidoAdminDTO(
                p.getId(),
                p.getUser() != null ? p.getUser().getEmail() : null,
                p.getProductoNombre(), p.getPrecioUsd(), p.getCostoTotalArs(),
                p.getEstado(), p.getTipoServicio(),
                p.getMetodoPago(), p.getPagoReferencia(),
                p.getMontoSena(), p.getMontoSaldo(), p.getSaldoReferencia(),
                p.getTrackingCode(), p.getTrackingCodigoCliente(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
