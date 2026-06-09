package com.hornetimports.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {

    @Query("SELECT c FROM Categoria c LEFT JOIN FETCH c.subcategorias s WHERE c.activo = true ORDER BY c.orden ASC, c.nombre ASC")
    List<Categoria> findActivasConSubcategorias();

    @Query("SELECT c FROM Categoria c LEFT JOIN FETCH c.subcategorias ORDER BY c.orden ASC, c.nombre ASC")
    List<Categoria> findAllConSubcategorias();
}
