package com.hornetimports.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazarCotizacionRequest(@NotBlank String motivo) {}
