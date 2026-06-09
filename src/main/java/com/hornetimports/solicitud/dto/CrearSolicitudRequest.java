package com.hornetimports.solicitud.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearSolicitudRequest(
        String notaCliente,
        @NotNull @Size(min = 1) @Valid List<AgregarItemRequest> items
) {}
