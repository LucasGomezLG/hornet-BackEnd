package com.hornetimports.pedido.dto;

import java.math.BigDecimal;

public record CriptoInstrucciones(
        String     red,
        String     direccion,
        BigDecimal montoUsdt,
        String     nota
) {}