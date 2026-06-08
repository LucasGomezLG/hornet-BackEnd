package com.hornetimports.cotizador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CotizacionRepository extends JpaRepository<Cotizacion, UUID> {

    Page<Cotizacion> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Cotizacion> findByEstadoOrderByCreatedAtDesc(EstadoCotizacion estado, Pageable pageable);

    @Modifying
    @Query("UPDATE Cotizacion c SET c.estado = :expirada WHERE c.estado = :pendiente AND c.createdAt < :limite")
    int expirarAntiguas(
            @Param("expirada") EstadoCotizacion expirada,
            @Param("pendiente") EstadoCotizacion pendiente,
            @Param("limite") OffsetDateTime limite);
}