package com.hornetimports.cotizador.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CotizarRequest(
        @NotBlank  String productoUrl,
        @NotBlank  String nombreProducto,
        @NotNull @Positive BigDecimal precioUsd,
        @NotNull @Positive @DecimalMax("30.0") BigDecimal pesoKg,
        @NotBlank  String categoriaId,
        @NotBlank @Pattern(regexp = "particular|mayorista") String tipo,
        @NotBlank @Pattern(regexp = "completo|forwarding")  String tipoServicio,
        @NotBlank @Pattern(regexp = "asia|europa|eeuu|otro") String origen
) {}