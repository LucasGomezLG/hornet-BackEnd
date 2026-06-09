package com.hornetimports.solicitud;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudRepository extends JpaRepository<Solicitud, UUID> {

    Page<Solicitud> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Solicitud> findByEstadoOrderByCreatedAtDesc(String estado, Pageable pageable);

    @Query("SELECT s FROM Solicitud s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<Solicitud> findByIdWithItems(UUID id);

    @Modifying
    @Query("""
        UPDATE Solicitud s SET s.estado = 'expirada'
        WHERE s.estado = 'cotizada' AND s.expiresAt < :ahora
        """)
    int expirarSolicitudesVencidas(OffsetDateTime ahora);
}
