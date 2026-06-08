package com.hornetimports.tienda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TiendaRepository extends JpaRepository<TiendaProducto, UUID> {
    Page<TiendaProducto> findByActivoTrue(Pageable pageable);
    Page<TiendaProducto> findByActivoTrueAndCategoria(String categoria, Pageable pageable);
    Page<TiendaProducto> findByActivoTrueAndDestacadoTrue(Pageable pageable);
}
