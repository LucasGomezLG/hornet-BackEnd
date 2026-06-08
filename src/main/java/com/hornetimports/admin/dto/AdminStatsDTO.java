package com.hornetimports.admin.dto;

import java.math.BigDecimal;

public record AdminStatsDTO(
        long pedidosHoy,
        BigDecimal ingresosUsdMes,
        long vendedoresActivos,
        long cotizacionesPendientes
) {}
