package com.hornetimports.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CrearTiendaProductoRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotBlank String categoria,
        UUID subcategoriaId,
        @NotNull @Positive BigDecimal precioUsd,
        @NotNull @Min(0) int stock,
        boolean destacado,
        String imagenUrl
) {}
