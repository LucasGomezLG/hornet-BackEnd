package com.hornetimports.tienda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TiendaRepository extends JpaRepository<TiendaProducto, UUID> {

    Page<TiendaProducto> findByActivoTrue(Pageable pageable);
    Page<TiendaProducto> findByActivoTrueAndCategoria(String categoria, Pageable pageable);
    Page<TiendaProducto> findByActivoTrueAndDestacadoTrue(Pageable pageable);

    @Query("SELECT p FROM TiendaProducto p WHERE p.activo = true AND p.subcategoria.id = :subId")
    Page<TiendaProducto> findByActivoTrueAndSubcategoriaId(@Param("subId") UUID subId, Pageable pageable);

    @Query("SELECT p FROM TiendaProducto p WHERE p.activo = true AND p.categoria = :cat AND p.subcategoria.id = :subId")
    Page<TiendaProducto> findByActivoTrueAndCategoriaAndSubcategoriaId(
            @Param("cat") String cat, @Param("subId") UUID subId, Pageable pageable);

    // Búsqueda por texto — solo se llama cuando search != null (evita lower(bytea) en PG)
    @Query("""
        SELECT p FROM TiendaProducto p
        WHERE p.activo = true
        AND (:categoria IS NULL OR p.categoria = :categoria)
        AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY p.createdAt DESC
    """)
    Page<TiendaProducto> buscarConTexto(
            @Param("categoria") String categoria,
            @Param("search") String search,
            Pageable pageable);
}
