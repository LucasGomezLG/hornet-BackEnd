package com.hornetimports.solicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SolicitudItemRepository extends JpaRepository<SolicitudItem, UUID> {

    Optional<SolicitudItem> findByIdAndSolicitudId(UUID id, UUID solicitudId);

    @Modifying
    @Query("""
        UPDATE SolicitudItem i SET i.estadoItem = 'expirado'
        WHERE i.estadoItem = 'aprobado'
          AND i.solicitud.id IN (
            SELECT s.id FROM Solicitud s WHERE s.estado = 'expirada'
          )
        """)
    int expirarItemsDeExpiradas();
}
