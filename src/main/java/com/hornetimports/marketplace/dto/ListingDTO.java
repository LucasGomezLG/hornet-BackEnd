package com.hornetimports.marketplace.dto;

import com.hornetimports.marketplace.Listing;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingDTO(
        UUID id,
        String nombre,
        String descripcion,
        String vendedorNombre,
        String categoria,
        UUID subcategoriaId,
        BigDecimal precioUsd,
        BigDecimal precioArs,
        int stock,
        String imagenUrl
) {
    public static ListingDTO from(Listing l) {
        String vendNombre = l.getVendedor().getNombre() != null
                ? l.getVendedor().getNombre()
                : l.getVendedor().getEmail();
        UUID subId = l.getSubcategoria() != null ? l.getSubcategoria().getId() : null;
        return new ListingDTO(
                l.getId(), l.getNombre(), l.getDescripcion(),
                vendNombre, l.getCategoria(), subId,
                l.getPrecioUsd(), l.getPrecioArs(), l.getStock(), l.getImagenUrl());
    }
}
