package com.hornetimports.categoria.dto;

import com.hornetimports.categoria.Subcategoria;

import java.util.UUID;

public record SubcategoriaDTO(
        UUID id,
        String categoriaId,
        String nombre,
        boolean activo,
        int orden
) {
    public static SubcategoriaDTO from(Subcategoria s) {
        return new SubcategoriaDTO(
                s.getId(),
                s.getCategoria().getId(),
                s.getNombre(),
                s.isActivo(),
                s.getOrden()
        );
    }
}
