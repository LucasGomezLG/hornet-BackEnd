package com.hornetimports.cotizador;

import com.hornetimports.cotizador.dto.CotizacionDesglose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "cotizaciones")
@Getter @Setter @NoArgsConstructor
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "producto_url", nullable = false)
    private String productoUrl;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "precio_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUsd;

    @Column(name = "peso_kg", nullable = false, precision = 6, scale = 3)
    private BigDecimal pesoKg;

    @Column(nullable = false)
    private String categoria;

    @Column(name = "costo_total_ars", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotalArs;

    @Convert(converter = CotizacionDesgloseConverter.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private CotizacionDesglose desglose = new CotizacionDesglose();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado = EstadoCotizacion.pendiente;

    @Column(name = "aprobada_por_admin", nullable = false)
    private boolean aprobadaPorAdmin = false;

    @Column(name = "tipo_servicio", nullable = false)
    private String tipoServicio = "completo";

    @Column(name = "utm_source")
    private String utmSource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}