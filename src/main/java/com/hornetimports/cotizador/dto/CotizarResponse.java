package com.hornetimports.cotizador.dto;

import java.util.UUID;

public record CotizarResponse(
        boolean ok,
        UUID cotizacionId,
        CotizacionDesglose desglose,
        String razon
) {}