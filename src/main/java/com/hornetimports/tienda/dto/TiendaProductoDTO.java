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
        boolean activo,
        String imagenUrl,
        UUID subcategoriaId,
        String subcategoriaNombre
) {
    public static TiendaProductoDTO from(TiendaProducto p) {
        UUID subId = p.getSubcategoria() != null ? p.getSubcategoria().getId() : null;
        String subNombre = p.getSubcategoria() != null ? p.getSubcategoria().getNombre() : null;
        return new TiendaProductoDTO(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getPrecioUsd(), p.getStock(), p.isDestacado(), p.isActivo(),
                p.getImagenUrl(), subId, subNombre);
    }
}
