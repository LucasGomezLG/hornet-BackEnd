package com.hornetimports.solicitud.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AgregarItemRequest(
        @NotBlank String nombreProducto,
        String urlProducto,
        @Positive BigDecimal precioUsdRef,
        @Positive @DecimalMax("30.0") BigDecimal pesoKg,
        String categoria,
        @Min(1) int cantidad,
        @Pattern(regexp = "asia|europa|eeuu|otro") String origen,
        @NotBlank @Pattern(regexp = "completo|forwarding") String tipoServicio,
        @NotBlank @Pattern(regexp = "particular|mayorista") String tipo
) {}
