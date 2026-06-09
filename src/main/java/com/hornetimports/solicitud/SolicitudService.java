package com.hornetimports.solicitud;

import com.hornetimports.cotizador.CotizadorEngine;
import com.hornetimports.cotizador.dto.CotizarRequest;
import com.hornetimports.cotizador.dto.CotizarResponse;
import com.hornetimports.email.EmailService;
import com.hornetimports.pedido.PedidoService;
import com.hornetimports.solicitud.dto.*;
import com.hornetimports.tipocambio.TipoCambioService;
import com.hornetimports.user.Profile;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository     solicitudRepository;
    private final SolicitudItemRepository itemRepository;
    private final PedidoService           pedidoService;
    private final EmailService            emailService;
    private final CotizadorEngine         cotizadorEngine;
    private final TipoCambioService       tipoCambioService;

    private static final int DIAS_EXPIRACION = 3;

    // ── Usuario ───────────────────────────────────────────────────────────────

    @Transactional
    public SolicitudDTO crear(CrearSolicitudRequest req, Profile user) {
        Solicitud s = new Solicitud();
        s.setUser(user);
        s.setNotaCliente(req.notaCliente());
        req.items().forEach(ir -> s.getItems().add(buildItem(ir, s)));
        return SolicitudDTO.from(solicitudRepository.save(s));
    }

    @Transactional(readOnly = true)
    public Page<SolicitudDTO> listarPorUsuario(Profile user, Pageable pageable) {
        return solicitudRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(SolicitudDTO::from);
    }

    @Transactional(readOnly = true)
    public SolicitudDTO getById(UUID id, Profile user) {
        Solicitud s = getSolicitudConItems(id);
        validarPropietario(s, user);
        return SolicitudDTO.from(s);
    }

    @Transactional
    public void cancelar(UUID id, Profile user) {
        Solicitud s = getSolicitudConItems(id);
        validarPropietario(s, user);
        if (!s.isPendiente()) {
            throw new IllegalStateException("Solo se puede cancelar una solicitud pendiente");
        }
        s.setEstado("cancelada");
        solicitudRepository.save(s);
    }

    @Transactional
    public SolicitudDTO agregarItem(UUID solicitudId, AgregarItemRequest req, Profile user) {
        Solicitud s = getSolicitudConItems(solicitudId);
        validarPropietario(s, user);
        validarEditable(s);
        s.getItems().add(buildItem(req, s));
        return SolicitudDTO.from(solicitudRepository.save(s));
    }

    @Transactional
    public SolicitudDTO editarItem(UUID solicitudId, UUID itemId, AgregarItemRequest req, Profile user) {
        Solicitud s = getSolicitudConItems(solicitudId);
        validarPropietario(s, user);
        validarEditable(s);
        SolicitudItem item = getItem(solicitudId, itemId);
        mapearItem(item, req);
        itemRepository.save(item);
        return SolicitudDTO.from(solicitudRepository.findByIdWithItems(solicitudId).orElseThrow());
    }

    @Transactional
    public SolicitudDTO eliminarItem(UUID solicitudId, UUID itemId, Profile user) {
        Solicitud s = getSolicitudConItems(solicitudId);
        validarPropietario(s, user);
        validarEditable(s);
        if (s.getItems().size() <= 1) {
            throw new IllegalStateException("La solicitud debe tener al menos un ítem");
        }
        s.getItems().removeIf(i -> i.getId().equals(itemId));
        return SolicitudDTO.from(solicitudRepository.save(s));
    }

    @Transactional
    public ConfirmarItemResponse confirmarItem(UUID solicitudId, UUID itemId,
                                               ConfirmarItemRequest req, Profile user) {
        Solicitud s = getSolicitudConItems(solicitudId);
        validarPropietario(s, user);
        if (!s.isCotizada()) {
            throw new IllegalStateException("La solicitud no está cotizada");
        }

        SolicitudItem item = getItem(solicitudId, itemId);
        if (!item.isAprobado()) {
            throw new IllegalStateException("El ítem no está aprobado o ya fue confirmado");
        }
        if (item.getCostoTotalArs() == null || item.getPrecioFinalUsd() == null) {
            throw new IllegalStateException("El ítem no tiene precio asignado");
        }

        ConfirmarItemResponse response = pedidoService.confirmarDesdeSolicitudItem(
                item, user, req.metodoPago());

        item.setEstadoItem("confirmado");
        itemRepository.save(item);
        actualizarEstadoSolicitudSiCompleta(s);

        return response;
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<SolicitudAdminDTO> listarAdmin(String estado, Pageable pageable) {
        Page<Solicitud> page = (estado != null && !estado.isBlank())
                ? solicitudRepository.findByEstadoOrderByCreatedAtDesc(estado, pageable)
                : solicitudRepository.findAll(pageable);
        return page.map(SolicitudAdminDTO::from);
    }

    @Transactional(readOnly = true)
    public SolicitudAdminDTO getByIdAdmin(UUID id) {
        return SolicitudAdminDTO.from(getSolicitudConItems(id));
    }

    @Transactional
    public SolicitudAdminDTO cotizar(UUID id, CotizarSolicitudRequest req) {
        Solicitud s = getSolicitudConItems(id);
        if (!s.isPendiente()) {
            throw new IllegalStateException("Solo se puede cotizar una solicitud pendiente");
        }

        for (CotizarItemRequest ir : req.items()) {
            SolicitudItem item = s.getItems().stream()
                    .filter(i -> i.getId().equals(ir.itemId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Ítem " + ir.itemId() + " no encontrado en esta solicitud"));
            aplicarCotizacion(item, ir);
        }

        s.setNotaAdmin(req.notaAdmin());
        s.setEstado("cotizada");
        s.setExpiresAt(OffsetDateTime.now().plusDays(DIAS_EXPIRACION));
        solicitudRepository.save(s);

        Profile user = s.getUser();
        String nombre = user.getNombre() != null ? user.getNombre() : user.getEmail();
        emailService.sendSolicitudCotizada(
                user.getEmail(), nombre, s.getId(), s.getItems().size());

        log.info("Solicitud {} cotizada por admin. Expira: {}", id, s.getExpiresAt());
        return SolicitudAdminDTO.from(getSolicitudConItems(id));
    }

    public CotizarResponse calcularSugerencia(UUID solicitudId, UUID itemId,
                                              String tipo, String tipoServicio,
                                              double precioUsd, double pesoKg,
                                              String categoria, String origen) {
        getSolicitudConItems(solicitudId); // valida existencia
        getItem(solicitudId, itemId);

        CotizarRequest cotReq = new CotizarRequest(
                "", "", java.math.BigDecimal.valueOf(precioUsd), java.math.BigDecimal.valueOf(pesoKg),
                categoria, tipo, tipoServicio, origen != null ? origen : "otro");
        return cotizadorEngine.calcular(cotReq, tipoCambioService.obtenerRate());
    }

    // ── Job de expiración ─────────────────────────────────────────────────────

    @Transactional
    public void expirarSolicitudesVencidas() {
        int solicitudes = solicitudRepository.expirarSolicitudesVencidas(OffsetDateTime.now());
        int items       = itemRepository.expirarItemsDeExpiradas();
        if (solicitudes > 0 || items > 0) {
            log.info("Solicitudes expiradas: {} — ítems expirados: {}", solicitudes, items);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Solicitud getSolicitudConItems(UUID id) {
        return solicitudRepository.findByIdWithItems(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada"));
    }

    private SolicitudItem getItem(UUID solicitudId, UUID itemId) {
        return itemRepository.findByIdAndSolicitudId(itemId, solicitudId)
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado"));
    }

    private void validarPropietario(Solicitud s, Profile user) {
        if (!s.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Sin acceso a esta solicitud");
        }
    }

    private void validarEditable(Solicitud s) {
        if (!s.isPendiente()) {
            throw new IllegalStateException(
                    "La solicitud no se puede modificar en estado " + s.getEstado());
        }
    }

    private SolicitudItem buildItem(AgregarItemRequest req, Solicitud s) {
        SolicitudItem item = new SolicitudItem();
        item.setSolicitud(s);
        mapearItem(item, req);
        return item;
    }

    private void mapearItem(SolicitudItem item, AgregarItemRequest req) {
        item.setNombreProducto(req.nombreProducto());
        item.setUrlProducto(req.urlProducto());
        item.setPrecioUsdRef(req.precioUsdRef());
        item.setPesoKg(req.pesoKg());
        item.setCategoria(req.categoria());
        item.setCantidad(req.cantidad());
        item.setOrigen(req.origen());
        item.setTipoServicio(req.tipoServicio());
        item.setTipo(req.tipo());
    }

    private void aplicarCotizacion(SolicitudItem item, CotizarItemRequest ir) {
        if (ir.aprobado()) {
            item.setPrecioFinalUsd(ir.precioFinalUsd());
            item.setCostoTotalArs(ir.costoTotalArs());
            item.setDesglose(ir.desglose());
            item.setNotaItem(ir.nota());
            item.setEstadoItem("aprobado");
        } else {
            item.setMotivoRechazo(ir.motivo());
            item.setNotaItem(ir.nota());
            item.setEstadoItem("rechazado");
        }
    }

    private void actualizarEstadoSolicitudSiCompleta(Solicitud s) {
        boolean todosTerminados = s.getItems().stream().allMatch(SolicitudItem::isTerminado);
        if (todosTerminados) {
            s.setEstado("procesada");
            solicitudRepository.save(s);
        }
    }
}
