package com.hornetimports.solicitud.dto;

import com.hornetimports.solicitud.SolicitudItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SolicitudItemDTO(
        UUID id,
        String nombreProducto,
        String urlProducto,
        BigDecimal precioUsdRef,
        BigDecimal pesoKg,
        String categoria,
        int cantidad,
        String origen,
        String tipoServicio,
        String tipo,
        // admin-filled
        BigDecimal precioFinalUsd,
        BigDecimal costoTotalArs,
        String desglose,
        String notaItem,
        String estadoItem,
        String motivoRechazo,
        OffsetDateTime createdAt
) {
    public static SolicitudItemDTO from(SolicitudItem i) {
        return new SolicitudItemDTO(
                i.getId(),
                i.getNombreProducto(), i.getUrlProducto(),
                i.getPrecioUsdRef(), i.getPesoKg(),
                i.getCategoria(), i.getCantidad(),
                i.getOrigen(), i.getTipoServicio(), i.getTipo(),
                i.getPrecioFinalUsd(), i.getCostoTotalArs(),
                i.getDesglose(), i.getNotaItem(),
                i.getEstadoItem(), i.getMotivoRechazo(),
                i.getCreatedAt());
    }
}
