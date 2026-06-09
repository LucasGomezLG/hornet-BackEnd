package com.hornetimports.solicitud.dto;

import com.hornetimports.pedido.dto.CriptoInstrucciones;
import com.hornetimports.pedido.dto.TransferenciaInstrucciones;

import java.math.BigDecimal;

public record ConfirmarItemResponse(
        String pedidoId,
        String tipoServicio,
        BigDecimal montoSenaArs,           // 50% del total (null si forwarding)
        BigDecimal montoSaldoArs,          // otro 50% (null si forwarding)
        BigDecimal montoTotalArs,          // total a pagar al llegar (null si completo)
        String metodoPago,
        String mpInitPoint,
        CriptoInstrucciones cripto,
        TransferenciaInstrucciones transferencia,
        String mensaje
) {}
