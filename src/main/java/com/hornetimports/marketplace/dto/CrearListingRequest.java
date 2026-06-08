package com.hornetimports.marketplace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearListingRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotBlank String categoria,
        @NotNull @Positive BigDecimal precioUsd,
        @NotNull @Min(0) int stock,
        String imagenUrl
) {}
