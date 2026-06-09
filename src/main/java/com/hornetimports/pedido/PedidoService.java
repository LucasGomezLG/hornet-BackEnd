package com.hornetimports.pedido;

import com.hornetimports.cotizador.Cotizacion;
import com.hornetimports.cotizador.CotizacionRepository;
import com.hornetimports.cotizador.EstadoCotizacion;
import com.hornetimports.pedido.dto.*;
import com.hornetimports.solicitud.SolicitudItem;
import com.hornetimports.solicitud.dto.ConfirmarItemResponse;
import com.hornetimports.user.Profile;
import com.hornetimports.user.TipoCuenta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final CotizacionRepository cotizacionRepository;
    private final PedidoRepository     pedidoRepository;
    private final EntityManager        entityManager;

    @Value("${cripto.usdt.red:TRC-20 (TRON)}")
    private String usdtRed;

    @Value("${cripto.usdt.direccion:#{null}}")
    private String usdtDireccion;

    @Value("${transferencia.banco:#{null}}")
    private String transferenciaBanco;

    @Value("${transferencia.titular:#{null}}")
    private String transferenciaTitular;

    @Value("${transferencia.cbu:#{null}}")
    private String transferenciaCbu;

    @Value("${transferencia.alias:#{null}}")
    private String transferenciaAlias;

    @Transactional
    public ConfirmarPedidoResponse confirmarPedido(ConfirmarPedidoRequest req, Profile user) {
        Cotizacion cotizacion = cotizacionRepository.findById(req.cotizacionId())
                .orElseThrow(() -> new EntityNotFoundException("Cotización no encontrada"));

        if (!Objects.equals(cotizacion.getUserId(), user.getId())) {
            throw new AccessDeniedException("Esta cotización no te pertenece");
        }
        if (!cotizacion.isAprobadaPorAdmin() || cotizacion.getEstado() != EstadoCotizacion.aprobada) {
            throw new IllegalStateException("La cotización no está aprobada o ya fue procesada");
        }

        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('pedido_seq')").getSingleResult();
        String pedidoId = String.format("HI-%04d", nextVal.longValue());

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCotizacion(cotizacion);
        pedido.setUser(user);
        pedido.setProductoNombre(cotizacion.getNombreProducto());
        pedido.setProductoUrl(cotizacion.getProductoUrl());
        pedido.setPrecioUsd(cotizacion.getPrecioUsd());
        pedido.setCostoTotalArs(cotizacion.getCostoTotalArs());
        pedido.setTipoServicio(cotizacion.getTipoServicio());
        pedido.setEstado(EstadoPedido.en_proceso);
        pedido.setMetodoPago(req.metodoPago());
        pedidoRepository.save(pedido);

        cotizacion.setEstado(EstadoCotizacion.procesada);
        cotizacionRepository.save(cotizacion);

        return switch (req.metodoPago()) {
            case "cripto" -> {
                BigDecimal montoUsdt = BigDecimal.valueOf(cotizacion.getDesglose().total)
                        .setScale(2, RoundingMode.HALF_UP);
                String nota = String.format(
                        "Transferí exactamente %s USDT a esta dirección por la red TRC-20. " +
                        "En el memo escribí %s si tu billetera lo permite. " +
                        "Tu pedido quedará en espera hasta que validemos el pago (1-24 horas hábiles).",
                        montoUsdt, pedidoId);
                yield new ConfirmarPedidoResponse(pedidoId, "cripto", null,
                        new CriptoInstrucciones(usdtRed, usdtDireccion, montoUsdt, nota), null);
            }
            case "transferencia" -> {
                BigDecimal monto = cotizacion.getCostoTotalArs().setScale(2, RoundingMode.HALF_UP);
                String nota = String.format(
                        "Transferí exactamente $%s ARS. En el concepto incluí el ID del pedido %s. " +
                        "Tu pedido quedará en espera hasta que validemos el pago (1-24 horas hábiles).",
                        monto.toPlainString(), pedidoId);
                yield new ConfirmarPedidoResponse(pedidoId, "transferencia", null, null,
                        new TransferenciaInstrucciones(transferenciaBanco, transferenciaTitular,
                                transferenciaCbu, transferenciaAlias, monto, "ARS", nota));
            }
            default -> throw new IllegalArgumentException("Método de pago inválido");
        };
    }

    @Transactional
    public ConfirmarItemResponse confirmarDesdeSolicitudItem(
            SolicitudItem item, Profile user, String metodoPago) {

        Number nextVal = (Number) entityManager
                .createNativeQuery("SELECT nextval('pedido_seq')").getSingleResult();
        String pedidoId = String.format("HI-%04d", nextVal.longValue());

        boolean forwarding = "forwarding".equals(item.getTipoServicio());

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setSolicitudItem(item);
        pedido.setUser(user);
        pedido.setProductoNombre(item.getNombreProducto());
        pedido.setProductoUrl(item.getUrlProducto());
        pedido.setPrecioUsd(item.getPrecioFinalUsd());
        pedido.setCostoTotalArs(item.getCostoTotalArs());
        pedido.setTipoServicio(item.getTipoServicio());
        pedido.setOrigen(item.getOrigen());
        pedido.setMetodoPago(metodoPago);

        BigDecimal total = item.getCostoTotalArs().setScale(2, RoundingMode.HALF_UP);

        if (forwarding) {
            pedido.setEstado(EstadoPedido.confirmado_sin_pago);
            pedido.setMontoSaldo(total);
            pedidoRepository.save(pedido);

            return new ConfirmarItemResponse(
                    pedidoId, "forwarding",
                    null, null, total,
                    metodoPago, null, null, null,
                    "Tu pedido fue creado. Te avisamos por email cuando tu envío llegue a Buenos Aires para coordinar el pago.");
        }

        // Completo — seña 50 %
        BigDecimal sena  = total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal saldo = total.subtract(sena);

        pedido.setEstado(EstadoPedido.esperando_sena);
        pedido.setMontoSena(sena);
        pedido.setMontoSaldo(saldo);
        pedidoRepository.save(pedido);

        return switch (metodoPago) {
            case "cripto" -> {
                BigDecimal senaUsdt = item.getPrecioFinalUsd()
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                String nota = String.format(
                        "Transferí exactamente %s USDT (seña del 50%%). Escribí %s en el memo. " +
                        "El saldo restante ($%s ARS) se paga cuando el producto llegue a BsAs.",
                        senaUsdt, pedidoId, saldo.toPlainString());
                yield new ConfirmarItemResponse(
                        pedidoId, "completo", sena, saldo, null,
                        metodoPago, null,
                        new CriptoInstrucciones(usdtRed, usdtDireccion, senaUsdt, nota), null, null);
            }
            case "transferencia" -> {
                String nota = String.format(
                        "Transferí exactamente $%s ARS (seña del 50%%). Incluí %s en el concepto. " +
                        "El saldo restante ($%s ARS) se paga cuando el producto llegue a BsAs.",
                        sena.toPlainString(), pedidoId, saldo.toPlainString());
                yield new ConfirmarItemResponse(
                        pedidoId, "completo", sena, saldo, null,
                        metodoPago, null, null,
                        new TransferenciaInstrucciones(
                                transferenciaBanco, transferenciaTitular,
                                transferenciaCbu, transferenciaAlias, sena, "ARS", nota), null);
            }
            default -> throw new IllegalArgumentException("Método de pago inválido: " + metodoPago);
        };
    }

    public Page<Pedido> getPedidosUsuario(Profile user, Pageable pageable) {
        return pedidoRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    public Pedido getPedidoPorId(String pedidoId, Profile user) {
        Pedido p = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        boolean esAdmin = user.getTipo() == TipoCuenta.admin;
        if (!esAdmin && !p.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Sin acceso a este pedido");
        }
        return p;
    }

    public SeguimientoPublicoDTO getSeguimientoPublico(String codigo) {
        Pedido p = pedidoRepository.findById(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));
        return new SeguimientoPublicoDTO(
                p.getId(), p.getProductoNombre(), p.getEstado(), p.getTipoServicio(),
                p.getTrackingCode(), p.getTrackingCodigoCliente(), p.getCreatedAt());
    }

    @Transactional
    public void actualizarEstadoPorPago(String pedidoId, String mpStatus, String pagoRef) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            log.warn("Webhook MP: pedido {} no encontrado", pedidoId);
            return;
        }
        pedido.setPagoReferencia(pagoRef);
        if ("approved".equals(mpStatus)) {
            pedido.setEstado(EstadoPedido.comprado);
        } else if ("rejected".equals(mpStatus) || "cancelled".equals(mpStatus)) {
            pedido.setEstado(EstadoPedido.cancelado);
        }
        pedidoRepository.save(pedido);
        log.info("Pedido {} actualizado por webhook MP — status: {}", pedidoId, mpStatus);
    }
}
