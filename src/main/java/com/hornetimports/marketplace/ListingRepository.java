package com.hornetimports.marketplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @Query("""
        SELECT l FROM Listing l
        JOIN FETCH l.vendedor v
        WHERE l.activo = true
        AND (:categoria IS NULL OR l.categoria = :categoria)
        AND (:search IS NULL OR
             LOWER(l.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY l.createdAt DESC
    """)
    Page<Listing> buscar(
            @Param("categoria") String categoria,
            @Param("search") String search,
            Pageable pageable);

    Page<Listing> findByVendedorIdOrderByCreatedAtDesc(UUID vendedorId, Pageable pageable);

    @Query("SELECT l FROM Listing l JOIN FETCH l.vendedor WHERE l.id = :id AND l.activo = true")
    Optional<Listing> findActivoById(@Param("id") UUID id);

    long countByVendedorId(UUID vendedorId);
}
