package com.hornetimports.cotizador.dto;

import jakarta.validation.constraints.*;

public record CotizarRequest(
        @NotBlank  String productoUrl,
        @NotBlank  String nombreProducto,
        @NotNull @Positive double precioUsd,
        @NotNull @Positive @DecimalMax("30.0") double pesoKg,
        @NotBlank  String categoriaId,
        @NotBlank @Pattern(regexp = "particular|mayorista") String tipo,
        @NotBlank @Pattern(regexp = "completo|forwarding")  String tipoServicio,
        @NotBlank @Pattern(regexp = "asia|europa|eeuu|otro") String origen
) {}