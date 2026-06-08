package com.hornetimports.pedido;

import com.hornetimports.cotizador.Cotizacion;
import com.hornetimports.user.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pedidos")
@Getter @Setter @NoArgsConstructor
public class Pedido {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Profile user;

    @Column(name = "producto_nombre", nullable = false)
    private String productoNombre;

    @Column(name = "producto_url")
    private String productoUrl;

    @Column(name = "precio_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUsd;

    @Column(name = "costo_total_ars", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotalArs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.en_proceso;

    @Column(name = "tracking_code")
    private String trackingCode;

    @Column(name = "tracking_codigo_cliente")
    private String trackingCodigoCliente;

    @Column(name = "tipo_servicio", nullable = false)
    private String tipoServicio = "completo";

    @Column
    private String origen;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "pago_referencia")
    private String pagoReferencia;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}