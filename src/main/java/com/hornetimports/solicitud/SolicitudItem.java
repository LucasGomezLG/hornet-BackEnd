package com.hornetimports.solicitud;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitud_items")
@Getter @Setter @NoArgsConstructor
public class SolicitudItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    // ── El usuario llena esto ─────────────────────────────────────────────────

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "url_producto")
    private String urlProducto;

    @Column(name = "precio_usd_ref", precision = 10, scale = 2)
    private BigDecimal precioUsdRef;

    @Column(name = "peso_kg", precision = 6, scale = 3)
    private BigDecimal pesoKg;

    @Column
    private String categoria;

    @Column(nullable = false)
    private int cantidad = 1;

    @Column
    private String origen;

    @Column(name = "tipo_servicio", nullable = false)
    private String tipoServicio = "completo";

    @Column(nullable = false)
    private String tipo = "particular";

    // ── El admin llena esto ───────────────────────────────────────────────────

    @Column(name = "precio_final_usd", precision = 10, scale = 2)
    private BigDecimal precioFinalUsd;

    @Column(name = "costo_total_ars", precision = 14, scale = 2)
    private BigDecimal costoTotalArs;

    @Column
    private String desglose;

    @Column(name = "nota_item")
    private String notaItem;

    @Column(name = "estado_item", nullable = false)
    private String estadoItem = "pendiente";

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public boolean isAprobado()    { return "aprobado".equals(estadoItem); }
    public boolean isConfirmado()  { return "confirmado".equals(estadoItem); }
    public boolean isTerminado()   { return !"pendiente".equals(estadoItem); }
}
