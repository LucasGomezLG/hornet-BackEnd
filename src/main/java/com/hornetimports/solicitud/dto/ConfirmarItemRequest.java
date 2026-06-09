package com.hornetimports.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarItemRequest(
        @NotBlank @Pattern(regexp = "mp|transferencia|cripto") String metodoPago
) {}
