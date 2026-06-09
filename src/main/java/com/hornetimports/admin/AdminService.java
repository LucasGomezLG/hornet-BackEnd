package com.hornetimports.admin;

import com.hornetimports.admin.dto.*;
import com.hornetimports.categoria.Subcategoria;
import com.hornetimports.categoria.SubcategoriaRepository;
import com.hornetimports.cotizador.Cotizacion;
import com.hornetimports.cotizador.CotizacionRepository;
import com.hornetimports.cotizador.EstadoCotizacion;
import com.hornetimports.email.EmailService;
import com.hornetimports.marketplace.ListingRepository;
import com.hornetimports.pedido.EstadoPedido;
import com.hornetimports.pedido.Pedido;
import com.hornetimports.pedido.PedidoRepository;
import com.hornetimports.tienda.TiendaProducto;
import com.hornetimports.tienda.TiendaRepository;
import com.hornetimports.user.Profile;
import com.hornetimports.user.ProfileRepository;
import com.hornetimports.user.TipoCuenta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final CotizacionRepository    cotizacionRepository;
    private final PedidoRepository        pedidoRepository;
    private final ProfileRepository       profileRepository;
    private final ListingRepository       listingRepository;
    private final TiendaRepository        tiendaRepository;
    private final SubcategoriaRepository  subcategoriaRepository;
    private final EmailService            emailService;
    private final EntityManager           entityManager;

    @Value("${email.admin:admin@hornetimports.com}")
    private String adminEmail;

    // ── Stats ────────────────────────────────────────────────────────────────

    public AdminStatsDTO getStats() {
        ZoneId ar = ZoneId.of("America/Argentina/Buenos_Aires");
        OffsetDateTime startOfDay  = LocalDate.now(ar).atStartOfDay(ar).toOffsetDateTime();
        OffsetDateTime endOfDay    = startOfDay.plusDays(1);
        OffsetDateTime startMonth  = YearMonth.now(ar).atDay(1).atStartOfDay(ar).toOffsetDateTime();
        OffsetDateTime endMonth    = startMonth.plusMonths(1);

        Long pedidosHoy = entityManager.createQuery(
                "SELECT COUNT(p) FROM Pedido p WHERE p.createdAt >= :s AND p.createdAt < :e",
                Long.class)
                .setParameter("s", startOfDay).setParameter("e", endOfDay)
                .getSingleResult();

        BigDecimal ingresosUsdMes = entityManager.createQuery(
                "SELECT SUM(p.precioUsd) FROM Pedido p " +
                "WHERE p.estado <> :cancelado AND p.createdAt >= :s AND p.createdAt < :e",
                BigDecimal.class)
                .setParameter("cancelado", EstadoPedido.cancelado)
                .setParameter("s", startMonth).setParameter("e", endMonth)
                .getSingleResult();
        if (ingresosUsdMes == null) ingresosUsdMes = BigDecimal.ZERO;

        long vendedoresActivos = profileRepository.countByTipo(TipoCuenta.vendedor);

        Long cotizacionesPendientes = entityManager.createQuery(
                "SELECT COUNT(c) FROM Cotizacion c WHERE c.estado = :estado", Long.class)
                .setParameter("estado", EstadoCotizacion.pendiente)
                .getSingleResult();

        Long solicitudesPendientes = entityManager.createQuery(
                "SELECT COUNT(s) FROM Solicitud s WHERE s.estado = 'pendiente'", Long.class)
                .getSingleResult();

        return new AdminStatsDTO(pedidosHoy, ingresosUsdMes, vendedoresActivos,
                cotizacionesPendientes, solicitudesPendientes);
    }

    // ── Cotizaciones ─────────────────────────────────────────────────────────

    public Page<CotizacionAdminDTO> getCotizaciones(String estadoStr, Pageable pageable) {
        Page<Cotizacion> page = estadoStr != null && !estadoStr.isBlank()
                ? cotizacionRepository.findByEstadoOrderByCreatedAtDesc(
                        EstadoCotizacion.valueOf(estadoStr), pageable)
                : cotizacionRepository.findAll(pageable);

        Set<UUID> userIds = page.stream()
                .map(Cotizacion::getUserId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> emailMap = profileRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(Profile::getId, Profile::getEmail));

        return page.map(c -> CotizacionAdminDTO.from(c, emailMap.get(c.getUserId())));
    }

    @Transactional
    public void aprobarCotizacion(UUID id) {
        Cotizacion c = cotizacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cotización no encontrada"));
        if (c.getEstado() != EstadoCotizacion.pendiente) {
            throw new IllegalStateException("Solo se puede aprobar cotizaciones pendientes");
        }
        c.setAprobadaPorAdmin(true);
        c.setEstado(EstadoCotizacion.aprobada);
        cotizacionRepository.save(c);

        if (c.getUserId() != null) {
            profileRepository.findById(c.getUserId()).ifPresent(p ->
                    emailService.sendCotizacionAprobada(p.getEmail(), c.getNombreProducto(), c.getId()));
        }
    }

    @Transactional
    public void rechazarCotizacion(UUID id, String motivo) {
        Cotizacion c = cotizacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cotización no encontrada"));
        if (c.getEstado() != EstadoCotizacion.pendiente) {
            throw new IllegalStateException("Solo se puede rechazar cotizaciones pendientes");
        }
        c.setEstado(EstadoCotizacion.rechazada);
        cotizacionRepository.save(c);

        if (c.getUserId() != null) {
            profileRepository.findById(c.getUserId()).ifPresent(p ->
                    emailService.sendCotizacionRechazada(p.getEmail(), c.getNombreProducto(), motivo));
        }
    }

    // ── Pedidos ──────────────────────────────────────────────────────────────

    public Page<PedidoAdminDTO> getPedidos(String estadoStr, String metodoPago, Pageable pageable) {
        EstadoPedido estado = estadoStr != null && !estadoStr.isBlank()
                ? EstadoPedido.valueOf(estadoStr) : null;
        String mp = (metodoPago != null && !metodoPago.isBlank()) ? metodoPago : null;
        return pedidoRepository.findAdminFiltered(estado, mp, pageable).map(PedidoAdminDTO::from);
    }

    @Transactional
    public PedidoAdminDTO actualizarPedido(String pedidoId, ActualizarPedidoRequest req) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        EstadoPedido nuevo = EstadoPedido.valueOf(req.estado());
        validarTransicion(p.getEstado(), nuevo);

        p.setEstado(nuevo);
        if (req.trackingCode() != null && !req.trackingCode().isBlank()) {
            p.setTrackingCode(req.trackingCode());
        }
        return PedidoAdminDTO.from(pedidoRepository.save(p));
    }

    @Transactional
    public PedidoAdminDTO confirmarPago(String pedidoId, ConfirmarPagoRequest req) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        if (p.getEstado() != EstadoPedido.en_proceso) {
            throw new IllegalStateException("El pedido no está en estado en_proceso");
        }
        if (!"cripto".equals(p.getMetodoPago()) && !"transferencia".equals(p.getMetodoPago())) {
            throw new IllegalStateException("Este endpoint es solo para cripto y transferencia");
        }

        p.setEstado(EstadoPedido.comprado);
        if (req.referencia() != null) p.setPagoReferencia(req.referencia());

        pedidoRepository.save(p);
        emailService.sendPedidoConfirmado(
                p.getUser().getEmail(), p.getProductoNombre(), p.getId());

        return PedidoAdminDTO.from(p);
    }

    // ── Seña y saldo ─────────────────────────────────────────────────────────

    @Transactional
    public PedidoAdminDTO confirmarSena(String pedidoId, ConfirmarPagoRequest req) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        if (p.getEstado() != EstadoPedido.esperando_sena) {
            throw new IllegalStateException("El pedido no está esperando seña");
        }
        p.setEstado(EstadoPedido.sena_confirmada);
        if (req != null && req.referencia() != null) p.setPagoReferencia(req.referencia());
        pedidoRepository.save(p);
        emailService.sendSenaConfirmada(p.getUser().getEmail(), p.getProductoNombre(), p.getId());
        return PedidoAdminDTO.from(p);
    }

    @Transactional
    public PedidoAdminDTO notificarLlegada(String pedidoId) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        boolean forwarding = "forwarding".equals(p.getTipoServicio());
        if (forwarding) {
            if (p.getEstado() != EstadoPedido.en_aduana) {
                throw new IllegalStateException("El pedido forwarding no está en aduana");
            }
            p.setEstado(EstadoPedido.esperando_pago);
        } else {
            if (p.getEstado() != EstadoPedido.en_aduana) {
                throw new IllegalStateException("El pedido no está en aduana");
            }
            p.setEstado(EstadoPedido.esperando_saldo);
        }
        pedidoRepository.save(p);
        emailService.sendProductoLlegoABsAs(
                p.getUser().getEmail(), p.getProductoNombre(), p.getId(),
                forwarding ? p.getCostoTotalArs() : p.getMontoSaldo());
        return PedidoAdminDTO.from(p);
    }

    @Transactional
    public PedidoAdminDTO confirmarSaldo(String pedidoId, ConfirmarPagoRequest req) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        boolean forwarding = "forwarding".equals(p.getTipoServicio());
        EstadoPedido estadoEsperado = forwarding
                ? EstadoPedido.esperando_pago : EstadoPedido.esperando_saldo;
        if (p.getEstado() != estadoEsperado) {
            throw new IllegalStateException("El pedido no está en estado " + estadoEsperado);
        }
        p.setEstado(forwarding ? EstadoPedido.pago_confirmado : EstadoPedido.saldo_confirmado);
        if (req != null && req.referencia() != null) p.setSaldoReferencia(req.referencia());
        pedidoRepository.save(p);
        emailService.sendSaldoConfirmado(p.getUser().getEmail(), p.getProductoNombre(), p.getId());
        return PedidoAdminDTO.from(p);
    }

    // ── Vendedores ───────────────────────────────────────────────────────────

    public List<VendedorDTO> getVendedores() {
        return profileRepository.findByTipo(TipoCuenta.vendedor).stream()
                .map(v -> VendedorDTO.from(v, listingRepository.countByVendedorId(v.getId())))
                .toList();
    }

    // ── Tienda (CRUD admin) ──────────────────────────────────────────────────

    public Page<TiendaProducto> getTiendaProductos(Pageable pageable) {
        return tiendaRepository.findAll(pageable);
    }

    @Transactional
    public TiendaProducto crearTiendaProducto(CrearTiendaProductoRequest req) {
        TiendaProducto p = new TiendaProducto();
        mapearTiendaProducto(p, req);
        p.setActivo(true);
        return tiendaRepository.save(p);
    }

    @Transactional
    public TiendaProducto actualizarTiendaProducto(UUID id, CrearTiendaProductoRequest req) {
        TiendaProducto p = tiendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        mapearTiendaProducto(p, req);
        return tiendaRepository.save(p);
    }

    @Transactional
    public TiendaProducto toggleTiendaProducto(UUID id) {
        TiendaProducto p = tiendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        p.setActivo(!p.isActivo());
        return tiendaRepository.save(p);
    }

    @Transactional
    public void eliminarTiendaProducto(UUID id) {
        if (!tiendaRepository.existsById(id)) {
            throw new EntityNotFoundException("Producto no encontrado");
        }
        tiendaRepository.deleteById(id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES_VALIDAS;
    static {
        Map<EstadoPedido, Set<EstadoPedido>> m = new java.util.HashMap<>();
        // Legacy
        m.put(EstadoPedido.en_proceso,          Set.of(EstadoPedido.comprado,           EstadoPedido.cancelado));
        m.put(EstadoPedido.comprado,             Set.of(EstadoPedido.en_transito,        EstadoPedido.cancelado));
        // Nuevo completo
        m.put(EstadoPedido.esperando_sena,       Set.of(EstadoPedido.sena_confirmada,    EstadoPedido.cancelado));
        m.put(EstadoPedido.sena_confirmada,      Set.of(EstadoPedido.en_transito,        EstadoPedido.cancelado));
        // Forwarding
        m.put(EstadoPedido.confirmado_sin_pago,  Set.of(EstadoPedido.en_transito,        EstadoPedido.cancelado));
        // Logística compartida
        m.put(EstadoPedido.en_transito,          Set.of(EstadoPedido.en_aduana,          EstadoPedido.cancelado));
        m.put(EstadoPedido.en_aduana,            Set.of(EstadoPedido.esperando_saldo,    EstadoPedido.esperando_pago,
                                                        EstadoPedido.entregado,          EstadoPedido.cancelado));
        // Pago final completo
        m.put(EstadoPedido.esperando_saldo,      Set.of(EstadoPedido.saldo_confirmado,   EstadoPedido.cancelado));
        m.put(EstadoPedido.saldo_confirmado,     Set.of(EstadoPedido.entregado));
        // Pago final forwarding
        m.put(EstadoPedido.esperando_pago,       Set.of(EstadoPedido.pago_confirmado,    EstadoPedido.cancelado));
        m.put(EstadoPedido.pago_confirmado,      Set.of(EstadoPedido.entregado));
        // Finales
        m.put(EstadoPedido.entregado,            Set.of());
        m.put(EstadoPedido.cancelado,            Set.of());
        TRANSICIONES_VALIDAS = java.util.Collections.unmodifiableMap(m);
    }

    private void validarTransicion(EstadoPedido actual, EstadoPedido nuevo) {
        Set<EstadoPedido> validos = TRANSICIONES_VALIDAS.getOrDefault(actual, Set.of());
        if (!validos.contains(nuevo)) {
            throw new IllegalStateException(
                    "Transición inválida: " + actual + " → " + nuevo);
        }
    }

    private void mapearTiendaProducto(TiendaProducto p, CrearTiendaProductoRequest req) {
        p.setNombre(req.nombre());
        p.setDescripcion(req.descripcion());
        p.setCategoria(req.categoria());
        p.setPrecioUsd(req.precioUsd());
        p.setStock(req.stock());
        p.setDestacado(req.destacado());
        if (req.imagenUrl() != null) p.setImagenUrl(req.imagenUrl());
        if (req.subcategoriaId() != null) {
            Subcategoria sub = subcategoriaRepository.findById(req.subcategoriaId()).orElse(null);
            p.setSubcategoria(sub);
        } else {
            p.setSubcategoria(null);
        }
    }
}
