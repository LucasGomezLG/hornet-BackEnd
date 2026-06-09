package com.hornetimports.solicitud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CotizarItemRequest(
        @NotNull UUID itemId,
        BigDecimal precioFinalUsd,
        BigDecimal costoTotalArs,
        String desglose,      // JSON string (sugerencia del motor u override manual)
        String nota,
        boolean aprobado,
        String motivo         // requerido si !aprobado
) {}
