package com.hornetimports.categoria.dto;

import com.hornetimports.categoria.Categoria;

import java.util.List;

public record CategoriaDTO(
        String id,
        String nombre,
        boolean activo,
        int orden,
        List<SubcategoriaDTO> subcategorias
) {
    public static CategoriaDTO from(Categoria c) {
        List<SubcategoriaDTO> subs = c.getSubcategorias().stream()
                .map(SubcategoriaDTO::from)
                .toList();
        return new CategoriaDTO(c.getId(), c.getNombre(), c.isActivo(), c.getOrden(), subs);
    }
}
