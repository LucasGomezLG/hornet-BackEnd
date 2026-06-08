package com.hornetimports.pedido.dto;

import java.math.BigDecimal;

public record TransferenciaInstrucciones(
        String     banco,
        String     titular,
        String     cbu,
        String     alias,
        BigDecimal monto,
        String     moneda,
        String     nota
) {}