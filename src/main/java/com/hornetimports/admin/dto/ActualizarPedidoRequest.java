package com.hornetimports.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarPedidoRequest(
        @NotBlank String estado,
        String trackingCode
) {}
