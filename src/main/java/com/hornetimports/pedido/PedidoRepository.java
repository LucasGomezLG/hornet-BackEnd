package com.hornetimports.pedido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    Page<Pedido> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Pedido> findByEstadoOrderByCreatedAtDesc(EstadoPedido estado, Pageable pageable);
    Optional<Pedido> findByTrackingCode(String trackingCode);

    @Query("""
        SELECT p FROM Pedido p JOIN FETCH p.user
        WHERE (:estado IS NULL OR p.estado = :estado)
        AND (:metodoPago IS NULL OR p.metodoPago = :metodoPago)
        ORDER BY p.createdAt DESC
    """)
    Page<Pedido> findAdminFiltered(
            @Param("estado") EstadoPedido estado,
            @Param("metodoPago") String metodoPago,
            Pageable pageable);
}