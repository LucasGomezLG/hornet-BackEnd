package com.hornetimports.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ConfirmarPedidoRequest(
        @NotNull  UUID   cotizacionId,
        @NotBlank @Pattern(regexp = "mp|transferencia|cripto") String metodoPago
) {}