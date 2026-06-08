package com.hornetimports.tienda.dto;

import com.hornetimports.tienda.TiendaProducto;

import java.math.BigDecimal;
import java.util.UUID;

public record TiendaProductoDTO(
        UUID id,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precioUsd,
        int stock,
        boolean destacado,
        String imagenUrl
) {
    public static TiendaProductoDTO from(TiendaProducto p) {
        return new TiendaProductoDTO(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getPrecioUsd(), p.getStock(), p.isDestacado(), p.getImagenUrl());
    }
}
