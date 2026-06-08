package com.hornetimports.admin.dto;

import com.hornetimports.cotizador.Cotizacion;
import com.hornetimports.cotizador.EstadoCotizacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CotizacionAdminDTO(
        UUID id,
        UUID userId,
        String userEmail,
        String nombreProducto,
        String productoUrl,
        BigDecimal precioUsd,
        BigDecimal pesoKg,
        String categoria,
        BigDecimal costoTotalArs,
        EstadoCotizacion estado,
        boolean aprobadaPorAdmin,
        String tipoServicio,
        OffsetDateTime createdAt
) {
    public static CotizacionAdminDTO from(Cotizacion c, String userEmail) {
        return new CotizacionAdminDTO(
                c.getId(), c.getUserId(), userEmail,
                c.getNombreProducto(), c.getProductoUrl(),
                c.getPrecioUsd(), c.getPesoKg(),
                c.getCategoria(), c.getCostoTotalArs(),
                c.getEstado(), c.isAprobadaPorAdmin(),
                c.getTipoServicio(), c.getCreatedAt());
    }
}
