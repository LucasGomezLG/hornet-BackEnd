package com.hornetimports.categoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearCategoriaRequest(
        @NotBlank String id,
        @NotBlank String nombre,
        @NotNull Integer orden
) {}
