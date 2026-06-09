package com.hornetimports.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, UUID> {
    List<Subcategoria> findByCategoriaIdOrderByOrdenAscNombreAsc(String categoriaId);
}
