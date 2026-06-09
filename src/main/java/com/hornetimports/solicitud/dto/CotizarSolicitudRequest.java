package com.hornetimports.solicitud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CotizarSolicitudRequest(
        @NotNull @Size(min = 1) @Valid List<CotizarItemRequest> items,
        String notaAdmin
) {}
